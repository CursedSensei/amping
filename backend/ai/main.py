# backend/main.py
import json
import os
import sys
from typing import List, Dict, Any, Optional
from datetime import datetime, timedelta
import httpx

# Ensure parent directory is in the path to allow direct execution
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel
from ai.config import settings

app = FastAPI(title="Amping: Gabby AI Patient Hub")

STATE_FILE = "session_state.json"

# Default state template
DEFAULT_STATE = {
    "profile": "adult", # "youth", "adult", "senior"
    "streak": 5,
    "xp": 450,
    "level": 2,
    "logs": [
        # Prepopulate past 14 days of compliance logs to show realistic historical data
        {"day": -13, "status": "success", "type": "quota", "timestamp": (datetime.now() - timedelta(days=13)).isoformat()},
        {"day": -12, "status": "success", "type": "quota", "timestamp": (datetime.now() - timedelta(days=12)).isoformat()},
        {"day": -11, "status": "success", "type": "quota", "timestamp": (datetime.now() - timedelta(days=11)).isoformat()},
        {"day": -10, "status": "success", "type": "grace", "timestamp": (datetime.now() - timedelta(days=10)).isoformat()},
        {"day": -9, "status": "success", "type": "quota", "timestamp": (datetime.now() - timedelta(days=9)).isoformat()},
        {"day": -8, "status": "success", "type": "quota", "timestamp": (datetime.now() - timedelta(days=8)).isoformat()},
        {"day": -7, "status": "success", "type": "quota", "timestamp": (datetime.now() - timedelta(days=7)).isoformat()},
        {"day": -6, "status": "success", "type": "quota", "timestamp": (datetime.now() - timedelta(days=6)).isoformat()},
        {"day": -5, "status": "success", "type": "quota", "timestamp": (datetime.now() - timedelta(days=5)).isoformat()},
        {"day": -4, "status": "success", "type": "grace", "timestamp": (datetime.now() - timedelta(days=4)).isoformat()},
        {"day": -3, "status": "success", "type": "quota", "timestamp": (datetime.now() - timedelta(days=3)).isoformat()},
        {"day": -2, "status": "success", "type": "quota", "timestamp": (datetime.now() - timedelta(days=2)).isoformat()},
        {"day": -1, "status": "success", "type": "quota", "timestamp": (datetime.now() - timedelta(days=1)).isoformat()},
    ],
    "today_status": "pending", # "pending", "success_quota", "success_grace", "failed"
    "current_day": 0,
    "current_phase": "empathy", # "empathy", "symptoms", "vdot"
    "clinical_notes": {
        "side_effects": "Pending",
        "nausea_severity": "Pending"
    }
}

def load_state() -> Dict[str, Any]:
    state = DEFAULT_STATE.copy()
    if os.path.exists(STATE_FILE):
        try:
            with open(STATE_FILE, "r") as f:
                loaded = json.load(f)
                for k, v in loaded.items():
                    if k == "clinical_notes" and isinstance(v, dict):
                        state["clinical_notes"].update(v)
                    else:
                        state[k] = v
        except Exception:
            return DEFAULT_STATE.copy()
    return state

def save_state(state: Dict[str, Any]):
    with open(STATE_FILE, "w") as f:
        json.dump(state, f, indent=2)

class SymptomsPayload(BaseModel):
    side_effects: str
    nausea_severity: str

class ChatMessage(BaseModel):
    role: str # "user", "assistant"
    content: str

class ChatPayload(BaseModel):
    messages: List[ChatMessage]
    profile: str
    motivation: Optional[str] = None

class UpdateProfileRequest(BaseModel):
    profile: str

class SimulationRequest(BaseModel):
    action: str # "quota_success", "grace_success", "tier_0_checkin", "tier_1_lapse", "tier_2_lapse", "tier_3_lapse"

