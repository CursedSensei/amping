from django.db import models
from django.utils import timezone

class AdherenceStatusEnum(models.TextChoices):
    APP_RECORDED = 'app_recorded', 'App Recorded'
    PROVIDER_RECONCILED = 'provider_reconciled', 'Provider Reconciled'
    TECHNICAL_MISS = 'technical_miss', 'Technical Miss'
    UNVERIFIED_ABSENCE = 'unverified_absence', 'Unverified Absence'

class ReconciliationMethodEnum(models.TextChoices):
    HOME_VISIT = 'home_visit', 'Home Visit'
    DOT_ORDER = 'dot_order', 'DOT Order'
    SEND_MESSAGE = 'send_message', 'Send Message'

class AdherenceDayRecord(models.Model):
    id = models.AutoField(primary_key=True)
    patient = models.ForeignKey('users.PatientUser', on_delete=models.CASCADE, related_name='adherence_day_records')

    date = models.DateField(auto_now_add=True)
    dose_date = models.DateField(default=timezone.now)
    video_url = models.URLField(blank=True, null=True)
    status = models.CharField(max_length=50, blank=True, choices=AdherenceStatusEnum.choices, default=AdherenceStatusEnum.TECHNICAL_MISS)

    reconciliation_note = models.TextField(blank=True, null=True)
    reconciliation_method = models.CharField(max_length=50, blank=True, null=True, choices=ReconciliationMethodEnum.choices)

class SymptomRecord(models.Model):
    id = models.AutoField(primary_key=True)
    adherence_record = models.ForeignKey(AdherenceDayRecord, on_delete=models.CASCADE, related_name='symptom_records')
    
    symptom = models.CharField(max_length=255, blank=True)