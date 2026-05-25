from ninja import Router
from ninja.security import django_auth
from ninja.errors import HttpError
from django.views.decorators.csrf import csrf_exempt
from Amping.schemas import BaseResponse
from users.schemas import LoginHealthProviderPayload, RefreshTokenPayload

healthcare_router = Router()
patient_router = Router()
auth_router = Router()




# PATIENT AUTHENTICATION

@auth_router.post("/refresh-token/", )
@csrf_exempt
def patient_refresh_token(request, data: RefreshTokenPayload):
    # implement token refresh logic here, e.g. verify refresh token, issue new access token

    return BaseResponse(message="Token refreshed successfully")


# HEALTHCARE AUTHENTICATION

@auth_router.post("/login/", auth=django_auth, response=BaseResponse)
def healthcare_login(request, data: LoginHealthProviderPayload):
    if request.user.is_authenticated:
        raise HttpError(400, "Already logged in")

    user = django_auth.authenticate(request, username=data.email, password=data.password)
    if user is None:
        raise HttpError(401, "Invalid credentials")

    return BaseResponse(message="Login successful")

@auth_router.post("/logout/", auth=django_auth, response=BaseResponse)
def healthcare_logout(request) -> BaseResponse:
    if not request.user.is_authenticated:
        raise HttpError(401, "Not authenticated")

    request.auth.delete()
    return BaseResponse(message="Logged out successfully")