# Streak and Gamification Logic
def calculate_xp_level(xp: int) -> tuple[int, int]:
    # Simple progression: Level is based on 500 XP intervals
    # Level 1: 0 - 499
    # Level 2: 500 - 999
    # Level 3: 1000 - 1499, etc.
    level = (xp // 500) + 1
    xp_in_level = xp % 500
    return level, xp_in_level

@app.get("/api/state")
def get_state():
    state = load_state()
    level, xp_in_level = calculate_xp_level(state["xp"])
    state["level"] = level
    state["xp_in_level"] = xp_in_level
    state["xp_needed"] = 500
    return state

@app.post("/api/profile")
def update_profile(req: UpdateProfileRequest):
    state = load_state()
    if req.profile not in ["youth", "adult", "senior"]:
        raise HTTPException(status_code=400, detail="Invalid profile type.")
    state["profile"] = req.profile
    save_state(state)
    return get_state()

@app.post("/api/symptoms")
def submit_symptoms(req: SymptomsPayload):
    state = load_state()
    state["current_phase"] = "vdot"
    state["clinical_notes"]["side_effects"] = req.side_effects
    state["clinical_notes"]["nausea_severity"] = req.nausea_severity
    save_state(state)
    return get_state()

@app.post("/api/upload_video")
async def upload_video(request: Request):
    content = await request.body()
    backend_dir = os.path.dirname(os.path.abspath(__file__))
    recordings_dir = os.path.join(backend_dir, "recordings")
    os.makedirs(recordings_dir, exist_ok=True)
    
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"vdot_{timestamp}.webm"
    filepath = os.path.join(recordings_dir, filename)
    
    with open(filepath, "wb") as f:
        f.write(content)
        
    relative_path = os.path.join("recordings", filename)
    return {"status": "success", "filepath": relative_path}

@app.post("/api/reset_today")
def reset_today():
    state = load_state()
    state["today_status"] = "pending"
    state["current_phase"] = "empathy"
    state["clinical_notes"] = {
        "side_effects": "Pending",
        "nausea_severity": "Pending"
    }
    
    # Remove today's log if it exists in logs list
    if state.get("logs"):
        latest_log = state["logs"][-1]
        if latest_log.get("day", -1) >= 0:
            state["logs"].pop()
            
    state["current_day"] = max(0, state.get("current_day", 1) - 1)
    save_state(state)
    return get_state()

@app.post("/api/reset")
def reset_state():
    state = DEFAULT_STATE.copy()
    save_state(state)
    return get_state()

@app.post("/api/simulate")
def simulate_day(req: SimulationRequest):
    state = load_state()
    
    current_day = state.get("current_day", 0) + 1
    state["current_day"] = current_day
    
    action = req.action
    profile = state["profile"]
    
    # Mathematical rules from Group B Final Paper:
    # Quota Gate (Success): Streak + 1, Full XP (100 XP)
    # Grace Window Gate (Success in extra 6h): Streak preserved, Half XP (50 XP)
    # Penalties:
    # - Tier 0 (Isolated lapse): Streak preserved, compassionate warning checkin
    # - Tier 1 (Low freq, Youth ceiling): S_t = floor(S_t * 0.75), min 1
    # - Tier 2 (Med freq, Senior ceiling): S_t = floor(S_t * 0.50), min 1
    # - Tier 3 (High freq): S_t = 0 (Adult only)
    
    log_entry = {
        "day": current_day,
        "timestamp": datetime.now().isoformat(),
        "status": "failed",
        "type": "none"
    }
    
    if action == "quota_success":
        state["streak"] += 1
        state["xp"] += 100
        state["today_status"] = "success_quota"
        log_entry["status"] = "success"
        log_entry["type"] = "quota"
        
        # Reset conversation phase for the next day
        state["current_phase"] = "empathy"
        state["clinical_notes"] = {
            "side_effects": "Pending",
            "nausea_severity": "Pending"
        }
        
    elif action == "grace_success":
        # Streak is fully preserved (no change to streak number, but not reset!)
        state["xp"] += 50
        state["today_status"] = "success_grace"
        log_entry["status"] = "success"
        log_entry["type"] = "grace"
        
        # Reset conversation phase for the next day
        state["current_phase"] = "empathy"
        state["clinical_notes"] = {
            "side_effects": "Pending",
            "nausea_severity": "Pending"
        }
        
    elif action == "tier_0_checkin":
        # Tier 0 preserves the streak with compassionate AI check-in
        # Streak is fully preserved
        state["today_status"] = "failed"
        log_entry["status"] = "failed"
        log_entry["type"] = "tier_0"
        
    elif action == "tier_1_lapse":
        # Tier 1 applies a partial reset (S_t = floor(S_t * 0.75), minimum 1)
        original_streak = state["streak"]
        state["streak"] = max(1, int(original_streak * 0.75))
        state["today_status"] = "failed"
        log_entry["status"] = "failed"
        log_entry["type"] = "tier_1"
        
    elif action == "tier_2_lapse":
        # Tier 2 applies a deeper partial reset (S_t = floor(S_t * 0.50), minimum 1)
        original_streak = state["streak"]
        state["streak"] = max(1, int(original_streak * 0.50))
        state["today_status"] = "failed"
        log_entry["status"] = "failed"
        log_entry["type"] = "tier_2"
        
    elif action == "tier_3_lapse":
        # Tier 3 executes a full reset (S_t = 0)
        # However, Programmatic Filter cap prevents Youth (Tier 1 cap) and Senior (Tier 2 cap)
        # from full reset! Let's enforce demographic safety caps:
        if profile == "youth":
            # Capped at Tier 1 penalty (S_t = floor(S_t * 0.75), minimum 1)
            state["streak"] = max(1, int(state["streak"] * 0.75))
            log_entry["type"] = "tier_1_cap"
        elif profile == "senior":
            # Capped at Tier 2 penalty (S_t = floor(S_t * 0.50), minimum 1)
            state["streak"] = max(1, int(state["streak"] * 0.50))
            log_entry["type"] = "tier_2_cap"
        else:
            # Adults get the full reset!
            state["streak"] = 0
            log_entry["type"] = "tier_3"
            
        state["today_status"] = "failed"
        log_entry["status"] = "failed"
        
    state["logs"].append(log_entry)
    save_state(state)
    return get_state()



# # Empathetic LLM Chat Routing
@app.post("/api/chat")
async def chat_endpoint(payload: ChatPayload):
    import re
    messages = payload.messages
    profile = payload.profile
    
    state = load_state()
    current_phase = state.get("current_phase", "empathy")
    
    last_user_message = messages[-1].content.lower() if messages else ""
    last_user_words = set(re.findall(r'\b\w+\b', last_user_message))
    
    # --- UPGRADE -0.5: VDOT UPLOAD COMPLETE INTERCEPT ---
    if "vdot upload complete" in last_user_message or "upload complete" in last_user_message or "ingestion complete" in last_user_message:
        motivation = payload.motivation or ""
        motivation_text = f" because of '{motivation}'" if motivation else ""
        congratulations = {
            "youth": f"Awesome job, champion! You successfully completed today's check-in and uploaded your VDOT video. Remember the reason why you are taking this medication{motivation_text}! Keep that streak alive!",
            "senior": f"Splendid work, Lola. You have successfully completed your daily medication check-in and video upload. Remember the reason why you are taking this medication{motivation_text}. Your health is so precious, dear.",
            "adult": f"Ingestion verification video uploaded successfully. Remember the reason why you are taking this medication{motivation_text}. Compliance log updated."
        }.get(profile, f"Medication video uploaded successfully. Remember the reason why you are taking this medication{motivation_text}.")
        
        response_content = f"{congratulations}\n\n<tool_call> {{\"name\": \"transition_to_success\"}} </tool_call>"
        return {
            "content": response_content,
            "status": "success",
            "current_phase": current_phase,
            "clinical_notes": state.get("clinical_notes", {})
        }

    # --- UPGRADE 0: CLINICAL CRISIS & SELF-HARM OVERRIDE ---
    crisis_keywords = ["kill myself", "harm myself", "hurt myself", "suicide", "end my life", "want to die", "hopeless", "give up", "cut myself", "self-harm", "wanna die", "die today"]
    is_harmful = any(kw in last_user_message for kw in crisis_keywords)
    
    if is_harmful:
        if profile == "youth":
            assistant_text = (
                "Please know you are very important and you do not have to carry this heavy weight alone. "
                "I want you to stay safe. I have activated a direct override link to call your care team or a crisis helpline right now. "
                "Please click the red emergency button below to connect with professional help. We can always log your pill once you are safe and supported.\n\n"
                '<tool_call> {"name": "emergency_override", "arguments": {"reason": "Youth self-harm threat detected"}} </tool_call>'
            )
        elif profile == "senior":
            assistant_text = (
                "Oh, dear friend, it is very sad to hear you say such words. "
                "Your life is so precious and we care about you very deeply. Please let me connect you with your healthcare provider or a professional support helpline right now. "
                "You do not have to carry this heavy burden alone, dear friend. Please use the button below to reach out immediately.\n\n"
                '<tool_call> {"name": "emergency_override", "arguments": {"reason": "Senior self-harm threat detected"}} </tool_call>'
            )
        else: # adult
            assistant_text = (
                "I am extremely concerned about your safety and wellbeing. Please know that your life has immense value and professional clinical support is available. "
                "I have initialized an emergency care override protocol. Please utilize the button below to connect immediately with your healthcare provider or a crisis support coordinator.\n\n"
                '<tool_call> {"name": "emergency_override", "arguments": {"reason": "Adult clinical threat override"}} </tool_call>'
            )
        return {
            "content": assistant_text,
            "status": "success",
            "current_phase": current_phase,
            "clinical_notes": state.get("clinical_notes", {})
        }
        
    # --- UPGRADE 0.5: DAILY INGESTION COMPLETE STANDBY ---
    today_status = state.get("today_status", "pending")
    if today_status in ["success_quota", "success_grace"]:
        if profile == "youth":
            assistant_text = (
                "You are very welcome. You have already completed today's VDOT check-in and locked in your streak. "
                "Have a nice day, my friend. I will stand by and we will check in again tomorrow."
            )
        elif profile == "senior":
            assistant_text = (
                "You are so very welcome. You have already completed your medicine check-in for today, "
                "and your record is safe. Please rest well and take care, dear friend. I will be here for you tomorrow."
            )
        else: # adult
            assistant_text = (
                "You are welcome. Your daily VDOT medication compliance log has already been successfully recorded at 100%. "
                "No further action is required today. I am standing by and will initialize your next session tomorrow."
            )
        return {
            "content": assistant_text,
            "status": "success",
            "current_phase": current_phase,
            "clinical_notes": state.get("clinical_notes", {})
        }
        
    # --- UPGRADE 1: SLIDING WINDOW CONTEXT ---
    MAX_HISTORY = 6
    recent_messages = messages[-MAX_HISTORY:]
    
    # --- UPGRADE 2: DYNAMIC PHASE SYSTEM PROMPT (STRICT STEERING, NO OPEN-ENDED QUESTIONS) ---
    if current_phase == "empathy":
        phase_instructions = (
            "We are currently in Phase 1: Empathetic Check-up.\n"
            "Your goal is to greet the patient warmly, show genuine clinical empathy, and immediately steer them towards their health check-in.\n"
            "CRITICAL: Do NOT ask open-ended questions like 'How can I assist you in feeling better today?', and do NOT ask how they are feeling right now.\n"
            "Instead, steer them directly to log their symptoms. You MUST append a structured tool call strictly in this format at the very end of your response to transition:\n"
            "<tool_call> {\"name\": \"show_symptom_checklist\"} </tool_call>\n"
            "EXAMPLE RESPONSE:\n"
            "I am so glad to hear that you are doing well today. Let us check in on your physical symptoms right now to keep your treatment safe.\n"
            "<tool_call> {\"name\": \"show_symptom_checklist\"} </tool_call>"
        )
    elif current_phase == "symptoms":
        phase_instructions = (
            "We are currently in Phase 2: Symptom Logging.\n"
            "Empathetically greet them and ask if they are experiencing any medication side effects "
            "(such as nausea, vomiting, joint pain, fatigue, or dark urine).\n"
            "CRITICAL: Do NOT ask open-ended questions. Steer the patient directly to choose their side-effects so we can progress to their daily VDOT filming.\n"
            "Once they respond with their physical status, you MUST generate a structured tool call strictly in this format at the end of your response to transition:\n"
            "<tool_call> {\"name\": \"transition_to_vdot\", \"arguments\": {\"side_effects\": \"nausea and fatigue\", \"nausea_severity\": \"Mild\"}} </tool_call>\n"
            "Valid nausea_severity values: 'None', 'Mild', 'Severe'."
        )
    else: # vdot
        phase_instructions = (
            "We are currently in Phase 3: Secure VDOT Filming.\n"
            "Warmly guide the patient to record their daily TB medication intake. Keep instructions brief, motivational, and highly focused.\n"
            "CRITICAL: Do NOT ask open-ended questions. Steer the patient directly to activate their camera stream and complete their ingestion.\n"
            "When they indicate readiness or when you prompt them, you MUST output a structured tool call strictly in this format:\n"
            "<tool_call> {\"name\": \"trigger_vdot\", \"arguments\": {\"duration_seconds\": 15}} </tool_call>"
        )

    # Route to Modal vLLM deployment
    if not settings.MODAL_API_URL:
        raise HTTPException(
            status_code=503,
            detail="Modal API URL is not configured. Please ensure MODAL_API_URL is set in settings."
        )

    try:
        async with httpx.AsyncClient(timeout=120.0) as client: # 120s timeout to accommodate remote GPU cold starts
            headers = {"Content-Type": "application/json"}
            openai_payload = {
                "model": settings.SERVED_MODEL_NAME,
                "messages": [{"role": m.role, "content": m.content} for m in messages],
                "temperature": 0.0,
                "max_tokens": 512
            }
            
            system_prompt = (
                f"You are 'Gabby', an AI conversational health companion designed to motivate TB patients.\n"
                f"Tailor your vocabulary, level of gamification, and empathy to the active profile: {profile}.\n"
                f"Active Phase Instructions:\n{phase_instructions}\n"
                f"CRITICAL STYLE RULES:\n"
                f"- STRICTLY FORBIDDEN TO USE EMOJIS: Do NOT use any emojis, stars (e.g. 🌟), icons, smiley faces, emoticons, or decorative symbols under any circumstances. All your replies must contain plain text only. If you use an emoji, you fail.\n"
                f"- USE MINIMAL LANGUAGE: Be extremely concise, direct, and brief. Use minimal sentences. Avoid extra explanations or chatty filler text.\n"
                f"- PROFESSIONAL CHILD-LIKE MANNERISM: Maintain a professional, clinically supportive, and safe tone, but express it with innocent, simple, child-like mannerisms (using simple words, gentle vocabulary, straightforward instructions, and honest guidance)."
            )
            
            openai_payload["messages"].insert(0, {"role": "system", "content": system_prompt})
            
            response = await client.post(
                f"{settings.MODAL_API_URL}/v1/chat/completions",
                json=openai_payload,
                headers=headers
            )
            
            if response.status_code == 200:
                result = response.json()
                assistant_text = result["choices"][0]["message"]["content"]
                
                # Clean thinking tags (e.g. <think>...</think>) from remote LLM responses if any leak out
                assistant_text = re.sub(r"<think>[\s\S]*?<\/think>", "", assistant_text)
                assistant_text = assistant_text.replace("</think>", "").replace("<think>", "").strip()
                
                # --- PROGRAMMATIC FAIL-SAFE FALLBACK INTERCEPT ---
                # If the remote LLM ignored our system instructions and failed to output a tool call,
                # we programmatically append the correct tool call based on current_phase & user input!
                if "<tool_call>" not in assistant_text:
                    if current_phase == "empathy":
                        # Only programmatically transition to symptoms if the user actually reported their mood
                        # (i.e. their last message is not just a greeting or start message)
                        is_greeting = any(w in last_user_words for w in ["hello", "hi", "hey", "gabby", "yo", "sup", "morning", "afternoon", "evening", "greetings"]) or last_user_message.strip() in ["", "?", "hi!", "hello!"]
                        if not is_greeting:
                            # Append the tool call and a steering symptom prompt checklist
                            transition_question = {
                                "youth": "\nLet us check your body today. Please fill out the symptom checklist below.",
                                "senior": "\nLet us review your body today, dear friend. Please check any symptoms you are feeling in the checklist card below.",
                                "adult": "\nLet us now document your physical symptoms. Please fill out the interactive symptom checklist below to log your status."
                            }[profile]
                            assistant_text += f"\n{transition_question}\n\n<tool_call> {{\"name\": \"show_symptom_checklist\"}} </tool_call>"
                    
                    elif current_phase == "symptoms":
                        is_submission = "submitted" in last_user_message or "reported" in last_user_message or "side effect" in last_user_message or any(w in last_user_message for w in ["nausea", "fatigue", "joint", "urine", "vomit", "none", "no"])
                        if is_submission:
                            severity = "Mild"
                            if any(w in last_user_message for w in ["severe", "very", "intense", "heavy", "bad", "high"]):
                                severity = "Severe"
                            elif any(w in last_user_message for w in ["no", "none", "fine", "okay", "good", "nothing", "zero"]):
                                severity = "None"
                            
                            side_effects = "nausea" if "nausea" in last_user_message or "sick" in last_user_message else "fatigue" if "tired" in last_user_message else "none"
                            if severity == "None":
                                side_effects = "none"
                                
                            # Append the transition tool call with VDOT confirmation check
                            transition_vdot = {
                                "youth": "\nYour symptoms are logged. Please drink some water. Are you ready to start your secure VDOT check-in now?",
                                "senior": "\nThank you for telling me. Please rest well. Are you ready to start the camera for your VDOT dose ingestion, dear friend?",
                                "adult": "\nUnderstood. Telemetry logs updated. Please confirm when you are ready to begin the secure VDOT ingestion filming session."
                            }[profile]
                            assistant_text += f"\n{transition_vdot}\n\n<tool_call> {{\"name\": \"transition_to_vdot\", \"arguments\": {{\"side_effects\": \"{side_effects}\", \"nausea_severity\": \"{severity}\"}}}} </tool_call>"
                        else:
                            transition_question = {
                                "youth": "\nLet us check your body today. Please fill out the symptom checklist below.",
                                "senior": "\nLet us review your body today, dear friend. Please check any symptoms you are feeling in the card below.",
                                "adult": "\nLet us document your physical symptoms. Please fill out the interactive symptom checklist below to log your status."
                            }[profile]
                            assistant_text += f"\n{transition_question}\n\n<tool_call> {{\"name\": \"show_symptom_checklist\"}} </tool_call>"
                    
                    elif current_phase == "vdot":
                        is_confirmed = any(w in last_user_message for w in ["yes", "start", "ready", "confirm", "ok", "sure", "now", "begin", "go", "video", "camera", "ingest", "button", "pill"]) and not any(w in last_user_message for w in ["not yet", "no", "wait", "hold", "stop", "cancel", "later"])
                        duration = 20 if profile == "senior" else 15
                        if is_confirmed:
                            transition_text = {
                                "youth": "\nOpening the secure VDOT camera now. Please keep your face and the pill in the frame.",
                                "senior": "\nActivating the camera now, dear friend. Please take your time.",
                                "adult": "\nActivating the secure VDOT filming session now. Please position the camera so your swallow is clearly visible."
                            }[profile]
                            assistant_text += f"\n{transition_text}\n\n<tool_call> {{\"name\": \"trigger_vdot\", \"arguments\": {{\"duration_seconds\": {duration}}}}} </tool_call>"
                        else:
                            transition_text = {
                                "youth": "\nNo worries. Please tell me when you are ready to start the camera.",
                                "senior": "\nPlease take your time, dear friend. Tell me when you are ready to start.",
                                "adult": "\nUnderstood. Standing by for ingestion confirmation. Please click the button to start the VDOT camera when ready."
                            }[profile]
                            assistant_text += f"\n{transition_text}"
                
                # Intercept and process any tool call generated (remotely or programmatically!)
                tool_call_match = re.search(r"<tool_call>([\s\S]*?)<\/tool_call>", assistant_text)
                if tool_call_match:
                    try:
                        tool_data = json.loads(tool_call_match.group(1).strip())
                        tool_name = tool_data.get("name")
                        arguments = tool_data.get("arguments", {})
                        
                        # Clean pure state transitions so they render beautifully or translate to checklist triggers
                        if tool_name == "transition_to_symptoms":
                            # Translate to show_symptom_checklist to trigger the checklist card!
                            tool_data["name"] = "show_symptom_checklist"
                            clean_text = assistant_text.replace(tool_call_match.group(0), "").strip()
                            if not clean_text:
                                clean_text = {
                                    "youth": "Let us check your body today. Please fill out the symptom checklist below.",
                                    "senior": "Let us review your body today, dear friend. Please check any symptoms you are feeling in the checklist card below.",
                                    "adult": "Let us now document your physical symptoms. Please fill out the interactive symptom checklist below to log your status."
                                }[profile]
                            assistant_text = f"{clean_text}\n\n<tool_call> {json.dumps(tool_data)} </tool_call>"
                            
                            state["current_phase"] = "symptoms"
                            if "arguments" in tool_data:
                                tool_data["arguments"].pop("mood", None)
                            save_state(state)
                            
                        elif tool_name == "show_symptom_checklist" or tool_name == "show_doctor_contact":
                            state["current_phase"] = "symptoms"
                            save_state(state)
                            
                        elif tool_name == "transition_to_vdot":
                            clean_text = assistant_text.replace(tool_call_match.group(0), "").strip()
                            if not clean_text:
                                clean_text = {
                                    "youth": "Your symptoms are logged. Please drink some water. Are you ready to start your secure VDOT check-in now?",
                                    "senior": "Thank you for telling me. Please rest well. Are you ready to start the camera for your VDOT dose ingestion, dear friend?",
                                    "adult": "Understood. Telemetry logs updated. Please confirm when you are ready to begin the secure VDOT ingestion filming session."
                                }[profile]
                            assistant_text = f"{clean_text}\n\n<tool_call> {json.dumps(tool_data)} </tool_call>"
                            
                            state["current_phase"] = "vdot"
                            state["clinical_notes"]["side_effects"] = arguments.get("side_effects", "None")
                            state["clinical_notes"]["nausea_severity"] = arguments.get("nausea_severity", "None")
                            save_state(state)
                    except Exception as e:
                        print(f"Error parsing remote tool call: {e}")
                        
                return {
                    "content": assistant_text,
                    "status": "success",
                    "current_phase": state.get("current_phase", "empathy"),
                    "clinical_notes": state.get("clinical_notes", {})
                }
            else:
                raise HTTPException(
                    status_code=502,
                    detail=f"Modal proxy returned status code {response.status_code}: {response.text}"
                )
    except Exception as e:
        if isinstance(e, HTTPException):
            raise e
        raise HTTPException(
            status_code=503,
            detail=f"Failed to communicate with Modal AI backend. Please verify your internet connection or cloud server status. Details: {str(e)}"
        )

# Mount static frontend files
app.mount("/", StaticFiles(directory="frontend", html=True), name="frontend")

if __name__ == "__main__":
    import uvicorn
    print(f"Starting Amping local development server on http://{settings.HOST}:{settings.PORT}...")
    uvicorn.run("backend.main:app", host=settings.HOST, port=settings.PORT, reload=True)
