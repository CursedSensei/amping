from .models import Token
from ninja.errors import HttpError
import random
import string

def get_random_string(length):
    letters_and_digits = string.ascii_letters + string.digits
    result_str = ''.join(random.choices(letters_and_digits, k=length))
    return result_str

def getPatientUserByToken(request):
    # TODO: Dont use database for access token validation, use something like JWT instead later

    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith('Bearer '):
        raise HttpError(401, "Authorization header missing or invalid")

    access_token = auth_header.split(' ')[1]
    try:
        token_obj = Token.objects.get(access_token=access_token)
        return token_obj.patient
    except Token.DoesNotExist:
        raise HttpError(401, "Invalid or expired token")