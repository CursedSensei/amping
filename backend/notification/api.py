from django.http import HttpRequest

from Amping.utils import create_routers
from .schemas import Mobile_GetNotificationsResponse
from .models import NotifyPatient
from users.utils import getPatientUserByToken

mobile_v1_router, web_v1_router = create_routers()

@mobile_v1_router.get('/notifications/', response=Mobile_GetNotificationsResponse)
def get_notifications(request: HttpRequest, id: int):
    patient = getPatientUserByToken(request)


    notifications = [
        Mobile_GetNotificationsResponse.Mobile_Notification(
            title=notification.title,
            message=notification.message,
            created_at=notification.created_at.isoformat(),
            hasRead=notification.hasRead,
            isForGabby=notification.isForGabby
        )
        for notification in NotifyPatient.objects.filter(patient=patient).order_by('-created_at')
    ]

    response = Mobile_GetNotificationsResponse(notifications=notifications)
    return response