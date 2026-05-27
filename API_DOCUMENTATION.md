# Amping API Documentation

## Base URL

All API routes are mounted under:

`/api/v1/`

## Route Groups

The currently registered router groups are:

- `/api/v1/mobile/`
- `/api/v1/web/`

## Admin

| Method | Path | Description |
| --- | --- | --- |
| GET | `/admin/` | Django admin site |

## Web Endpoints

### `POST /api/v1/web/login/`

Authenticate a healthcare provider user.
Possible return codes:
- `200` — Login successful
- `400` — Already authenticated / bad request
- `401` — Invalid credentials
- `500` — Server error

Payload (request): `Web_LoginHealthProviderPayload`
- `email` (string): healthcare provider email
- `password` (string): plaintext password

Response: `Web_LoginHealthProviderResponse`
- `message` (string): human-readable status message

Example request payload:

```json
{
  "email": "provider@example.com",
  "password": "secret-password"
}
```

Example response payload:

```json
{
  "message": "Login successful"
}
```

### `POST /api/v1/web/logout/`

Log out the currently authenticated healthcare provider user.

No request payload schema.
Possible return codes:
- `200` — Logged out successfully
- `401` — Not authenticated
- `500` — Server error

Response: `Web_LogoutResponse`
- `message` (string): human-readable status

Example response payload:

```json
{
  "message": "Logged out successfully"
}
```

Notes:

- Route config: `@web_v1_router.post("/logout/", response=Web_LogoutResponse)`
- Uses router-level Django session auth.

### `GET /api/v1/web/profile/`

Get the authenticated healthcare provider profile.

Possible return codes:
- `200` — Success
- `401` — Unauthorized

Response: `Web_HealthCareProviderDetailResponse`
- `id` (int)
- `firstname` (string)
- `lastname` (string)
- `email` (string)
- `contact` (string)
- `clinic` (string)

Example response payload:

```json
{
  "id": 10,
  "firstname": "Dr.",
  "lastname": "Who",
  "email": "dr@example.com",
  "contact": "",
  "clinic": "Central Clinic"
}
```

### `GET /api/v1/web/patient/{patient_id}/gamification/`

Get gamification status for a patient.

Response: `Web_GamificationResponse`

```json
{
  "best_streak": 14,
  "current_streak": 7,
  "heart_quota": 3,
  "penalty_history": [
    { "date": "2026-04-01T00:00:00Z", "label": "missed_day", "tier": 1 }
  ],
  "total_regimen_days": 400
}
```

Possible return codes:
- `200` — Success
- `401` — Unauthorized
- `404` — Patient not found
- `500` — Server error

Response: `Web_GamificationResponse`
- `best_streak` (int)
- `current_streak` (int)
- `heart_quota` (int)
- `penalty_history` (list of objects): each with `date` (ISO string), `label` (string), `tier` (int)
 - `penalty_history` (list of objects): each with `date` (ISO string), `label` (string), `tier` (int; possible values: 1, 2, 3)
- `total_regimen_days` (int)

Example response payload:

```json
{
  "best_streak": 14,
  "current_streak": 7,
  "heart_quota": 3,
  "penalty_history": [
    { "date": "2026-04-01T00:00:00Z", "label": "missed_day", "tier": 1 }
  ],
  "total_regimen_days": 400
}
```

Notes:

- Route config: `@web_v1_router.get("/patient/{patient_id}/gamification/", response=Web_GamificationResponse)`
- Returns summary fields such as `current_streak`, `heart_quota`, and `penalty_history`.

### `GET /api/v1/web/patient/`

List all patients for the authenticated healthcare provider.

Possible return codes:
- `200` — Success
- `401` — Unauthorized

Response: `Web_GetAllPatientsResponse`
- `patients` (list): items with `id` (int), `firstname` (string), `lastname` (string), `email` (string), `contact` (string), `birthyear` (int)

Example response payload:

```json
{
  "patients": [
    { "id": 1, "firstname": "Jane", "lastname": "Doe", "email": "jane@example.com", "contact": "", "birthyear": 1980 }
  ]
}
```

Route config: `@web_v1_router.get("/patient/", response=Web_GetAllPatientsResponse)`

### `POST /api/v1/web/patient/`

Create a new patient under the authenticated healthcare provider.

