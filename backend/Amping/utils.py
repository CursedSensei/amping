from ninja import Router
from ninja.security import django_auth

def create_routers():
    """
        Always return tuple(mobile router, web router).
    """

    mobile_router = Router(auth=None)
    web_router = Router(auth=django_auth)
    return mobile_router, web_router