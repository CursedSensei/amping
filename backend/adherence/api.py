from datetime import date, timedelta

from django.http import HttpRequest
from django.db import transaction
from django.shortcuts import get_object_or_404
from ninja import Query

from Amping.utils import create_routers
from users.utils import getPatientUserByToken
from gamification.models import PatientStats
from .models import AdherenceDayRecord, AdherenceStatusEnum, SymptomRecord
from .schemas import Mobile_GetAdherenceVideoEndpointResponse, Mobile_UploadSymtomsPayload, Mobile_UploadSymtomsResponse, Mobile_WeeklyAdherenceResponse, Web_AdherenceMonthRequest, Web_AdherenceMonthResponse, Web_AdherenceMonthResponse, Web_AnomalousEntriesResponse, Web_ReconcileAnomalyPayload, Web_ReconcileAnomalyResponse


mobile_v1_router, web_v1_router = create_routers()

@web_v1_router.get("/patient/{patient_id}/adherence_month/", response=Web_AdherenceMonthResponse)
def get_adherence_month(request: HttpRequest, patient_id: int, filter: Query[Web_AdherenceMonthRequest]):
    month = filter.month
    year = filter.year

    adherence_days = AdherenceDayRecord.objects.filter(patient_id=patient_id, date__year=year, date__month=month)

    response = Web_AdherenceMonthResponse(
        month=month,
        year=year,
        month_pdc=0.9, # TODO: Calculate real PDC for the month
        pdc_target=0.95,
        adherence_days=[
            Web_AdherenceMonthResponse.Web_AdherenceDayEntry(
                id=record.id,
                date=record.date,
                status=record.status,
                symptoms=record.symptoms,
                video_link=record.video_link
            )
            for record in adherence_days
        ]
    )

    return response

@web_v1_router.get("/patient/{patient_id}/anomalous_entries/", response=Web_AnomalousEntriesResponse)
def get_anomalous_entries(request: HttpRequest, patient_id: int):
    adherence_days = AdherenceDayRecord.objects.filter(patient_id=patient_id, status__in=[AdherenceStatusEnum.TECHNICAL_MISS, AdherenceStatusEnum.UNVERIFIED_ABSENCE])

    response = Web_AnomalousEntriesResponse(
        entries=[
            Web_AnomalousEntriesResponse.Web_AnomalousEntry(
                id=record.id,
                date=record.date,
                reason=record.get_status_display()
            )
            for record in adherence_days
        ]
    )
    return response

@web_v1_router.post("/patient/{patient_id}/reconcile_anomalies/", response=Web_ReconcileAnomalyResponse)
def reconcile_anomalies(request: HttpRequest, patient_id: int, payload: Web_ReconcileAnomalyPayload):
    reconciled_count = len(payload.entry_ids)

    with transaction.atomic():
        for entry_id in payload.entry_ids:
            try:
                record = AdherenceDayRecord.objects.get(id=entry_id, patient_id=patient_id)
                if record.status not in [AdherenceStatusEnum.TECHNICAL_MISS, AdherenceStatusEnum.UNVERIFIED_ABSENCE]:
                    reconciled_count -= 1
                    continue
                record.status = payload.verification_method
                record.reconciliation_note = payload.reason
                record.save()
            except AdherenceDayRecord.DoesNotExist:
                reconciled_count -= 1

    #TODO: Recalculate streaks, heart quota, and PDC for the patient after reconciliation
    patient_stats = get_object_or_404(PatientStats, patient_id=patient_id)

    response = Web_ReconcileAnomalyResponse(
        reconciled_count=reconciled_count,
        updated_streak=patient_stats.streak,
        updated_heart_quota=patient_stats.heart_quota,
        updated_pdc=patient_stats.pdc
    )

    return response



@mobile_v1_router.get("/weekly_adherence/", response=Mobile_WeeklyAdherenceResponse)
def get_weekly_adherence(request: HttpRequest):
    patient = getPatientUserByToken(request)
    adherence_days = AdherenceDayRecord.objects.filter(patient=patient, date__gte=date.today()-timedelta(days=7)).order_by('date')
    response = Mobile_WeeklyAdherenceResponse(
        adherence_days=[
            Mobile_WeeklyAdherenceResponse.Mobile_AdherenceDayEntry(
                date=record.date,
                status=record.status
            )
            for record in adherence_days
        ]
    )
    return response

@mobile_v1_router.post("/upload_symptoms/", response=Mobile_UploadSymtomsResponse)
def upload_symptoms(request: HttpRequest, payload: Mobile_UploadSymtomsPayload):
    patient = getPatientUserByToken(request)
    adherence_record = None

    with transaction.atomic():
        adherence_record = AdherenceDayRecord.objects.filter(patient=patient, date=date.today()).first()
        if not adherence_record:
            adherence_record = AdherenceDayRecord.objects.create(patient=patient, date=date.today())

        for symptom in payload.symptoms:
            SymptomRecord.objects.create(adherence_record=adherence_record, symptom=symptom)
    return Mobile_UploadSymtomsResponse(message="Symptoms uploaded successfully")

@mobile_v1_router.get("/adherence_video_endpoint/", response=Mobile_GetAdherenceVideoEndpointResponse)
def get_adherence_video_endpoint(request: HttpRequest):
    return Mobile_GetAdherenceVideoEndpointResponse(video_upload_url="https://example.com/upload_endpoint")