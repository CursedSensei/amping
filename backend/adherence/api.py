from django.http import HttpRequest

from Amping.utils import create_routers
from .models import AdherenceStatusEnum
from .schemas import Web_AdherenceMonthResponse, Web_AdherenceMonthResponse, Web_AnomalousEntriesResponse, Web_ReconcileAnomalyPayload, Web_ReconcileAnomalyResponse


mobile_v1_router, web_v1_router = create_routers()

@web_v1_router.get("/patient/{patient_id}/adherence_month/", response=Web_AdherenceMonthResponse)
def get_adherence_month(request: HttpRequest, patient_id: int):
    # Placeholder implementation - replace with actual logic to fetch adherence data for the month
    response = Web_AdherenceMonthResponse(
        month=1,
        year=2024,
        month_pdc=0.9,
        pdc_target=0.95,
        adherence_days=[
            Web_AdherenceMonthResponse.Web_AdherenceDayEntry(
                id=1,
                date="2024-01-01",
                status=AdherenceStatusEnum.APP_RECORDED,
                symptoms=["cough", "fever"],
                video_link="http://example.com/video1"
            ),
            Web_AdherenceMonthResponse.Web_AdherenceDayEntry(
                id=2,
                date="2024-01-02",
                status=AdherenceStatusEnum.TECHNICAL_MISS,
                symptoms=[],
                video_link=None
            ),
        ]
    )
    return response

@web_v1_router.get("/patient/{patient_id}/anomalous_entries/", response=Web_AnomalousEntriesResponse)
def get_anomalous_entries(request: HttpRequest, patient_id: int):
    # Placeholder implementation - replace with actual logic to fetch anomalous entries
    response = Web_AnomalousEntriesResponse(
        entries=[
            Web_AnomalousEntriesResponse.Web_AnomalousEntry(
                id=1,
                date="2024-01-02",
                reason="Technical miss - no video uploaded"
            ),
            Web_AnomalousEntriesResponse.Web_AnomalousEntry(
                id=2,
                date="2024-01-10",
                reason="Unverified absence - patient did not report"
            ),
        ]
    )
    return response

@web_v1_router.post("/patient/{patient_id}/reconcile_anomalies/", response=Web_ReconcileAnomalyResponse)
def reconcile_anomalies(request: HttpRequest, patient_id: int, payload: Web_ReconcileAnomalyPayload):
    # Placeholder implementation - replace with actual logic to reconcile anomalies
    response = Web_ReconcileAnomalyResponse(
        reconciled_count=len(payload.entry_ids),
        updated_streak=6,
        updated_heart_quota=2,
        updated_pdc=0.92
    )
    return response