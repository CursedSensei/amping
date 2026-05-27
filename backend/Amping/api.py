from ninja import NinjaAPI
from users.api import mobile_v1_router as users_mobile_v1_router, web_v1_router as users_web_v1_router
from gamification.api import web_v1_router as gamification_web_v1_router, mobile_v1_router as gamification_mobile_v1_router
from adherence.api import web_v1_router as adherence_web_v1_router, mobile_v1_router as adherence_mobile_v1_router


api_v1 = NinjaAPI()

api_v1.add_router("/mobile/", users_mobile_v1_router)
api_v1.add_router("/mobile/", gamification_mobile_v1_router)
api_v1.add_router("/mobile/", adherence_mobile_v1_router)
api_v1.add_router("/web/", users_web_v1_router)
api_v1.add_router("/web/", gamification_web_v1_router)
api_v1.add_router("/web/", adherence_web_v1_router)