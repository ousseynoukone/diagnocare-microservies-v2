# GDPR Implementation - Complete Documentation

## Overview

DiagnoCare implements GDPR compliance features including consent management, data encryption, data export, and anonymization.

---

## GDPR Features

### 1. Consent Management

**Implementation**: Required consent acceptance during registration

**Fields**:
- `privacyPolicyAccepted`: Boolean (required)
- `termsAccepted`: Boolean (required)
- `consentDate`: Timestamp
- `consentVersion`: String (e.g., "v1.0-2024")

**Validation**: Both fields must be `true` for registration

**Storage**: AuthService `users` table

---

### 2. Data Encryption at Rest

**Algorithm**: AES-256-GCM

**Encrypted Fields**:

**AuthService**:
- `users.email`
- `users.phone_number`

**DiagnoCareService**:
- `users.email`
- `users.address`
- `users.phone_number`
- `patient_medical_profiles.weight`
- `patient_medical_profiles.mean_bp`
- `patient_medical_profiles.mean_chol`

**Implementation**: JPA AttributeConverters automatically encrypt/decrypt

---

### 3. Data Export

**Endpoint**: `GET /api/v1/diagnocare/users/{id}/export`

**Service**: `UserDataExportService`

**Exports**:
- User profile (all fields, decrypted)
- Medical profile
- All session symptoms
- All predictions
- All check-ins
- All reports

**Format**: JSON. The response body is **raw JSON** (not wrapped in the standard API envelope `{ data, message, statusCode }`), so clients can parse the export object directly.

**Purpose**: GDPR Article 15 (Right of access)

---

### 4. Data Anonymization

**Trigger**: User account deletion (`USER_DELETED` Kafka event)

**Service**: `UserDataAnonymizationService`

**Process**:
1. Anonymize PII:
   - Email → `deleted_{uuid}@anonymized.local`
   - First name → `ANONYMOUS_USER`
   - Last name → `ANONYMOUS_USER`
   - Phone → `null`
   - Address → `null`
2. Set `isActive = false` (cannot login)
3. **Preserve health data**:
   - Predictions
   - Session symptoms
   - Check-ins
   - Reports
   - Medical profile

**Purpose**: GDPR Article 17 (Right to erasure) while preserving anonymized health data for research

---

### 5. Email Uniqueness with Encryption

**Challenge**: Encrypted email values vary (random IV), so unique constraint doesn't work

**Solution**: SHA-256 hash of email for uniqueness checks

**Implementation**:
- `emailHash`: SHA-256 hash of email (plain text)
- Unique constraint on `emailHash`
- Used for email uniqueness validation
- Calculated in `@PrePersist` lifecycle callback

---

## Privacy Policy & Terms of Service

**Location**: `docs/privacy-policy.md`, `docs/terms-of-service.md`

**Content**:
- Data collection practices
- Data processing purposes
- User rights
- Security measures
- Contact information

---

## Legal Basis for Health Data

**Article 9(2)(a)**: Explicit consent  
**Article 9(2)(h)**: Healthcare purposes  
**Article 9(2)(j)**: Research purposes (anonymized data)

---

## Data Retention

- **Active users**: Data retained indefinitely
- **Deleted users**: PII anonymized, health data retained (anonymized)
- **OTP codes**: Deleted after expiration (10 minutes)

---

## User Rights

### Right of Access (Article 15)
- **Implementation**: Data export endpoint
- **Format**: JSON export

### Right to Erasure (Article 17)
- **Implementation**: Account deletion → Anonymization
- **Note**: Health data preserved (anonymized)

### Right to Data Portability (Article 20)
- **Implementation**: Data export endpoint
- **Format**: Machine-readable JSON

### Right to Rectification (Article 16)
- **Implementation**: User update endpoint

---

## Security Measures

1. **Encryption at Rest**: AES-256-GCM
2. **Password Hashing**: BCrypt
3. **JWT Tokens**: Secure token-based authentication
4. **HTTPS**: Required in production
5. **Access Control**: Role-based authorization

---

## See Also
- [Database Schema](02-database-schema.md) - Encryption details
- [Auth Service](03-auth-service.md) - Consent management
- [DiagnoCare Service](04-diagnocare-service.md) - Export & anonymization
