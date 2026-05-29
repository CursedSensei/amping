# Backend (Django)

## Stack

- Django 5
- PostgreSQL
- django-ninja
- boto3 (object storage integration)

## Setup

1. Create and activate a Python virtual environment.
2. Install dependencies:
   - `pip install -r requirements.txt`
3. Create `.env` from `.env.example` and set values for DB, JWT, and storage variables.

## Run Locally

From `/tmp/workspace/CursedSensei/amping/backend`:

1. Apply migrations:
   - `python manage.py migrate`
2. Start the server:
   - `python manage.py runserver`

Default local backend URL: `http://localhost:8000`

## Validation

- Test command: `python manage.py test`

Note: tests currently require valid database environment configuration.
