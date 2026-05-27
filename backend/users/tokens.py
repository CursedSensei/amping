import jwt
from datetime import datetime, timedelta, timezone
from django.conf import settings
import os

def generate_modal_access_token(patient_user) -> str:
    """
    Generates a short-lived signed JWT session token for the PatientUser client
    to communicate directly with the serverless Modal GPU backend.
    """
    # Load shared secret key from environment or fallback to Django Secret Key
    shared_secret = os.getenv(
        "JWT_SHARED_SECRET", 
        "1a880ab5ad18e1cf525ba10f88e6627e86d40ddee21224b01870850073da22e5"
    )
    
    # Translate database age_group ('child', 'adult', 'senior') to AI profiles ('youth', 'adult', 'senior')
    age_group = getattr(patient_user, "age_group", "adult")
    profile = "youth" if age_group == "child" else age_group
    
    # Payload claims representing patient demographic identity & streak progress
    payload = {
        "sub": str(patient_user.id),
        "username": f"{patient_user.firstname} {patient_user.lastname}".strip(),
        "profile": profile,
        "current_phase": "empathy", # Always start check-in at Empathy Phase
        "streak": getattr(patient_user, "current_streak", 5), # Fallback to default streak
        "exp": datetime.now(timezone.utc) + timedelta(minutes=15),
        "iat": datetime.now(timezone.utc),
        "iss": "https://amping-backend.com"
    }
    
    # Signed with HMAC-SHA256
    token = jwt.encode(payload, shared_secret, algorithm="HS256")
    return token
