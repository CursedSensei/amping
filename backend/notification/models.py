from django.db import models
# pyrefly: ignore [missing-import]
from user.models import PatientUser

# Create your models here.
class NotifyPatient(models.Model):
    id = models.AutoField(primary_key=True)
    title = models.CharField(max_length=50)
    message = models.TextField()
    patient = models.ForeignKey(PatientUser, on_delete=models.CASCADE)
    created_at = models.DateTimeField(auto_now_add=True)
    hasRead = models.BooleanField(default=False)
    isForGabby = models.BooleanField(default=True)

    