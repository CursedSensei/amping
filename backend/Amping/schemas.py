from ninja import Schema
from pydantic import ConfigDict

class ApiSchema(Schema):
    model_config = ConfigDict(extra="forbid")



class BaseResponse(ApiSchema):
    message: str

__ALL__ = [
    'BaseResponse'
]