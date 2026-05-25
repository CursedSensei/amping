from django.contrib import admin
from users.models import HealthCareProviderUser, PatientUser

admin.site.register(HealthCareProviderUser)
admin.site.register(PatientUser)