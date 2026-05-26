from datetime import date

from django.db import models
from django.contrib.auth.models import AbstractBaseUser, AbstractUser

from .managers import HealthCareProviderUserManager

class AgeGroup(models.TextChoices):
    CHILD = 'child', 'Child'
    ADULT = 'adult', 'Adult'
    SENIOR = 'senior', 'Senior'


class BaseUser(models.Model):
    id = models.AutoField(primary_key=True)
    firstname = models.CharField(max_length=255)
    lastname = models.CharField(max_length=100)
    contact = models.CharField(max_length=30)



class HealthCareProviderUser(BaseUser, AbstractUser):
    username = None
    email = models.EmailField(unique=True)

    objects = HealthCareProviderUserManager()

    USERNAME_FIELD = 'email'
    REQUIRED_FIELDS = []

class PatientUser(BaseUser):
    healthcare_provider = models.ForeignKey(HealthCareProviderUser, on_delete=models.CASCADE, related_name='patients')

    birthdate = models.DateField()
    age = date.today().year - birthdate.year - ((date.today().month, date.today().day) < (birthdate.month, birthdate.day))
    age_group = models.CharField(max_length=10, choices=AgeGroup.choices, default=AgeGroup.ADULT)

    refresh_token = models.CharField(max_length=255, unique=True)



class PatientGuardian(BaseUser):
    patient = models.ForeignKey(PatientUser, on_delete=models.CASCADE, related_name='guardians')


class Token(models.Model):
    access_token = models.CharField(max_length=255, primary_key=True)
    patient = models.ForeignKey(PatientUser, on_delete=models.CASCADE, related_name='tokens')
    expires_at = models.DateTimeField()