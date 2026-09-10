# Auth and Health Contract

## GET `/actuator/health`

Returns `200` with service health information.

## POST `/api/v1/auth/register`

Request: `email`, `password`, `displayName`.

Returns `201` with a safe user summary. Password is never returned.

## POST `/api/v1/auth/login`

Request: `email`, `password`.

Returns `200` with access/refresh session information and safe user summary.
Invalid credentials return the standard error shape without identifying whether the email exists.

## POST `/api/v1/auth/logout`

Requires authentication. Revokes the active refresh/session record and returns `204`.

## GET `/api/v1/users/me`

Requires authentication. Returns the authenticated user's safe profile and roles.

## Error Contract

```json
{
  "type": "https://servicehub.ai/errors/validation",
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more fields are invalid.",
  "instance": "/api/v1/auth/register",
  "correlationId": "uuid",
  "fieldErrors": [{"field": "email", "message": "Must be a valid email address"}]
}
```
