from django.http import HttpRequest

from Amping.utils import create_routers
from .models import PenaltyTierEnum
from .schemas import Web_GamificationResponse


mobile_v1_router, web_v1_router = create_routers()

@web_v1_router.get("/patient/{patient_id}/gamification/", response=Web_GamificationResponse)
def get_gamification_status(request: HttpRequest, patient_id: int):
    # Placeholder implementation - replace with actual logic to compute gamification status
    response = Web_GamificationResponse(
        total_regimen_days=30,
        current_streak=5,
        best_streak=10,
        heart_quota=3,
        penalty_history=[
            Web_GamificationResponse.Web_PenaltyEvent(date="2024-01-15", tier=PenaltyTierEnum.TIER_1, label="Test dose"),
            Web_GamificationResponse.Web_PenaltyEvent(date="2024-01-20", tier=PenaltyTierEnum.TIER_2, label="Test dose"),
        ]
    )
    return response