Possible return codes:
- `201` — Created
- `400` — Validation error
- `401` — Unauthorized
- `500` — Server error

Request payload: `Web_CreatePatientPayload`
- `firstname` (string)
- `lastname` (string)
- `email` (string)
- `contact` (string)
- `birthyear` (int)
- `guardians` (list of `Web_PatientGuardianEntry`)
- `regimen_start` (date)
- `total_days` (int)

`Web_PatientGuardianEntry`:
- `id` (int)
- `firstname` (string)
- `lastname` (string)
- `email` (string)
- `contact` (string)

Response: `Web_CreatePatientResponse`
- `id` (int): newly created patient id

Example request payload:

```json
{
  "firstname": "John",
  "lastname": "Smith",
  "email": "john.smith@example.com",
  "contact": "",
  "birthyear": 1990,
  "guardians": [],
  "regimen_start": "2026-05-01",
  "total_days": 120
}
```

Example response payload:

```json
{ "id": 42 }
```

Route config: `@web_v1_router.post("/patient/", response=Web_CreatePatientResponse)`

### `GET /api/v1/web/patient/{patient_id}/`

Get detailed patient information.

Possible return codes:
- `200` — Success
- `401` — Unauthorized
- `404` — Not found

Response: `Web_PatientDetailResponse`
- `id`, `firstname`, `lastname`, `email`, `contact`, `birthyear`
- `guardians` (list of `Web_PatientGuardianEntry`)
- `regimen_start` (date)
- `current_day` (int)
- `total_days` (int)
- `month_pdc` (float)
- `pdc_target` (float)
- `month3_protected` (bool)

Example response payload (abridged):

```json
{
  "id": 501,
  "firstname": "Alice",
  "lastname": "Lopez",
  "regimen_start": "2026-01-01",
  "current_day": 120,
  "total_days": 400,
  "month_pdc": 0.95,
  "pdc_target": 0.9,
  "month3_protected": false
}
```

Route config: `@web_v1_router.get("/patient/{patient_id}/", response=Web_PatientDetailResponse)`

### `GET /api/v1/web/patient/{patient_id}/adherence_month/`

Get the adherence summary for a patient for a month.

Response: `Web_AdherenceMonthResponse`

Example response:

```json
{
  "adherence_days": [
    {
      "date": "2026-05-01T00:00:00Z",
      "id": 501,
      "status": "app_recorded",
      "symptoms": [],
      "video_link": null
    },
    {
      "date": "2026-05-02T00:00:00Z",
      "id": 502,
      "status": "technical_miss",
      "symptoms": ["nausea"],
      "video_link": "https://videos.example.com/502.mp4"
    }
  ],
  "month": 5,
  "month_pdc": 95,
  "pdc_target": 90,
  "year": 2026
}
```

Possible return codes:
- `200` — Success
- `401` — Unauthorized
- `404` — Patient or month data not found
- `500` — Server error

Response: `Web_AdherenceMonthResponse`
- `adherence_days` (list): items with `date` (ISO string), `id` (int), `status` (string), `symptoms` (list of strings), `video_link` (string|null)
 - `adherence_days` (list): items with `date` (ISO string), `id` (int), `status` (string; possible values: "app_recorded", "provider_reconciled", "technical_miss", "unverified_absence"), `symptoms` (list of strings), `video_link` (string|null)
- `month` (int)
- `month_pdc` (int)
- `pdc_target` (int)
- `year` (int)

Example response payload:

```json
{
  "adherence_days": [
    {
      "date": "2026-05-01T00:00:00Z",
      "id": 501,
      "status": "app_recorded",
      "symptoms": [],
      "video_link": null
    },
    {
      "date": "2026-05-02T00:00:00Z",
      "id": 502,
      "status": "technical_miss",
      "symptoms": ["nausea"],
      "video_link": "https://videos.example.com/502.mp4"
    }
  ],
  "month": 5,
  "month_pdc": 95,
  "pdc_target": 90,
  "year": 2026
}
```

Notes:

- Route config: `@web_v1_router.get("/patient/{patient_id}/adherence_month/", response=Web_AdherenceMonthResponse)`
- Returns `adherence_days` entries with status and optional `video_link`.

### `GET /api/v1/web/patient/{patient_id}/anomalous_entries/`

List anomalous adherence entries for a patient.

