# API Endpoints - Complete Reference

## Base URLs

**Gateway** (Production): `http://localhost:8765`  
**AuthService** (Direct): `http://localhost:8081/api/v1/auth`  
**DiagnoCareService** (Direct): `http://localhost:8080/api/v1/diagnocare`

---

## Authentication

All DiagnoCare endpoints require JWT authentication:
```
Authorization: Bearer <access_token>
```

Auth endpoints are public (no authentication required).

---

## Auth Service Endpoints

### POST `/api/v1/auth/register`
Register new user

### POST `/api/v1/auth/login`
Login and get tokens

### POST `/api/v1/auth/refresh-token`
Refresh access token

### POST `/api/v1/auth/validate-token`
Validate JWT token

### POST `/api/v1/auth/otp/send`
Send email verification OTP

### POST `/api/v1/auth/otp/validate`
Validate OTP and verify email

### PUT `/api/v1/auth/users/{id}`
Update user profile

### DELETE `/api/v1/auth/users/{id}`
Delete user account

---

## DiagnoCare Service Endpoints

### Predictions

#### POST `/api/v1/diagnocare/predictions`
Create prediction. **Body**: `userId` (long), `symptomLabels` (array of strings). Optional medical profile is loaded from the user; no `symptomIds` or raw description in the request.

#### GET `/api/v1/diagnocare/predictions/{id}`
Get prediction by ID

#### PUT `/api/v1/diagnocare/predictions/{id}`
Update prediction

#### DELETE `/api/v1/diagnocare/predictions/{id}`
Delete prediction

#### GET `/api/v1/diagnocare/predictions/user/{userId}`
Get user predictions

#### GET `/api/v1/diagnocare/predictions/red-alerts`
Get red alert predictions

### Symptoms

Symptoms are managed by the ML pipeline and metadata; the API exposes read and delete only (no create/update).

#### GET `/api/v1/diagnocare/symptoms`
List all symptoms

#### GET `/api/v1/diagnocare/symptoms/{id}`
Get symptom by ID

#### DELETE `/api/v1/diagnocare/symptoms/{id}`
Delete symptom

#### GET `/api/v1/diagnocare/symptoms/search?label={query}`
Search symptoms by label

#### GET `/api/v1/diagnocare/symptoms/ml-metadata`
Get ML symptoms metadata (labels and translations from the ML service)

### Check-Ins

#### POST `/api/v1/diagnocare/check-ins`
Submit check-in. **Body**: `userId`, `previousPredictionId`, `symptomLabels` (array of strings). No `symptomIds` in the request.

#### GET `/api/v1/diagnocare/check-ins?userId={userId}`
Get user check-ins

### Users

#### GET `/api/v1/diagnocare/users/{id}`
Get user by ID

#### PUT `/api/v1/diagnocare/users/{id}`
Update user

#### DELETE `/api/v1/diagnocare/users/{id}`
Delete user

#### GET `/api/v1/diagnocare/users/{id}/export`
Export user data (GDPR). Response is **raw JSON** (not wrapped in the usual `{ data, message, statusCode }` envelope).

### Medical Profiles

#### GET `/api/v1/diagnocare/medical-profiles/user/{userId}`
Get medical profile

#### POST `/api/v1/diagnocare/medical-profiles`
Create/update profile

### Reports

#### GET `/api/v1/diagnocare/reports/user/{userId}`
Get user reports

#### POST `/api/v1/diagnocare/reports`
Create report

---

## Response Format

### Success Response
```json
{
  "message": "Success",
  "statusCode": 200,
  "data": {...}
}
```

### Error Response
```json
{
  "message": "Error message",
  "statusCode": 400,
  "data": null
}
```

---

## Status Codes

- `200 OK`: Success
- `201 Created`: Resource created
- `204 No Content`: Success (no body)
- `400 Bad Request`: Validation error
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Access denied
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource conflict (e.g., duplicate email)
- `500 Internal Server Error`: Server error

---

## See Also
- [Auth Service](03-auth-service.md) - Detailed auth endpoints
- [DiagnoCare Service](04-diagnocare-service.md) - Detailed DiagnoCare endpoints
