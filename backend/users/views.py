from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_POST
from django.conf import settings
import json
import os
from users.models import PatientUser, HealthCareProviderUser, AgeGroup
from users.tokens import generate_modal_access_token

class MockPatient:
    """
    Mock patient object used as a resilient fallback if the database has no 
    records or ForeignKey dependencies are not satisfied yet.
    """
    def __init__(self, user_id):
        self.id = 999
        self.firstname = "Leo" if user_id == "youth" else "Lola" if user_id == "senior" else "Rafael"
        self.lastname = "Patient"
        self.age = 15 if user_id == "youth" else 75 if user_id == "senior" else 35
        self.age_group = "child" if user_id == "youth" else "senior" if user_id == "senior" else "adult"
        self.current_streak = 5

@csrf_exempt
@require_POST
def fetch_chat_session_token(request):
    """
    Direct endpoint mapping for the Android patient client (POST /api/chat/session).
    Issues a short-lived signed JWT token containing patient telemetry for Modal inference.
    """
    try:
        body = json.loads(request.body)
        user_id = body.get("userId", "adult").lower() # e.g. "youth", "senior", "adult"
        motivation = body.get("motivation", "")
    except Exception:
        user_id = "adult"
        motivation = ""

    patient = None
    
    # 1. Try to fetch a matching patient from the database
    try:
        db_age_group = "child" if user_id == "youth" else "senior" if user_id == "senior" else "adult"
        patient = PatientUser.objects.filter(age_group=db_age_group).first()
    except Exception:
        pass # Gracefully handle unapplied migrations or db connectivity issues

    # 2. Fallback to resilient mock object if database is empty
    if not patient:
        patient = MockPatient(user_id)

    # 3. Generate token
    token = generate_modal_access_token(patient, motivation)
    
    # 4. Read Modal URL from environment or configuration
    modal_url = os.getenv("MODAL_API_URL", "https://gabby-hermes-4-secure-serve.modal.run")
    # Clean protocol prefix (http/https/ws/wss) if client expects raw domain with port
    modal_url = modal_url.replace("https://", "").replace("http://", "").replace("wss://", "").replace("ws://", "")

    return JsonResponse({
        "token": token,
        "modalUrl": modal_url
    })
