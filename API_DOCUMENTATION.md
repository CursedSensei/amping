# Amping API Documentation

API reference generated from route wiring in `backend/Amping/urls.py`, `backend/Amping/api.py`, and endpoint definitions in `backend/users/api.py`.

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

## Schema Index

The following schemas are defined in `backend/users/schemas.py`:

| Schema | Purpose | Fields |
| --- | --- | --- |
| `Web_LoginHealthProviderPayload` | Login request payload | `email: str`, `password: str` |
| `Web_LoginHealthProviderResponse` | Login response payload | `message: str` |
| `Web_LogoutResponse` | Logout response payload | `message: str` |
| `Mobile_RefreshTokenPayload` | Refresh-token request payload | `refresh_token: str` |
| `Mobile_RefreshTokenResponse` | Refresh-token response payload | `access_token: str` |

The following schemas are defined in `backend/gamification/schemas.py`:

| Schema | Purpose | Fields |
| --- | --- | --- |
| `Web_GamificationResponse` | Gamification status for a patient | `total_regimen_days: int`, `current_streak: int`, `best_streak: int`, `heart_quota: int`, `penalty_history: list[Web_PenaltyEvent]` |

The following schemas are defined in `backend/adherence/schemas.py`:

| Schema | Purpose | Fields |
| --- | --- | --- |
| `Web_AdherenceMonthRequest` | Request payload for monthly adherence | `month: int`, `year: int` |
| `Web_AdherenceMonthResponse` | Monthly adherence summary | `month: int`, `year: int`, `month_pdc: float`, `pdc_target: float`, `adherence_days: list[Web_AdherenceDayEntry]` |
| `Web_AnomalousEntriesResponse` | List of anomalous adherence entries | `entries: list[Web_AnomalousEntry]` |
| `Web_ReconcileAnomalyPayload` | Payload to reconcile anomalous entries | `entry_ids: list[int]`, `verification_method: str`, `reason: str` |
| `Web_ReconcileAnomalyResponse` | Response after reconciling anomalies | `reconciled_count: int`, `updated_streak: int`, `updated_heart_quota: int`, `updated_pdc: float` |

## Web Endpoints

### `POST /api/v1/web/login/`

Authenticate a healthcare provider user.

`Web_LoginHealthProviderPayload`

```json
{
  "email": "provider@example.com",
  "password": "secret-password"
}
```

`Web_LoginHealthProviderResponse`

```json
{
  "message": "Login successful"
}
```

Notes:

- Route config: `@web_v1_router.post("/login/", auth=None, response=Web_LoginHealthProviderResponse)`
- Returns `400` if already authenticated.
- Returns `401` if credentials are invalid.

### `POST /api/v1/web/logout/`

Log out the currently authenticated healthcare provider user.

No request payload schema.

`Web_LogoutResponse`

```json
{
  "message": "Logged out successfully"
}
```

Notes:

- Route config: `@web_v1_router.post("/logout/", response=Web_LogoutResponse)`
- Uses router-level Django session auth.

### `GET /api/v1/web/patient/{patient_id}/gamification/`

Get gamification status for a patient.

Response: `Web_GamificationResponse`

Notes:

- Route config: `@web_v1_router.get("/patient/{patient_id}/gamification/", response=Web_GamificationResponse)`
- Returns summary fields such as `current_streak`, `heart_quota`, and `penalty_history`.

### `GET /api/v1/web/patient/{patient_id}/adherence_month/`

Get the adherence summary for a patient for a month.

Response: `Web_AdherenceMonthResponse`

Notes:

- Route config: `@web_v1_router.get("/patient/{patient_id}/adherence_month/", response=Web_AdherenceMonthResponse)`
- Returns `adherence_days` entries with status and optional `video_link`.

### `GET /api/v1/web/patient/{patient_id}/anomalous_entries/`

List anomalous adherence entries for a patient.

Response: `Web_AnomalousEntriesResponse`

Notes:

- Route config: `@web_v1_router.get("/patient/{patient_id}/anomalous_entries/", response=Web_AnomalousEntriesResponse)`

### `POST /api/v1/web/patient/{patient_id}/reconcile_anomalies/`

Reconcile one or more anomalous adherence entries (mark verified, update streaks, etc.).

Request: `Web_ReconcileAnomalyPayload`

Response: `Web_ReconcileAnomalyResponse`

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

Notes:

- Route config: `@mobile_v1_router.post("/refresh-token/", response=Mobile_RefreshTokenResponse)`

## Route Registration Notes

- All sub-routers are mounted under `/api/v1/` in [backend/Amping/urls.py](backend/Amping/urls.py) and wired in [backend/Amping/api.py](backend/Amping/api.py) via `api_v1.add_router(...)` for `mobile/` and `web/` groups.

## Current Coverage

Endpoints registered in `users`, `gamification`, and `adherence` apps are included above. Additional schema types exist (e.g. patient detail and create-patient schemas in `backend/users/schemas.py`) but corresponding routes are not currently exposed in the routers.

## Current Coverage

No other endpoints are currently defined outside the three authentication endpoints above.
