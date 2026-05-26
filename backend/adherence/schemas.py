

from Amping.schemas import ApiSchema
from datetime import date


class Web_PDCTrendResponse(ApiSchema):
    class Web_WeeklyPDCEntry(ApiSchema):
        week: int
        pdc: float

    weekly_pdc: list[Web_WeeklyPDCEntry]
    pdc_target: float


class Web_AnomalousEntriesResponse(ApiSchema):
    class Web_AnomalousEntry(ApiSchema):
        date: date
        reason: str

    entries: list[Web_AnomalousEntry]


class Web_AdherenceMonthRequest(ApiSchema):
    month: int
    year: int

class Web_AdherenceMonthResponse(ApiSchema):
    class Web_AdherenceDayEntry(ApiSchema):
        date: date
        adherence_type: str
        symptoms: list[str]
        video_link: str | None

    month: int
    year: int
    adherence_days: list[Web_AdherenceDayEntry]

__ALL__ = [
    "Web_PDCTrendResponse",
    "Web_AnomalousEntriesResponse",
    "Web_AdherenceMonthRequest",
    "Web_AdherenceMonthResponse",
]