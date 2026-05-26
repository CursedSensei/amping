from ninja import NinjaAPI
from users.api import mobile_v1_router as users_mobile_v1_router, web_v1_router as users_web_v1_router


api_v1 = NinjaAPI()

api_v1.add_router("/mobile/", users_mobile_v1_router)
api_v1.add_router("/web/", users_web_v1_router)