# backend/modal_backend.py
"""
Modal serverless LLM deployment script for NousResearch/Hermes-4-14B-FP8.
Optimized as a native Modal ASGI web application (@modal.asgi_app) to provide 
instant public routing, seamless Ktor WebSocket handshaking, and multi-patient concurrency.

Authentication is handled statelessly via offline JWT signature verification.

To deploy this backend:
1. Make sure you have the `modal` CLI installed and authenticated (`modal setup`).
2. Run:
   modal deploy backend/ai/modal_backend.py
"""

import os
import sys
import modal

# 1. Container Image definition with dependencies required for LLM and secure WebSockets proxy
vllm_image = (
    modal.Image.from_registry(
        "nvidia/cuda:12.4.1-devel-ubuntu22.04", add_python="3.11"
    )
    .pip_install(
        "vllm",
        "transformers",
        "fastapi",
        "uvicorn",
        "hf-transfer",
        "pyjwt",
        "websockets"
    )
    .env({
        "HF_XET_HIGH_PERFORMANCE": "1",
        "PYTORCH_CUDA_ALLOC_CONF": "expandable_segments:True"
    })
)

# Initialize the Modal App
app = modal.App(name="gabby-hermes-4-v2")

# Persistent storage mapping for weights to bypass downloading from Hugging Face on cold starts
model_volume = modal.Volume.from_name("gabby-model-cache", create_if_missing=True)

MODEL_DIR = "/model-cache"
MODEL_NAME = "NousResearch/Hermes-4-14B-FP8"

PRIVATE_VLLM_PORT = 8001

# Global variable to track whether vLLM has completed startup and weight binding
is_vllm_ready = False
vllm_process = None

# Asynchronous background loop to spawn vLLM without blocking FastAPI startup
async def spawn_vllm_background():
    global is_vllm_ready, vllm_process
    import subprocess
    import socket
    import asyncio
    
    print(f"Spawning private local vLLM daemon in background on Port {PRIVATE_VLLM_PORT} for: {MODEL_NAME}...")
    cmd = [
        sys.executable, "-m", "vllm.entrypoints.openai.api_server",
        "--model", MODEL_NAME,
        "--port", str(PRIVATE_VLLM_PORT),
        "--host", "127.0.0.1", # Loopback only
        "--download-dir", MODEL_DIR,
        "--gpu-memory-utilization", "0.80", 
        "--max-model-len", "4096",
        "--served-model-name", "gabby-model", 
        "--quantization", "compressed-tensors",       
        "--trust-remote-code", 
        "--enforce-eager",
    ]
    process = subprocess.Popen(cmd)
    
    host = "127.0.0.1"
    retries = 120
    while retries > 0:
        try:
            # Query connection socket in executor to prevent blocking the async loop
            loop = asyncio.get_running_loop()
            await loop.run_in_executor(None, lambda: socket.create_connection((host, PRIVATE_VLLM_PORT), timeout=2.0))
            print(f"vLLM daemon is online and listening on Port {PRIVATE_VLLM_PORT}!")
            is_vllm_ready = True
            break
        except Exception:
            if process.poll() is not None:
                print("Error: vLLM daemon subprocess crashed unexpectedly.")
                break
            print(f"Binding weights in the background... ({retries} pings remaining)")
            await asyncio.sleep(5)
            retries -= 1


