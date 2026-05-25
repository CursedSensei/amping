# Amping API Documentation

Initial API reference generated from `backend/Amping/urls.py` and the router wiring in `backend/Amping/api.py`.

## Base URL

All API routes are mounted under:

`/api/v1/`

## Route Groups

The following router groups are currently registered:

- `/api/v1/healthcare/`
- `/api/v1/patients/`
- `/api/v1/auth/`

## Admin

| Method | Path | Description |
| --- | --- | --- |
| GET | `/admin/` | Django admin site |

## Authentication

### `POST /api/v1/auth/login/`

Authenticate a healthcare provider user.

Request body:

```json
{
	"email": "provider@example.com",
	"password": "secret-password"
}
```

Response:

```json
{
	"message": "Login successful"
}
```

Notes:

- Requires Django authentication.
- Returns `400` if the user is already authenticated.
- Returns `401` if the credentials are invalid.

### `POST /api/v1/auth/logout/`

Log out the currently authenticated healthcare provider user.

Response:

```json
{
	"message": "Logged out successfully"
}
```

Notes:

- Requires an authenticated session.
- Returns `401` if the request is not authenticated.

### `POST /api/v1/auth/refresh-token/`

Refresh a patient token using a refresh token.

Request body:

```json
{
	"refresh_token": "current-refresh-token"
}
```

Response:

```json
{
	"message": "Token refreshed successfully"
}
```

## Not Yet Implemented

The `healthcare` and `patients` router groups are mounted, but no endpoints are currently defined in them.
