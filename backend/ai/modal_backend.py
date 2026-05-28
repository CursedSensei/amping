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
            "Your goal is to greet the patient warmly and guide them toward their daily health check-in.\n"
            "\n"
            "RULE A — EMOTIONAL DISCLOSURE: If the patient expresses sadness, loneliness, anxiety, worry, fear, grief, or any emotional difficulty:\n"
            "  - Respond with genuine, warm empathy. Acknowledge what they shared specifically — do not be generic.\n"
            "  - Ask ONE caring follow-up question so they feel heard (e.g. 'What has been weighing on you?' or 'Would you like to tell me more?').\n"
            "  - Do NOT emit show_symptom_checklist in this response.\n"
            "  - On your NEXT response after they reply, warmly bridge to the check-in and emit show_symptom_checklist.\n"
            "\n"
            "RULE B — GREETING OR NEUTRAL MESSAGE: If the patient's message is a greeting or a neutral/positive statement:\n"
            "  - Do NOT ask how they are feeling or use open-ended questions.\n"
            "  - Greet warmly and steer them directly to the symptom log.\n"
            "  - You MUST emit show_symptom_checklist at the very end of your response.\n"
            "\n"
            "EXAMPLE — Emotional disclosure (Rule A, no tool call):\n"
            "Patient: 'I am feeling sad today.'\n"
            "Gabby: 'I am so sorry to hear that. You do not have to carry that alone. What has been weighing on your heart?'\n"
            "\n"
            "EXAMPLE — Bridge after emotional exchange (Rule A second turn, with tool call):\n"
            "Patient: 'I just miss my family a lot.'\n"
            "Gabby: 'I hear you. That kind of longing is real and it matters. While I hold that with you, let us also take care of your body today.\n"
            "<tool_call> {\"name\": \"show_symptom_checklist\"} </tool_call>'\n"
            "\n"
            "EXAMPLE — Neutral greeting (Rule B, with tool call):\n"
            "Patient: 'Hi Gabby'\n"
            "Gabby: 'Hello! I am glad you are here. Let us take care of your health today.\n"
            "<tool_call> {\"name\": \"show_symptom_checklist\"} </tool_call>'"
        ),
        "symptoms": (
            "You are currently in Phase 2: Symptom Logging.\n"
            "The patient has ALREADY submitted their symptom checklist. Their reported symptoms are in their message (e.g. 'Symptoms reported: nausea (None severity).' or 'Symptoms reported: no side effects.').\n"
            "CRITICAL: The checklist is ALREADY COMPLETE. Do NOT ask them to fill out a checklist or choose symptoms again.\n"
            "Read their reported symptoms carefully. Acknowledge them with brief, genuine empathy (e.g. note the specific symptom if present, or affirm they are clear). Then immediately generate the transition_to_vdot tool call.\n"
            "Extract side_effects and nausea_severity from their message. Valid nausea_severity values: 'None', 'Mild', 'Severe'.\n"
            "You MUST generate a structured tool call strictly in this format at the very end of your response:\n"
            "<tool_call> {\"name\": \"transition_to_vdot\", \"arguments\": {\"side_effects\": \"nausea\", \"nausea_severity\": \"None\"}} </tool_call>\n"
            "EXAMPLE RESPONSE (patient reported nausea, None severity):\n"
            "Noted. Nausea logged with no severe intensity today. Please drink some water and rest. Are you ready to record your medication video now?\n"
            "<tool_call> {\"name\": \"transition_to_vdot\", \"arguments\": {\"side_effects\": \"nausea\", \"nausea_severity\": \"None\"}} </tool_call>\n"
            "EXAMPLE RESPONSE (no side effects reported):\n"
            "Wonderful. No side effects noted today. Are you ready to begin your VDOT recording?\n"
            "<tool_call> {\"name\": \"transition_to_vdot\", \"arguments\": {\"side_effects\": \"none\", \"nausea_severity\": \"None\"}} </tool_call>"
        ),
        "vdot": (
            "You are currently in Phase 3: Secure VDOT Filming.\n"
            "Warmly guide the patient to record their daily TB medication intake. Keep instructions brief, motivational, and highly focused.\n"
            "CRITICAL: Do NOT ask open-ended questions. Steer the patient directly to activate their camera stream and complete their ingestion.\n"
            "When they indicate readiness or when you prompt them, you MUST output a structured tool call strictly in this format:\n"
            "<tool_call> {\"name\": \"trigger_vdot\", \"arguments\": {\"duration_seconds\": 15}} </tool_call>\n"
            "EXAMPLE RESPONSE:\n"
            "Excellent. Please hold the pill clearly in the frame, take it, and show me you have swallowed it safely. Press the button below to start filming.\n"
            "<tool_call> {\"name\": \"trigger_vdot\", \"arguments\": {\"duration_seconds\": 15}} </tool_call>"
        ),
        "complete": (
            "The patient has just successfully completed their daily VDOT medication check-in and video upload.\n"
            "Your task is to craft a warm, brief, and genuinely personal completion message.\n"
            "\n"
            "INSTRUCTIONS:\n"
            "- Review the full conversation history above. If the patient shared emotional difficulty earlier "
            "(sadness, grief, loss, worry, loneliness), acknowledge that they completed today's check-in despite "
            "what they are carrying. Be specific — name what they shared. Do not be generic.\n"
            "- The patient's motivation for taking this medication is in the system context. Weave it into your "
            "message naturally, as something you genuinely remember — do not quote it literally or say 'because of X'.\n"
            "- Be celebratory but sincere and concise. Avoid hollow or generic praise.\n"
            "- You MUST emit this tool call at the very end of your response:\n"
            "<tool_call> {\"name\": \"transition_to_success\"} </tool_call>\n"
            "\n"
            "EXAMPLE (patient shared grief, motivated by family):\n"
            "Even on a day as heavy as this one, you showed up — for yourself and for the people who love you. "
            "That is not a small thing. Check-in complete.\n"
            "<tool_call> {\"name\": \"transition_to_success\"} </tool_call>\n"
            "\n"
            "EXAMPLE (no emotional context, patient motivated by getting well):\n"
            "Well done. You are one step closer to getting well. Your check-in is locked in — keep that streak going.\n"
            "<tool_call> {\"name\": \"transition_to_success\"} </tool_call>"
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
            motivation = payload.get("motivation", "")
            
            print(f"Token verified! Patient: {patient_id}, Profile: {profile}, Active Phase: {current_phase}")

            # 2. Await conversation history JSON frame from Ktor WebSocket.
            # Format: [{"role": "user"|"assistant", "content": "..."}, ...]
            messages_text = await websocket.receive_text()
            try:
                chat_messages = json.loads(messages_text)
                if not isinstance(chat_messages, list):
                    raise ValueError("Expected a JSON array of messages")
            except (json.JSONDecodeError, ValueError, TypeError):
                # Backward-compat fallback: treat as a plain-text single user prompt
                chat_messages = [{"role": "user", "content": messages_text}]

            # Derive the last user message for intercepts and fail-safe keyword matching
            prompt = next(
                (m.get("content", "") for m in reversed(chat_messages) if m.get("role") == "user"),
                ""
            )
            print(f"Received {len(chat_messages)} messages. Last user prompt: '{prompt}'")

            # When the patient signals upload complete, override the phase so the LLM
            # crafts a personalized completion message using the full conversation history
            # (Phase 1 emotional context + onboarding motivation) rather than a template.
            if "vdot upload complete" in prompt.lower() or "upload complete" in prompt.lower() or "ingestion complete" in prompt.lower():
                current_phase = "complete"

            # Check if background vLLM daemon is still binding weights.
            # If so, stream friendly live status updates directly to the client rather than hanging!
            global is_vllm_ready
            if not is_vllm_ready:
                print("vLLM daemon still initializing in background. Waiting for model to load...")
                while not is_vllm_ready:
                    await asyncio.sleep(2.0)

            # 3. Formulate the system instructions securely on the server
            phase_instruction = PHASE_PROMPTS.get(current_phase, PHASE_PROMPTS["empathy"])
            system_prompt = (
                f"You are 'Gabby', an AI conversational health companion designed to motivate TB patients.\n"
                f"Tailor your vocabulary, level of gamification, and empathy to the active profile: {profile}.\n"
                f"Active Phase Instructions:\n{phase_instruction}\n"
            )
            if motivation:
                system_prompt += f"\nThe patient's personal motivation for continuing treatment is: '{motivation}'. Refer to this motivation to encourage and inspire them."

            system_prompt += (
                f"\nCRITICAL STYLE RULES:\n"
                f"- STRICTLY FORBIDDEN TO USE EMOJIS: Do NOT use any emojis, stars (e.g. 🌟), icons, smiley faces, emoticons, or decorative symbols under any circumstances. All your replies must contain plain text only. If you use an emoji, you fail.\n"
                f"- USE MINIMAL LANGUAGE: Be extremely concise, direct, and brief. Use minimal sentences. Avoid extra explanations or chatty filler text.\n"
                f"- PROFESSIONAL CHILD-LIKE MANNERISM: Maintain a professional, clinically supportive, and safe tone, but express it with innocent, simple, child-like mannerisms (using simple words, gentle vocabulary, straightforward instructions, and honest guidance)."
            )

            # Build the full LLM message list: system prompt + conversation history
            llm_messages = [{"role": "system", "content": system_prompt}]
            for m in chat_messages:
                role = m.get("role", "user")
                content = m.get("content", "")
                if role in ("user", "assistant") and content:
                    llm_messages.append({"role": role, "content": content})

            openai_payload = {
                "model": "gabby-model",
                "messages": llm_messages,
                "temperature": 0.0,
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

                    full_response = ""
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
                                    full_response += text_delta
                                    # Yield token chunk as a WebSocket text frame
                                    await websocket.send_text(json.dumps({
                                        "type": "token",
                                        "content": text_delta
                                    }))
                            except Exception:
                                pass

                    # Programmatic fail-safe: inject the correct tool call if the LLM forgot it
                    if "<tool_call>" not in full_response:
                        prompt_lower = prompt.lower()

                        if current_phase == "empathy":
                            # If the patient expressed an emotional state, let Gabby's empathetic
                            # response stand without forcing the checklist. The LLM is instructed
                            # to bridge to the checklist on the next turn.
                            emotional_keywords = [
                                "sad", "depress", "anxious", "anxiety", "worried", "worry",
                                "scared", "afraid", "lonely", "stressed", "stress", "overwhelm",
                                "angry", "upset", "unhappy", "crying", "struggling", "nervous",
                                "frustrated", "heartbroken", "grieving", "grief", "miserable"
                            ]
                            is_emotional = any(kw in prompt_lower for kw in emotional_keywords)
                            if not is_emotional:
                                transition_question = {
                                    "youth": "\nLet us check your body today. Please fill out the symptom checklist below.",
                                    "senior": "\nLet us review your body today, dear friend. Please check any symptoms you are feeling in the checklist card below.",
                                    "adult": "\nLet us now document your physical symptoms. Please fill out the interactive symptom checklist below to log your status."
                                }.get(profile, "\nLet us now document your physical symptoms. Please fill out the interactive symptom checklist below to log your status.")
                                fallback_block = f"{transition_question}\n\n<tool_call> {{\"name\": \"show_symptom_checklist\"}} </tool_call>"
                                await websocket.send_text(json.dumps({"type": "token", "content": fallback_block}))

                        elif current_phase == "symptoms":
                            # Patient has submitted the checklist — extract symptoms and transition to VDOT
                            severity = (
                                "Severe" if any(w in prompt_lower for w in ["severe", "very", "intense", "bad", "high"])
                                else "None" if any(w in prompt_lower for w in ["none", "no side", "nothing", "zero", "no symptom"])
                                else "Mild"
                            )
                            side_effects = (
                                "nausea" if "nausea" in prompt_lower
                                else "fatigue" if "fatigue" in prompt_lower or "tired" in prompt_lower
                                else "joint pain" if "joint" in prompt_lower
                                else "none"
                            )
                            if severity == "None" and side_effects == "none":
                                side_effects = "none"
                            transition_text = {
                                "youth": "\nGot it logged! Are you ready to start your VDOT check-in now?",
                                "senior": "\nThank you for telling me, dear. Are you ready to start the camera for your VDOT dose?",
                                "adult": "\nTelemetry logs updated. Please confirm when ready to begin VDOT filming."
                            }.get(profile, "\nSymptoms logged. Ready to begin VDOT filming?")
                            fallback_block = f"{transition_text}\n\n<tool_call> {{\"name\": \"transition_to_vdot\", \"arguments\": {{\"side_effects\": \"{side_effects}\", \"nausea_severity\": \"{severity}\"}}}} </tool_call>"
                            await websocket.send_text(json.dumps({"type": "token", "content": fallback_block}))

                        elif current_phase == "vdot":
                            is_confirmed = any(w in prompt_lower for w in ["yes", "start", "ready", "confirm", "ok", "sure", "now", "begin", "go", "video", "camera", "pill"]) \
                                and not any(w in prompt_lower for w in ["not yet", "no", "wait", "hold", "stop", "cancel", "later"])
                            duration = 20 if profile == "senior" else 15
                            if is_confirmed:
                                transition_text = {
                                    "youth": "\nOpening the camera now. Keep your face and the pill in the frame!",
                                    "senior": "\nActivating the camera now, dear friend. Please take your time.",
                                    "adult": "\nActivating VDOT filming now. Please ensure your swallow is clearly visible."
                                }.get(profile, "\nStarting VDOT recording now.")
                                fallback_block = f"{transition_text}\n\n<tool_call> {{\"name\": \"trigger_vdot\", \"arguments\": {{\"duration_seconds\": {duration}}}}} </tool_call>"
                                await websocket.send_text(json.dumps({"type": "token", "content": fallback_block}))

                        elif current_phase == "complete":
                            # Always guarantee the success transition fires
                            fallback_block = "\n\n<tool_call> {\"name\": \"transition_to_success\"} </tool_call>"
                            await websocket.send_text(json.dumps({"type": "token", "content": fallback_block}))

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
@modal.concurrent(max_inputs=100)
@modal.asgi_app()
def serve():
    # Return the secure proxy FastAPI application directly. 
    # Modal natively hooks the ASGI server loop and handles global edge routing.
    print("Initializing native ASGI web application...")
    return create_secure_proxy_app()