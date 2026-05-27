import asyncio

from django.tasks import task


@task()
async def mobile_token_worker():
    while True:
        print("Running mobile token worker...")
        await asyncio.sleep(2)