from ninja.errors import HttpError
from django.contrib.auth import authenticate, login, logout
from django.http import HttpRequest
from Amping.utils import create_routers
from users.schemas import Mobile_RefreshTokenResponse, Web_LoginHealthProviderPayload, Mobile_RefreshTokenPayload, Web_LoginHealthProviderResponse, Web_LogoutResponse

mobile_v1_router, web_v1_router = create_routers()


# PATIENT AUTHENTICATION

@mobile_v1_router.post("/refresh-token/", response=Mobile_RefreshTokenResponse)
def patient_refresh_token(request: HttpRequest, data: Mobile_RefreshTokenPayload):
    # implement token refresh logic here, e.g. verify refresh token, issue new access token

    return Mobile_RefreshTokenResponse(message="Token refreshed successfully")


# HEALTHCARE AUTHENTICATION

@web_v1_router.post("/login/", auth=None, response=Web_LoginHealthProviderResponse)
def healthcare_login(request: HttpRequest, data: Web_LoginHealthProviderPayload):
    if request.user.is_authenticated:
        raise HttpError(400, "Already logged in")

    user = authenticate(request, email=data.email, password=data.password)
    if user is None:
        raise HttpError(401, "Invalid credentials")

    login(request, user)
    return Web_LoginHealthProviderResponse(message="Login successful")

@web_v1_router.post("/logout/", response=Web_LogoutResponse)
def healthcare_logout(request: HttpRequest):
    if request.user.is_authenticated:
        logout(request)

    return Web_LogoutResponse(message="Logged out successfully")