Response: `Web_AnomalousEntriesResponse`

Example response:

```json
{
  "entries": [
    { "date": "2026-05-05T00:00:00Z", "id": 201, "reason": "device_offline" },
    { "date": "2026-05-12T00:00:00Z", "id": 202, "reason": "technical_miss" }
  ]
}
```

Possible return codes:
- `200` — Success
- `401` — Unauthorized
- `404` — Patient not found
- `500` — Server error

Response: `Web_AnomalousEntriesResponse`
- `entries` (list): items with `date` (ISO string), `id` (int), `reason` (string)

Example response payload:

```json
{
  "entries": [
    { "date": "2026-05-05T00:00:00Z", "id": 201, "reason": "device_offline" },
    { "date": "2026-05-12T00:00:00Z", "id": 202, "reason": "technical_miss" }
  ]
}
```

Notes:

- Route config: `@web_v1_router.get("/patient/{patient_id}/anomalous_entries/", response=Web_AnomalousEntriesResponse)`

### `POST /api/v1/web/patient/{patient_id}/reconcile_anomalies/`

Reconcile one or more anomalous adherence entries (mark verified, update streaks, etc.).

Possible return codes:
- `200` — Reconciliation successful (returns summary)
- `400` — Validation error (missing/invalid fields)
- `401` — Unauthorized
- `404` — Patient or entry not found
- `500` — Server error

Request payload: `Web_ReconcileAnomalyPayload`
- `entry_ids` (list[int]): IDs of entries to reconcile
- `reason` (string): provider's note/reason for reconciliation
- `verification_method` (ReconciliationMethodEnum): (`home_visit`, `dot_order`, `send_message`)

Response: `Web_ReconcileAnomalyResponse`
- `reconciled_count` (int)
- `updated_heart_quota` (int)
- `updated_pdc` (float)
- `updated_streak` (int)

Example request payload:

```json
{
  "entry_ids": [345, 346],
  "reason": "Provider verified technical miss",
  "verification_method": "phone_call"
}
```

Example response payload:

```json
{
  "reconciled_count": 2,
  "updated_heart_quota": 5,
  "updated_pdc": 87.0,
  "updated_streak": 7
}
```

Notes:

- Route config: `@web_v1_router.post("/patient/{patient_id}/reconcile_anomalies/", response=Web_ReconcileAnomalyResponse)`
- Request includes `entry_ids` and `verification_method`.

## Mobile Endpoints

### `POST /api/v1/mobile/refresh-token/`

Refresh a patient token using a refresh token.

`Mobile_RefreshTokenPayload`

```json
{
  "refresh_token": "current-refresh-token"
}
```

`Mobile_RefreshTokenResponse`

```json
{
  "access_token": "new-access-token"
}
```

Possible return codes:
- `200` — Token refreshed successfully
- `400` — Invalid / expired refresh token
- `401` — Unauthorized
- `500` — Server error

Request payload: `Mobile_RefreshTokenPayload`
- `refresh_token` (string)

Response: `Mobile_RefreshTokenResponse`
- `access_token` (string)

Example request payload:

```json
{
  "refresh_token": "current-refresh-token"
}
```

Example response payload:

```json
{
  "access_token": "new-access-token"
}
```

Notes:

- Route config: `@mobile_v1_router.post("/refresh-token/", response=Mobile_RefreshTokenResponse)`

### `GET /api/v1/mobile/profile/`

Get the patient profile (authenticated by access token).

Possible return codes:
- `200` — Success
- `401` — Unauthorized

Response: `Mobile_PatientProfileResponse`
- `id`, `firstname`, `lastname`, `email`, `contact`, `birthyear`
- `regimen_start` (date)
- `current_day` (int)
- `total_days` (int)

Example response payload:

```json
{
  "id": 501,
  "firstname": "Bob",
  "lastname": "Lee",
  "regimen_start": "2026-01-01",
  "current_day": 30,
  "total_days": 120
}
```

Route config: `@mobile_v1_router.get("/profile/", response=Mobile_PatientProfileResponse)`

### `GET /api/v1/mobile/healthcare-profile/`

Get the healthcare provider details for a patient's registered provider.

Possible return codes:
- `200` — Success
- `401` — Unauthorized

Response: `Mobile_HealthCareProviderProfileResponse` (same as `Web_HealthCareProviderDetailResponse`)
- `id`, `firstname`, `lastname`, `email`, `contact`, `clinic`

