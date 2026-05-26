from Amping.schemas import ApiSchema
from datetime import date

class Web_CreatePatientPayload(ApiSchema):
    name: str
    email: str
    contact: str
    age: int

class Web_CreatePatientResponse(ApiSchema):
    message: str

class Web_LoginHealthProviderPayload(ApiSchema):
    email: str
    password: str

class Web_LogoutResponse(ApiSchema):
    message: str

class Web_LoginHealthProviderResponse(ApiSchema):
    message: str

class Mobile_RefreshTokenPayload(ApiSchema):
    refresh_token: str

class Mobile_RefreshTokenResponse(ApiSchema):
    access_token: str



class Web_PatientDetailResponse(ApiSchema):
    class Web_PatientGuardianEntry(ApiSchema):
        id: int
        firstname: str
        lastname: str
        email: str
        contact: str

    id: int
    firstname: str
    lastname: str
    email: str
    contact: str
    birthyear: int
    guardians: list[Web_PatientGuardianEntry]

    regimen_start: date
    current_day: int
    total_days: int

    month_pdc: float
    pdc_target: float
    month3_protected: bool

class Web_HealthCareProviderDetailResponse(ApiSchema):
    id: int
    firstname: str
    lastname: str
    email: str
    contact: str
    clinic: str

__ALL__ = [
    "Web_CreatePatientPayload",
    "Web_CreatePatientResponse",
    "Web_LoginHealthProviderPayload",
    "Web_LoginHealthProviderResponse",
    "Web_LogoutResponse",
    "Mobile_RefreshTokenPayload",
    "Mobile_RefreshTokenResponse",
    "Web_PatientDetailResponse",
    "Web_HealthCareProviderDetailResponse",
]