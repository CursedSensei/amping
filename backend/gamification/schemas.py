from Amping.schemas import ApiSchema
from datetime import date

from .models import PenaltyTierEnum


class Web_GamificationResponse(ApiSchema):
    class Web_PenaltyEvent(ApiSchema):
        date: date
        tier: PenaltyTierEnum
        label: str

    total_regimen_days: int
    current_streak: int
    best_streak: int
    heart_quota: int
    penalty_history: list[Web_PenaltyEvent]

class Mobile_StatsResponse(ApiSchema):
    class Mobile_PenaltyEvent(ApiSchema):
        date: date
        tier: PenaltyTierEnum
        label: str

    total_regimen_days: int
    current_streak: int
    best_streak: int
    heart_quota: int
    penalty_history: list[Mobile_PenaltyEvent]

__ALL__ = [
    "Web_GamificationResponse",
    "Mobile_StatsResponse"
]