from ninja.errors import HttpError
from django.contrib.auth import authenticate, login, logout
from django.http import HttpRequest
from django.shortcuts import get_object_or_404
from django.db import IntegrityError, transaction
from Amping.utils import create_routers
from .utils import get_random_string
from .schemas import Mobile_RefreshTokenResponse, Web_LoginHealthProviderPayload, Mobile_RefreshTokenPayload, Web_LoginHealthProviderResponse, Web_LogoutResponse
from .models import PatientUser, Token
from .apps import logger

mobile_v1_router, web_v1_router = create_routers()


# PATIENT AUTHENTICATION

@mobile_v1_router.post("/refresh-token/", response=Mobile_RefreshTokenResponse)
def patient_refresh_token(request: HttpRequest, data: Mobile_RefreshTokenPayload):
    patient_user = get_object_or_404(PatientUser, refresh_token=data.refresh_token)
    access_token = Token(
        access_token=get_random_string(255),
        patient=patient_user,
        expires_at="2027-12-31T23:59:59Z"
    )

    # TODO: Remove old tokens for this patient to prevent token sprawl

    while True:
        try:
            with transaction.atomic():
                Token.save(access_token)
            break
        except IntegrityError:
            logger.error("Failed to create access token for patient %s", patient_user.id)
            access_token.access_token = get_random_string(255)

    return Mobile_RefreshTokenResponse(access_token=access_token.access_token)


# HEALTHCARE AUTHENTICATION

@web_v1_router.post("/login/", auth=None, response=Web_LoginHealthProviderResponse)
def healthcare_login(request: HttpRequest, data: Web_LoginHealthProviderPayload):
    if request.user.is_authenticated:
        raise HttpError(400, "Already logged in")

    user = authenticate(request, email=data.email, password=data.password)
    if user is None:
        raise HttpError(401, "Invalid credentials")

    login(request, user)
    return Web_LoginHealthProviderResponse(message="Login successful")

@web_v1_router.post("/logout/", response=Web_LogoutResponse)
def healthcare_logout(request: HttpRequest):
    if request.user.is_authenticated:
        logout(request)

    return Web_LogoutResponse(message="Logged out successfully")