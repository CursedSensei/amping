import boto3
from django.conf import settings
from ninja.errors import HttpError

s3_client = boto3.client(
    "s3",
    endpoint_url=settings.VIDEO_BUCKET_ENDPOINT_URL,
    aws_access_key_id=settings.VIDEO_BUCKET_ACCESS_KEY_ID,
    aws_secret_access_key=settings.VIDEO_BUCKET_SECRET_ACCESS_KEY,
    region_name=settings.VIDEO_BUCKET_REGION,
    config=boto3.session.Config(signature_version='s3v4')
)

def _get_video_key(patient_id: int, record_id: int) -> str:
    """Helper function to construct the S3 video key based on patient and record IDs."""
    return f"patient_{patient_id}-adherence_{record_id}.mp4"


def create_signed_url(patient_id: int, record_id: int) -> str:
    """Generates a pre-signed URL for uploading a video to the S3 bucket."""
    video_key = _get_video_key(patient_id, record_id)
    try:
        url = s3_client.generate_presigned_url(
            'put_object',
            Params={
                'Bucket': settings.VIDEO_BUCKET_NAME,
                'Key': video_key,
            },
            ExpiresIn=7200  # URL expires in 2 hours
        )
        return url
    except Exception as e:
        raise HttpError(500, "Could not generate video upload URL")

def verify_video_upload(url_endpoint: str) -> bool:
    """Checks if the video has been successfully uploaded to the S3 bucket."""
    try :
        response = s3_client.head_object(
            Bucket=settings.VIDEO_BUCKET_NAME,
            Key=url_endpoint.split('/')[-1]  # Extract the video key from the endpoint
        )
        return response['ResponseMetadata']['HTTPStatusCode'] == 200
    except s3_client.exceptions.NoSuchKey:
        return False
    except Exception as e:
        raise HttpError(500, "Error verifying video upload")
    
def get_public_video_url(patient_id: int, record_id: int) -> str:
    """Constructs the public URL for an uploaded video."""
    video_key = _get_video_key(patient_id, record_id)
    return f"{settings.VIDEO_BUCKET_BASE_PUBLIC_URL}/{video_key}"