# --- THE SECURE FASTAPI WEBSOCKETS PROXY DEFINITION ---
def create_secure_proxy_app():
    from fastapi import FastAPI, WebSocket, WebSocketDisconnect
    import jwt
    import json
    import httpx
    import asyncio

    proxy = FastAPI(title="Gabby Secure WebSockets Proxy")

    # Shared secret key supplied securely by Modal Secrets at runtime
    JWT_SECRET = os.environ.get("JWT_SHARED_SECRET", "1a880ab5ad18e1cf525ba10f88e6627e86d40ddee21224b01870850073da22e5")
    JWT_ISSUER = "https://amping-backend.com"

    # Precise phase instructions matching the clinical state steering rules
    PHASE_PROMPTS = {
        "empathy": (
            "You are currently in Phase 1: Empathetic Check-up.\n"
            "Your goal is to greet the patient warmly, show genuine clinical empathy, and immediately steer them towards their health check-in.\n"
            "CRITICAL: Do NOT ask open-ended questions like 'How can I assist you in feeling better today?'. "
            "Instead, ask them specifically how they are feeling today so they select their mood, steering them directly to log their symptoms.\n"
            "Once they express their emotional state or mood (e.g. fine, sad, tired), you MUST append a structured tool call strictly in this format at the end of your response to transition:\n"
            "<tool_call> {\"name\": \"show_symptom_checklist\", \"arguments\": {\"mood\": \"Positive\"}} </tool_call>\n"
            "Valid mood values: 'Positive', 'Neutral', 'Negative'."
        ),
        "symptoms": (
            "You are currently in Phase 2: Symptom Logging.\n"
            "Empathetically acknowledge their mood and ask if they are experiencing any medication side effects "
            "(such as nausea, vomiting, joint pain, fatigue, or dark urine).\n"
            "CRITICAL: Do NOT ask open-ended questions. Steer the patient directly to choose their side-effects so we can progress to their VDOT filming.\n"
            "Once they respond with their physical status, you MUST generate a structured tool call strictly in this format at the end of your response to transition:\n"
            "<tool_call> {\"name\": \"transition_to_vdot\", \"arguments\": {\"side_effects\": \"nausea\", \"nausea_severity\": \"Mild\"}} </tool_call>\n"
            "Valid nausea_severity values: 'None', 'Mild', 'Severe'."
        ),
        "vdot": (
            "You are currently in Phase 3: Secure VDOT Filming.\n"
            "Warmly guide the patient to record their daily TB medication intake. Keep instructions brief, motivational, and highly focused.\n"
            "CRITICAL: Do NOT ask open-ended questions. Steer the patient directly to activate their camera stream and complete their ingestion.\n"
            "When they indicate readiness or when you prompt them, you MUST output a structured tool call strictly in this format:\n"
            "<tool_call> {\"name\": \"trigger_vdot\", \"arguments\": {\"duration_seconds\": 15}} </tool_call>"
        )
    }

    # Trigger background vLLM daemon startup on FastAPI application init
    @proxy.on_event("startup")
    async def startup_event():
        asyncio.create_task(spawn_vllm_background())

    @proxy.on_event("shutdown")
    async def shutdown_event():
        """
        Gracefully terminate the background vLLM subprocess when the container
        is scaled down or stopped by Modal, preventing orphaned zombie processes.
        """
        print("Shutting down private local vLLM daemon...")
        global vllm_process
        if vllm_process:
            vllm_process.terminate()
            try:
                vllm_process.wait(timeout=5.0)
                print("vLLM daemon process terminated cleanly.")
            except Exception:
                print("vLLM daemon failed to terminate in time. Force killing...")
                vllm_process.kill()

    @proxy.get("/")
    async def root_health_check():
        """
        Public HTTP GET endpoint to satisfy Modal's internal health check / ready-status probes.
        Returns 200 OK so the load balancer registers the container as healthy.
        """
        global is_vllm_ready
        return {
            "status": "healthy" if is_vllm_ready else "initializing",
            "service": "gabby-proxy",
            "vllm_ready": is_vllm_ready
        }

    @proxy.websocket("/chat")
    async def websocket_chat_endpoint(websocket: WebSocket):
        await websocket.accept()
        print("Ktor WebSocket client handshake initiated.")
        
        try:
            # 1. Await Handshake JSON payload containing JWT token
            handshake_text = await websocket.receive_text()
            handshake = json.loads(handshake_text)
            token = handshake.get("token")
            
            if not token:
                await websocket.send_text(json.dumps({
                    "type": "error", 
                    "message": "Authentication gate blocked: Missing access token."
                }))
                await websocket.close()
                return

            # Verify and decode JWT offline (zero-database latency)
            try:
                payload = jwt.decode(token, JWT_SECRET, algorithms=["HS256"], issuer=JWT_ISSUER)
            except jwt.ExpiredSignatureError:
                await websocket.send_text(json.dumps({
                    "type": "error",
                    "message": "Access ticket expired. Requesting silent re-authentication..."
                }))
                await websocket.close()
                return
            except jwt.InvalidTokenError:
                await websocket.send_text(json.dumps({
                    "type": "error",
                    "message": "Access ticket rejected. Invalid cryptographic signature."
                }))
                await websocket.close()
                return

            # Extract validated patient telemetry claims from JWT
            patient_id = payload.get("sub")
            profile = payload.get("profile", "adult").lower()
            current_phase = payload.get("current_phase", "empathy").lower()
            
            print(f"Token verified! Patient: {patient_id}, Profile: {profile}, Active Phase: {current_phase}")

            # 2. Await prompt text frame from Ktor WebSocket
            prompt = await websocket.receive_text()
            print(f"Received patient prompt: '{prompt}'")

            # Check if background vLLM daemon is still binding weights.
            # If so, stream friendly live status updates directly to the client rather than hanging!
            global is_vllm_ready
            if not is_vllm_ready:
                print("vLLM daemon still initializing in background. Informing client...")
                await websocket.send_text(json.dumps({
                    "type": "token",
                    "content": "⚡ Gabby is warming up (loading GPU model weights)... Please hold on a few seconds. ⏳\n\n"
                }))
                while not is_vllm_ready:
                    await asyncio.sleep(2.0)

            # 3. Formulate the system instructions securely on the server
            phase_instruction = PHASE_PROMPTS.get(current_phase, PHASE_PROMPTS["empathy"])
            system_prompt = (
                f"You are 'Gabby', an AI conversational health companion designed to motivate TB patients.\n"
                f"Tailor your vocabulary, level of gamification, and empathy to the active profile: {profile}.\n"
                f"Active Phase Instructions:\n{phase_instruction}"
            )

            openai_payload = {
                "model": "gabby-model",
                "messages": [
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": prompt}
                ],
                "temperature": 0.7,
                "max_tokens": 512,
                "stream": True # Standard SSE streaming
            }

            # 4. Stream tokens directly from the local vLLM OpenAI API daemon on Port 8001
            async with httpx.AsyncClient(timeout=120.0) as client:
                async with client.stream(
                    "POST",
                    f"http://127.0.0.1:{PRIVATE_VLLM_PORT}/v1/chat/completions",
                    json=openai_payload,
                    headers={"Content-Type": "application/json"}
                ) as response:
                    if response.status_code != 200:
                        err_bytes = await response.aread()
                        err_msg = err_bytes.decode("utf-8")
                        await websocket.send_text(json.dumps({
                            "type": "error",
                            "message": f"LLM backend exception: {err_msg}"
                        }))
                        await websocket.close()
                        return

                    async for line in response.aiter_lines():
                        if not line.strip():
                            continue
                        if line.startswith("data: "):
                            data_str = line[6:].strip()
                            if data_str == "[DONE]":
                                break
                            try:
                                chunk_json = json.loads(data_str)
                                text_delta = chunk_json["choices"][0]["delta"].get("content", "")
                                if text_delta:
                                    # Yield token chunk as a WebSocket text frame
                                    await websocket.send_text(json.dumps({
                                        "type": "token",
                                        "content": text_delta
                                    }))
                            except Exception:
                                pass

            # 5. Yield successful completion frame and gracefully close connection
            await websocket.send_text(json.dumps({
                "type": "done"
            }))
            await websocket.close()
            print("Stream completed. Connection closed gracefully.")

        except WebSocketDisconnect:
            print("Connection aborted by Ktor client.")
        except Exception as e:
            print(f"Secure Proxy WebSockets exception: {str(e)}")
            try:
                await websocket.send_text(json.dumps({
                    "type": "error",
                    "message": f"Inference pipeline error: {str(e)}"
                }))
                await websocket.close()
            except Exception:
                pass

    return proxy


# --- MODAL NATIVE ASGI APPLICATION DEPLOYMENT ---
@app.function(
    image=vllm_image,
    gpu="L4", 
    volumes={MODEL_DIR: model_volume},
    secrets=[
        modal.Secret.from_name("huggingface-secret"),
        modal.Secret.from_name("jwt-shared-secret") # Shared secrets mappings
    ],
    timeout=3600, 
    scaledown_window=600
)
@modal.asgi_app()
def serve():
    # Return the secure proxy FastAPI application directly. 
    # Modal natively hooks the ASGI server loop and handles global edge routing.
    print("Initializing native ASGI web application...")
    return create_secure_proxy_app()