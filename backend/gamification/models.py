from django.db import models

class PenaltyTierEnum(models.IntegerChoices):
    TIER_1 = 1, 'Tier 1 — Soft Warning'
    TIER_2 = 2, 'Tier 2 — Partial Reset A'
    TIER_3 = 3, 'Tier 3 — Partial Reset B'
    TIER_4 = 4, 'Tier 4 — Full Reset + Escalation'

class PatientStats(models.Model):
    id = models.AutoField(primary_key=True)
    patient = models.OneToOneField('users.PatientUser', on_delete=models.CASCADE, related_name='stats')

    regimen_start_date = models.DateField()
    total_regimen_days = models.IntegerField(default=0)
    current_day = models.IntegerField(default=0)
    current_streak = models.IntegerField(default=0)
    best_streak = models.IntegerField(default=0)
    heart_quota = models.IntegerField(default=3)

    forgiveness_quota_used_this_month = models.IntegerField(default=3)
    forgiveness_quota_period_start = models.DateField(null=True, blank=True)

    month3_protected = models.BooleanField(default=False)

class PenaltyEvent(models.Model):
    id = models.AutoField(primary_key=True)
    patient_stats = models.ForeignKey(PatientStats, on_delete=models.CASCADE, related_name='penalty_events')
    adherence_record = models.ForeignKey('adherence.AdherenceDayRecord', on_delete=models.CASCADE, related_name='penalty_event')

    date = models.DateField()
    tier = models.IntegerField(choices=PenaltyTierEnum.choices)
    
    penalty_given = models.IntegerField(default=0)
    reverted = models.BooleanField(default=False)