from django.db import models
from django.contrib.auth.models import AbstractUser

class AgeGroup(models.TextChoices):
    CHILD = 'child', 'Child'
    ADULT = 'adult', 'Adult'
    SENIOR = 'senior', 'Senior'



class HealthCareProviderUser(AbstractUser):
    name = models.CharField(max_length=255)
    contact = models.CharField(max_length=30)
    clinic_name = models.CharField(max_length=255, default="")

class PatientUser(models.Model):
    id = models.AutoField(primary_key=True)
    healthcare_provider = models.ForeignKey(HealthCareProviderUser, on_delete=models.CASCADE, related_name='patients')

    name = models.CharField(max_length=255)
    contact = models.CharField(max_length=30)
    email = models.EmailField()
    age = models.IntegerField()
    age_group = models.CharField(max_length=10, choices=AgeGroup.choices, default=AgeGroup.ADULT)

    refresh_token = models.CharField(max_length=255, unique=True)



class Token(models.Model):
    access_token = models.CharField(max_length=255, primary_key=True)
    patient = models.ForeignKey(PatientUser, on_delete=models.CASCADE, related_name='tokens')
    expires_at = models.DateTimeField()