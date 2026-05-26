from Amping.schemas import ApiSchema
from datetime import date


class Web_GamificationResponse(ApiSchema):
    class Web_PenaltyEvent(ApiSchema):
        date: date
        tier: int
        label: str

    current_streak: int
    best_streak: int
    heart_quota: int
    penalty_history: list[Web_PenaltyEvent]

__ALL__ = [
    "Web_GamificationResponse",
]