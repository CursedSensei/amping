from ninja import Schema

class CreatePatientPayload(Schema):
    name: str
    email: str
    contact: str
    age: int
    age_group: str

class LoginHealthProviderPayload(Schema):
    email: str
    password: str

class RefreshTokenPayload(Schema):
    refresh_token: str

__ALL__ = [
    "CreatePatientPayload",
    "LoginHealthProviderPayload",
    "RefreshTokenPayload"
]