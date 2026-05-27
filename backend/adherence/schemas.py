

from Amping.schemas import ApiSchema
from datetime import date

from .models import AdherenceStatusEnum


class Web_PDCTrendResponse(ApiSchema):
    class Web_WeeklyPDCEntry(ApiSchema):
        week: int
        pdc: float

    weekly_pdc: list[Web_WeeklyPDCEntry]
    pdc_target: float


class Web_AnomalousEntriesResponse(ApiSchema):
    class Web_AnomalousEntry(ApiSchema):
        id: int
        date: date
        reason: str

    entries: list[Web_AnomalousEntry]


class Web_AdherenceMonthRequest(ApiSchema):
    month: int
    year: int

class Web_AdherenceMonthResponse(ApiSchema):
    class Web_AdherenceDayEntry(ApiSchema):
        id: int
        date: date
        status: AdherenceStatusEnum
        symptoms: list[str]
        video_link: str | None

    month: int
    year: int
    month_pdc: float
    pdc_target: float
    adherence_days: list[Web_AdherenceDayEntry]


class Web_ReconcileAnomalyPayload(ApiSchema):
    """
    Sent by a healthcare provider or BHW to reconcile one or more anomalous
    entries (e.g. mark a technical-miss as provider-verified).
    """
    entry_ids: list[int]
    verification_method: AdherenceStatusEnum
    reason: str

class Web_ReconcileAnomalyResponse(ApiSchema):
    """Returned after a successful reconciliation batch."""
    reconciled_count: int
    updated_streak: int
    updated_heart_quota: int
    updated_pdc: float


class Mobile_WeeklyAdherenceResponse(ApiSchema):
    class Mobile_AdherenceDayEntry(ApiSchema):
        date: date
        status: AdherenceStatusEnum

    week_start: date
    week_end: date
    adherence_days: list[Mobile_AdherenceDayEntry]

class Mobile_UploadSymtomsPayload(ApiSchema):
    date: date
    symptoms: list[str]

class Mobile_UploadSymtomsResponse(ApiSchema):
    message: str

class Mobile_GetAdherenceVideoEndpointResponse(ApiSchema):
    adherence_day_id: int
    video_endpoint: str

__ALL__ = [
    "Web_PDCTrendResponse",
    "Web_AnomalousEntriesResponse",
    "Web_AdherenceMonthRequest",
    "Web_AdherenceMonthResponse",
    "Web_ReconcileAnomalyPayload",
    "Web_ReconcileAnomalyResponse",
    "Mobile_WeeklyAdherenceResponse",
    "Mobile_UploadSymtomsResponse",
    "Mobile_GetAdherenceVideoEndpointResponse",
    "Mobile_UploadSymtomsPayload",
    "Mobile_UploadAdherenceVideoPayload"
]