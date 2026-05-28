# pyrefly: ignore [missing-import]
from Amping.schemas import ApiSchema

class Mobile_GetNotificationsResponse(ApiSchema):
    id: int
    title: str
    message: str
    created_at: str
    hasRead: bool
    isForGabby: bool
    

__ALL__ = [
    "Mobile_GetNotificationsResponse"
]