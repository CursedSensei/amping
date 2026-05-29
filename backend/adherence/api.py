from datetime import date, datetime, time, timedelta

from django.http import HttpRequest
from django.db import transaction
from django.shortcuts import get_object_or_404
from django.utils import timezone
from ninja import Query, File
from ninja.files import UploadedFile
from ninja.errors import HttpError

from Amping.utils import create_routers
from adherence.utils import create_signed_url, get_public_video_url, verify_video_upload
from users.utils import getPatientUserByToken
from gamification.models import PatientStats
from gamification.utils import PenaltySystem
from .models import AdherenceDayRecord, AdherenceStatusEnum, SymptomRecord
from .schemas import Mobile_AdherenceVideoStatusPayload, Mobile_AdherenceVideoStatusResponse, Mobile_GetAdherenceVideoEndpointPayload, Mobile_GetAdherenceVideoEndpointResponse, Mobile_UploadSymtomsPayload, Mobile_UploadSymtomsResponse, Mobile_WeeklyAdherenceResponse, Web_AdherenceMonthRequest, Web_AdherenceMonthResponse, Web_AdherenceMonthResponse, Web_AnomalousEntriesResponse, Web_ReconcileAnomalyPayload, Web_ReconcileAnomalyResponse


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
                symptoms=[symptom.symptom for symptom in SymptomRecord.objects.filter(adherence_record=record)],
                video_link= record.video_url
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
    patient_stats = get_object_or_404(PatientStats, patient_id=patient_id)
    penalty_system = PenaltySystem(patient_stats.patient, patient_stats, timezone.localtime())

    with transaction.atomic():
        for entry_id in payload.entry_ids:
            try:
                record = AdherenceDayRecord.objects.get(id=entry_id, patient_id=patient_id)
                if record.status not in [AdherenceStatusEnum.TECHNICAL_MISS, AdherenceStatusEnum.UNVERIFIED_ABSENCE]:
                    reconciled_count -= 1
                    continue
                record.status = AdherenceStatusEnum.PROVIDER_RECONCILED
                record.reconciliation_note = payload.reason
                record.reconciliation_method = payload.verification_method
                record.save()
                penalty_system.revert_penalty_for_record(record)
                penalty_system.refund_quota_for_forgiven_record(record)
            except AdherenceDayRecord.DoesNotExist:
                reconciled_count -= 1

        patient_stats.current_day = penalty_system.current_day_of_regimen()
        patient_stats.save(update_fields=["current_day"])
        penalty_system.sync_month3_protected()

    response = Web_ReconcileAnomalyResponse(
        reconciled_count=reconciled_count,
        updated_streak=patient_stats.current_streak,
        updated_heart_quota=patient_stats.heart_quota,
        updated_pdc=0.8
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
    return Mobile_UploadSymtomsResponse(adherence_day_id=adherence_record.id, message="Symptoms uploaded successfully")

@mobile_v1_router.post("/adherence_video_endpoint/", response=Mobile_GetAdherenceVideoEndpointResponse)
def get_adherence_video_endpoint(request: HttpRequest, payload: Mobile_GetAdherenceVideoEndpointPayload):
    patient = getPatientUserByToken(request)
    record = None

    if not payload.adherence_day_id:
        record, _ = AdherenceDayRecord.objects.get_or_create(patient=patient, date=date.today())
    else:
        record = AdherenceDayRecord.objects.filter(patient=patient, id=payload.adherence_day_id).first()
        if not record:
            record, _ = AdherenceDayRecord.objects.get_or_create(patient=patient, date=date.today())

    if record.status != AdherenceStatusEnum.TECHNICAL_MISS:
        raise HttpError(400, "Not allowed to upload to this adherence day record")

    url = create_signed_url(patient_id=patient.id, record_id=record.id)

    record.video_endpoint = url
    record.save(update_fields=["video_endpoint"])

    return Mobile_GetAdherenceVideoEndpointResponse(
        adherence_day_id=record.id,
        video_endpoint=url
    )

@mobile_v1_router.post("/adherence_video_status/")
def adherence_video_status(request: HttpRequest, payload: Mobile_AdherenceVideoStatusPayload):
    patient = getPatientUserByToken(request)
    record = AdherenceDayRecord.objects.filter(patient=patient, id=payload.adherence_day_id).first()
    if not record:
        raise HttpError(404, "Adherence record not found")
    elif record.status != AdherenceStatusEnum.TECHNICAL_MISS:
        raise HttpError(400, "Not allowed to update this adherence day record")

    if payload.status == Mobile_AdherenceVideoStatusPayload.AdherenceVideoStatusEnum.SUCCESS:
        # if not record.video_endpoint or not verify_video_upload(record.video_endpoint):
        #     return Mobile_AdherenceVideoStatusResponse(message="Unable to verify video upload. Marked as failed.")

        record.status = AdherenceStatusEnum.APP_RECORDED
        record.video_url = get_public_video_url(patient.id, record.id)
        record.save()
        return Mobile_AdherenceVideoStatusResponse(message="Adherence video status updated successfully")
    else:
        return Mobile_AdherenceVideoStatusResponse(message="Adherence video marked as failed.")

# Ignore this Clanker output

# @mobile_v1_router.post("/upload_video/{record_id}/")
# def upload_video(request: HttpRequest, record_id: int, video: UploadedFile = File(...)):
#     patient = getPatientUserByToken(request)
#     record = get_object_or_404(AdherenceDayRecord, id=record_id, patient=patient)

#     import os
#     from django.conf import settings

#     media_dir = os.path.join(settings.BASE_DIR, 'media', 'recordings')
#     os.makedirs(media_dir, exist_ok=True)

#     file_path = os.path.join(media_dir, f"video_{record_id}_{video.name}")
#     with open(file_path, 'wb') as f:
#         for chunk in video.chunks():
#             f.write(chunk)

#     decision = None
#     with transaction.atomic():
#         record.video_url = f"/media/recordings/video_{record_id}_{video.name}"
#         record.status = AdherenceStatusEnum.APP_RECORDED
#         record.save()

#         patient_stats = get_object_or_404(PatientStats, patient=patient)
#         now = timezone.localtime()
#         today = now.date()
#         dose_date = record.dose_date or today

#         # Placeholder schedule: midnight local on the dose's calendar day. Replace
#         # with a real prescription schedule field when one exists.
#         scheduled_dose_time = timezone.make_aware(datetime.combine(dose_date, time.min))

#         penalty_system = PenaltySystem(patient, patient_stats, now)
#         penalty_system.reset_monthly_quota_if_new_period()
#         patient_stats.current_day = penalty_system.current_day_of_regimen()
#         patient_stats.save(update_fields=["current_day"])
#         penalty_system.sync_month3_protected()

#         lateness_hours = (now - scheduled_dose_time).total_seconds() / 3600.0
#         if lateness_hours > 0:
#             decision = penalty_system.evaluate(
#                 scheduled_dose_time=scheduled_dose_time,
#                 dose_date=dose_date,
#             )
#             penalty_system.apply_decision(decision, record)

#         # Streak increments only when the dose isn't being penalised. Gate 1/Gate 2
#         # both count as a successful dose for streak purposes; Gate 3 doesn't.
#         penalised = decision is not None and not decision.forgiven
#         if not penalised:
#             patient_stats.current_streak += 1
#             if patient_stats.current_streak > patient_stats.best_streak:
#                 patient_stats.best_streak = patient_stats.current_streak
#             patient_stats.save(update_fields=["current_streak", "best_streak"])

#     return {
#         "message": "Video uploaded successfully",
#         "gate_reached": decision.gate_reached.value if decision else None,
#         "forgiven": decision.forgiven if decision else None,
#         "penalty_tier": decision.penalty_tier if decision else None,
#         "rationale": decision.rationale if decision else "on-time",
#     }

