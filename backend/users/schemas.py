from Amping.schemas import ApiSchema, BaseResponse

class Auth_CreatePatientPayload(ApiSchema):
    name: str
    email: str
    contact: str
    age: int

class Auth_CreatePatientResponse(BaseResponse):
    pass

class Auth_LoginHealthProviderPayload(ApiSchema):
    email: str
    password: str

class Auth_RefreshTokenPayload(ApiSchema):
    refresh_token: str

__ALL__ = [
    "Auth_CreatePatientPayload",
    "Auth_CreatePatientResponse",
    "Auth_LoginHealthProviderPayload",
    "Auth_RefreshTokenPayload"
]