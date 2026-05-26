from ninja import NinjaAPI, Router
from ninja.security import django_auth

mobile_v1_router = Router(csrf_exempt=True, auth=None)
web_v1_router = Router(auth=django_auth)


api_v1 = NinjaAPI()

api_v1.add_router("/mobile/", mobile_v1_router)
api_v1.add_router("/web/", web_v1_router)