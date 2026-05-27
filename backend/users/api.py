from datetime import date

from ninja.errors import HttpError
from django.contrib.auth import authenticate, login, logout
from django.http import HttpRequest
from django.shortcuts import get_object_or_404
from django.db import IntegrityError, transaction
from Amping.utils import create_routers
from adherence.models import AdherenceDayRecord, AdherenceStatusEnum, SymptomRecord
from gamification.models import PatientStats
from .utils import get_random_string, getPatientUserByToken
from .schemas import Mobile_HealthCareProviderProfileResponse, Mobile_PatientProfileResponse, Mobile_RefreshTokenResponse, Web_CreatePatientPayload, Web_CreatePatientResponse, Web_GetAllPatientsResponse, Web_LoginHealthProviderPayload, Mobile_RefreshTokenPayload, Web_LoginHealthProviderResponse, Web_LogoutResponse, Web_PatientDetailResponse, Web_PatientGuardianEntry
from .models import HealthCareProviderUser, PatientGuardian, PatientUser, Token
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



@web_v1_router.get("/patient/", response=Web_GetAllPatientsResponse)
def get_all_patients(request: HttpRequest):
    patients = PatientUser.objects.filter(healthcare_provider=request.user)
    patient_entries = [
        Web_GetAllPatientsResponse.Web_PatientEntry(
            id=patient.id,
            firstname=patient.firstname,
            lastname=patient.lastname,
            email=patient.email,
            contact=patient.contact,
            birthyear=patient.birthyear
        )
        for patient in patients
    ]
    return Web_GetAllPatientsResponse(patients=patient_entries)

@web_v1_router.post("/patient/", response=Web_CreatePatientResponse)
def create_patient(request: HttpRequest, data: Web_CreatePatientPayload):
    patient_user = PatientUser(
        firstname=data.firstname,
        lastname=data.lastname,
        email=data.email,
        contact=data.contact,
        birthyear=data.birthyear,
        healthcare_provider=request.user,
        refresh_token=get_random_string(255)
    )

    try:
        with transaction.atomic():
            while True:
                try:
                    patient_user.save()
                    break
                except IntegrityError:
                    logger.error("Failed to create patient user with email %s, retrying...", data.email)
                    patient_user.refresh_token = get_random_string(255)

            for guardian_data in data.guardians:
                PatientGuardian(
                    patient=patient_user,
                    firstname=guardian_data.firstname,
                    lastname=guardian_data.lastname,
                    email=guardian_data.email,
                    contact=guardian_data.contact,
                ).save()

            PatientStats(
                patient=patient_user,
                total_regimen_days=data.total_days,
                current_streak=0,
                best_streak=0,
                heart_quota=3,
                regimen_start_date=data.regimen_start
            ).save()
    except IntegrityError as e:
        logger.error("Failed to create patient user: %s", str(e))
        raise HttpError(500, "Failed to create patient user")

    return Web_CreatePatientResponse(id=patient_user.id)

@web_v1_router.get("/patient/{patient_id}/", response=Web_PatientDetailResponse)
def get_patient_detail(request: HttpRequest, patient_id: int):
    patient_user = get_object_or_404(PatientUser, id=patient_id, healthcare_provider=request.user)
    patient_stats = get_object_or_404(PatientStats, patient=patient_user)
    guardians = [
        Web_PatientGuardianEntry(
            id=guardian.id,
            firstname=guardian.firstname,
            lastname=guardian.lastname,
            email=guardian.email,
            contact=guardian.contact
        )
        for guardian in PatientGuardian.objects.filter(patient=patient_user)
    ]
    
    return Web_PatientDetailResponse(
        id=patient_user.id,
        firstname=patient_user.firstname,
        lastname=patient_user.lastname,
        email=patient_user.email,
        contact=patient_user.contact,
        birthyear=patient_user.birthyear,
        guardians=guardians,

        regimen_start=patient_stats.regimen_start_date,
        current_day=patient_stats.current_day,
        total_days=patient_stats.total_regimen_days,

        month_pdc=0.5,  # TODO: Calculate real PDC for the month
        pdc_target=0.8,
        month3_protected=patient_stats.month3_protected
    )



@mobile_v1_router.get("/profile/", response=Mobile_PatientProfileResponse)
def get_patient_profile(request: HttpRequest):
    patient_user = getPatientUserByToken(request)
    patient_stats = get_object_or_404(PatientStats, patient=patient_user)

    return Mobile_PatientProfileResponse(
        id=patient_user.id,
        firstname=patient_user.firstname,
        lastname=patient_user.lastname,
        email=patient_user.email,
        contact=patient_user.contact,
        birthyear=patient_user.birthyear,

        regimen_start=patient_stats.regimen_start_date,
        current_day=patient_stats.current_day,
        total_days=patient_stats.total_regimen_days
    )

@mobile_v1_router.get("/healthcare-profile/", response=Mobile_HealthCareProviderProfileResponse)
def get_healthcare_profile(request: HttpRequest):
    patient_user = getPatientUserByToken(request)
    healthcare_provider = patient_user.healthcare_provider

    return Mobile_HealthCareProviderProfileResponse(
        id=healthcare_provider.id,
        firstname=healthcare_provider.firstname,
        lastname=healthcare_provider.lastname,
        email=healthcare_provider.email,
        contact=healthcare_provider.contact,
        clinic=healthcare_provider.clinic
    )