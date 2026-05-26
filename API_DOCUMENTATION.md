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

## Current Coverage

No other endpoints are currently defined outside the three authentication endpoints above.
