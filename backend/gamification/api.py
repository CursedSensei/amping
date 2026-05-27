from django.http import HttpRequest

from Amping.utils import create_routers
from users.utils import getPatientUserByToken
from .models import PatientStats, PenaltyEvent, PenaltyTierEnum
from .schemas import Mobile_StatsResponse, Web_GamificationResponse
from django.shortcuts import get_object_or_404
from .apps import logger


mobile_v1_router, web_v1_router = create_routers()

@web_v1_router.get("/patient/{patient_id}/gamification/", response=Web_GamificationResponse)
def get_gamification_status(request: HttpRequest, patient_id: int):
    patient_stats = get_object_or_404(PatientStats, patient_id=patient_id)
    penalty_events = PenaltyEvent.objects.filter(patient_stats=patient_stats).order_by('-date')

    response = Web_GamificationResponse(
        total_regimen_days=patient_stats.total_regimen_days,
        current_streak=patient_stats.current_streak,
        best_streak=patient_stats.best_streak,
        heart_quota=patient_stats.heart_quota,
        penalty_history=[
            Web_GamificationResponse.Web_PenaltyEvent(
                date=event.date.isoformat(),
                tier=event.tier,
                label=event.label
            )
            for event in penalty_events
        ]
    )

    return response

@mobile_v1_router.get("/stats/", response=Mobile_StatsResponse)
def get_gamification_status_mobile(request: HttpRequest):
    patient = getPatientUserByToken(request)

    patient_stats = get_object_or_404(PatientStats, patient=patient)
    penalty_events = PenaltyEvent.objects.filter(patient_stats=patient_stats).order_by('-date')

    response = Mobile_StatsResponse(
        total_regimen_days=patient_stats.total_regimen_days,
        current_streak=patient_stats.current_streak,
        best_streak=patient_stats.best_streak,
        heart_quota=patient_stats.heart_quota,
        penalty_history=[
            Mobile_StatsResponse.Mobile_PenaltyEvent(
                date=event.date.isoformat(),
                tier=event.tier,
                label=event.label
            )
            for event in penalty_events
        ]
    )

    return response