Example response payload:

```json
{
  "id": 10,
  "firstname": "Dr.",
  "lastname": "Who",
  "email": "dr@example.com",
  "contact": "",
  "clinic": "Central Clinic"
}
```

Route config: `@mobile_v1_router.get("/healthcare-profile/", response=Mobile_HealthCareProviderProfileResponse)`

### `GET /api/v1/mobile/stats/`

Return gamification stats for the authenticated patient.

Possible return codes:
- `200` — Success
- `401` — Unauthorized

Response: `Mobile_StatsResponse`
- `total_regimen_days` (int)
- `current_streak` (int)
- `best_streak` (int)
- `heart_quota` (int)
- `penalty_history` (list of events with `date`, `tier`, `label`)

Route config: `@mobile_v1_router.get("/stats/", response=Mobile_StatsResponse)`

Example response payload:

```json
{
  "total_regimen_days": 400,
  "current_streak": 7,
  "best_streak": 14,
  "heart_quota": 3,
  "penalty_history": [
    { "date": "2026-04-01", "tier": 1, "label": "missed_day" }
  ]
}
```

### `GET /api/v1/mobile/weekly_adherence/`

Get adherence entries for the current week for the authenticated patient.

Possible return codes:
- `200` — Success
- `401` — Unauthorized

Response: `Mobile_WeeklyAdherenceResponse`
- `week_start` (date)
- `week_end` (date)
- `adherence_days` (list): items with `date` (date), `status` (string; possible values: "app_recorded", "provider_reconciled", "technical_miss", "unverified_absence")

Route config: `@mobile_v1_router.get("/weekly_adherence/", response=Mobile_WeeklyAdherenceResponse)`

Example response payload:

```json
{
  "week_start": "2026-05-18",
  "week_end": "2026-05-24",
  "adherence_days": [
    { "date": "2026-05-18", "status": "app_recorded" },
    { "date": "2026-05-19", "status": "technical_miss" }
  ]
}
```

### `POST /api/v1/mobile/upload_symptoms/`

Upload symptoms for a given date (mobile client).

Possible return codes:
- `200` — Success
- `400` — Validation error
- `401` — Unauthorized

Request payload: `Mobile_UploadSymtomsPayload`
- `date` (date)
- `symptoms` (list[string])

Response: `Mobile_UploadSymtomsResponse`
- `message` (string)

Route config: `@mobile_v1_router.post("/upload_symptoms/", response=Mobile_UploadSymtomsResponse)`

Example request payload:

```json
{
  "date": "2026-05-27",
  "symptoms": ["nausea", "headache"]
}
```

Example response payload:

```json
{ "message": "Symptoms uploaded successfully" }
```

### `GET /api/v1/mobile/adherence_video_endpoint/`

Return a signed/temporary upload endpoint for adherence video uploads.

Possible return codes:
- `200` — Success
- `401` — Unauthorized

Response: `Mobile_GetAdherenceVideoEndpointResponse`
- `adherence_day_id` (int)
- `video_endpoint` (string)

Route config: `@mobile_v1_router.get("/adherence_video_endpoint/", response=Mobile_GetAdherenceVideoEndpointResponse)`

Example response payload:

```json
{
  "adherence_day_id": 502,
  "video_endpoint": "https://upload.example.com/signed-url/abcdef"
}
```

## Route Registration Notes

- All sub-routers are mounted under `/api/v1/` in [backend/Amping/urls.py](backend/Amping/urls.py) and wired in [backend/Amping/api.py](backend/Amping/api.py) via `api_v1.add_router(...)` for `mobile/` and `web/` groups.

Recent updates in [backend/Amping/api.py](backend/Amping/api.py#L1-L14):
- The `gamification` and `adherence` routers are now mounted alongside `users` for both `mobile/` and `web/` groups. The file registers all three apps' mobile and web routers under the same `mobile/` and `web/` prefixes.

## Current Coverage

Endpoints registered in `users`, `gamification`, and `adherence` apps are included above. Additional schema types exist (e.g. patient detail and create-patient schemas in `backend/users/schemas.py`) but corresponding routes are not currently exposed in the routers.

## Current Coverage

No other endpoints are currently defined outside the three authentication endpoints above.
