from ninja import NinjaAPI
from users.api import healthcare_router, patient_router, auth_router

api_v1 = NinjaAPI()

api_v1.add_router("/healthcare/", healthcare_router)
api_v1.add_router("/patients/", patient_router)
api_v1.add_router("/auth/", auth_router)