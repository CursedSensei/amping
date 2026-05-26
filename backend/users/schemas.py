from Amping.schemas import ApiSchema

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

__ALL__ = [
    "Web_CreatePatientPayload",
    "Web_CreatePatientResponse",
    "Web_LoginHealthProviderPayload",
    "Web_LoginHealthProviderResponse",
    "Web_LogoutResponse",
    "Mobile_RefreshTokenPayload",
    "Mobile_RefreshTokenResponse"
]