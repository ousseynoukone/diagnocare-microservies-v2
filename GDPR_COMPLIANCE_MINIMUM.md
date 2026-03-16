# GDPR Compliance - Minimum Requirements for DiagnoCare

## Executive Summary

**DiagnoCare processes sensitive health data (special category under GDPR Article 9)**, which requires **explicit consent** and **enhanced protection**. This document outlines the **minimum requirements** to achieve basic GDPR compliance.

---

## 🔐 Encryption & Anonymization Requirements

### **GDPR Requirements for Health Data:**

**Article 32 (Security of Processing) - REQUIRED:**
> "Taking into account the state of the art, the cost of implementation and the nature, scope, context and purposes of processing as well as the risk of varying likelihood and severity for the rights and freedoms of natural persons, the controller and the processor shall implement **appropriate technical and organisational measures** to ensure a level of security appropriate to the risk, including... **encryption of personal data**."

**For health data (special category), encryption is REQUIRED, not optional.**

### **Three Approaches Explained:**

| Approach | Reversible? | Use Case | GDPR Status |
|----------|------------|----------|-------------|
| **Encryption** | ✅ Yes (with key) | Active user data storage | **REQUIRED** for health data |
| **Pseudonymization** | ✅ Yes (with mapping) | Analytics, ML processing | **Recommended** (reduces risk) |
| **Anonymization** | ❌ No (irreversible) | Archived/deleted data, research | **Optional** (data becomes non-personal) |

### **What You MUST Do:**

1. **Encrypt sensitive fields at rest** (email, phone, medical data) - **REQUIRED**
2. **Use HTTPS for all API calls** - **REQUIRED**
3. **Pseudonymize data sent to ML service** - **Strongly Recommended**
4. **Anonymize or delete old data** - **Recommended** (reduces liability)

### **Current Status:**
- ❌ No encryption at rest
- ❓ HTTPS status unknown (check configuration)
- ❌ No pseudonymization for ML service
- ❌ No anonymization strategy

---

## 🔴 Critical Issues Found

### 1. **No Explicit Consent Mechanism**
- Users register without accepting privacy policy/terms
- No consent tracking for data processing
- No consent withdrawal mechanism

### 2. **Incomplete Data Deletion**
- `deleteUser()` only sets `isActive=false` (soft delete)
- Health data (Predictions, SessionSymptoms, Reports, CheckIns) are NOT deleted
- Data remains in database permanently

### 3. **No Data Export (Right to Data Portability)**
- Users cannot export their data
- No API endpoint for data export

### 4. **No Privacy Policy/Terms Acceptance**
- Registration doesn't require privacy policy acceptance
- No terms of service acceptance

### 5. **No Data Retention Policy**
- No automatic deletion of old data
- No retention period defined

---

## ✅ Minimum Requirements to Pass GDPR

### **Priority 1: CRITICAL (Must Have)**

#### 1.1 Add Consent Management

**What to implement:**
```java
// Add to User entity (AuthService)
@Column(name = "privacy_policy_accepted", nullable = false)
private Boolean privacyPolicyAccepted = false;

@Column(name = "terms_accepted", nullable = false)
private Boolean termsAccepted = false;

@Column(name = "consent_date")
private Date consentDate;

@Column(name = "consent_version")
private String consentVersion; // e.g., "v1.0-2024"
```

**Registration flow:**
- Require checkbox: "I accept Privacy Policy and Terms of Service"
- Store consent date and version
- Re-request consent if policy changes

**Files to modify:**
- `AuthService/src/main/java/com/homosapiens/authservice/model/User.java`
- `AuthService/src/main/java/com/homosapiens/authservice/model/dtos/UserRegisterDto.java`
- `AuthService/src/main/java/com/homosapiens/authservice/controller/AuthController.java`

---

#### 1.2 Implement Complete Data Deletion

**Current problem:**
```java
// Current: Only soft delete
user.setIsActive(false); // ❌ Data still exists
```

**Required: Hard delete all user data**

**What to implement:**

```java
// In AuthService - AuthService.java
public void deleteUser(Long id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));
    
    // Send event to DiagnoCareService to delete all health data FIRST
    sendUserEvent(KafkaEvent.USER_DELETED, user, false);
    
    // Delete OTPs
    otpRepository.deleteByUser(user);
    
    // Finally delete user
    userRepository.delete(user); // Hard delete
}
```

**In DiagnoCareService - Create UserDataDeletionService:**

```java
@Service
@Transactional
public class UserDataDeletionService {
    
    public void deleteAllUserData(Long userId) {
        // 1. Delete CheckIns
        checkInRepository.deleteByUserId(userId);
        
        // 2. Delete Reports
        reportRepository.deleteByUserId(userId);
        
        // 3. Delete Predictions (cascade will handle PathologyResults)
        List<Prediction> predictions = predictionRepository.findByUserId(userId);
        predictionRepository.deleteAll(predictions);
        
        // 4. Delete SessionSymptoms
        sessionSymptomRepository.deleteByUserId(userId);
        
        // 5. Delete PatientMedicalProfile
        patientMedicalProfileRepository.deleteByUserId(userId);
        
        // 6. Finally delete User
        userRepository.deleteById(userId);
    }
}
```

**Files to create/modify:**
- `DiagnoCareService/src/main/java/com/homosapiens/diagnocareservice/service/UserDataDeletionService.java`
- `DiagnoCareService/src/main/java/com/homosapiens/diagnocareservice/core/kafka/UserSyncConsumer.java` (update USER_DELETED handler)
- Add cascade delete repositories

---

#### 1.3 Add Data Export (Right to Data Portability)

**What to implement:**

```java
@GetMapping("/users/{id}/export")
public ResponseEntity<byte[]> exportUserData(@PathVariable Long id) {
    // Generate JSON file with all user data
    UserDataExportDTO exportData = userDataExportService.exportUserData(id);
    
    // Convert to JSON
    String json = objectMapper.writeValueAsString(exportData);
    byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
    
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"user-data-export.json\"")
        .contentType(MediaType.APPLICATION_JSON)
        .body(jsonBytes);
}
```

**Export should include:**
- User profile data
- All predictions
- All symptoms
- All check-ins
- Medical profile
- Reports

**Files to create:**
- `DiagnoCareService/src/main/java/com/homosapiens/diagnocareservice/service/UserDataExportService.java`
- `DiagnoCareService/src/main/java/com/homosapiens/diagnocareservice/dto/UserDataExportDTO.java`
- Add endpoint to `UserController.java`

---

#### 1.4 Add Privacy Policy & Terms Acceptance

**What to implement:**

1. **Create Privacy Policy document** (static HTML/PDF)
2. **Add to registration:**
```java
@NotNull(message = "Privacy policy acceptance is required")
private Boolean privacyPolicyAccepted;

@NotNull(message = "Terms acceptance is required")
private Boolean termsAccepted;
```

3. **Store acceptance in database** (see 1.1)

**Files to create:**
- `docs/privacy-policy.md` or `privacy-policy.html`
- `docs/terms-of-service.md`
- Update `UserRegisterDto.java`

---

### **Priority 2: IMPORTANT (Should Have)**

#### 2.1 Add Data Retention Policy

**What to implement:**

```java
@Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
public void deleteExpiredData() {
    // Delete inactive users after 3 years
    LocalDateTime threeYearsAgo = LocalDateTime.now().minusYears(3);
    List<User> inactiveUsers = userRepository.findInactiveUsersBefore(threeYearsAgo);
    
    for (User user : inactiveUsers) {
        userDataDeletionService.deleteAllUserData(user.getId());
    }
}
```

**Retention periods:**
- Active users: Keep all data
- Inactive users (3 years): Delete all data
- Predictions older than 5 years: Archive or delete

---

#### 2.2 Add Audit Logging

**What to implement:**

```java
@Entity
@Table(name = "data_access_logs")
public class DataAccessLog {
    private Long id;
    private Long userId;
    private String action; // "VIEW", "EXPORT", "DELETE", "UPDATE"
    private String resourceType; // "PREDICTION", "PROFILE", etc.
    private Long resourceId;
    private String ipAddress;
    private Date timestamp;
}
```

**Log all sensitive data access:**
- Viewing predictions
- Exporting data
- Deleting account
- Updating medical profile

---

#### 2.3 Add Data Processing Information

**What to implement:**

Add endpoint to inform users about data processing:

```java
@GetMapping("/privacy/data-processing-info")
public ResponseEntity<DataProcessingInfoDTO> getDataProcessingInfo() {
    return ResponseEntity.ok(new DataProcessingInfoDTO(
        "We process your health data to provide disease predictions",
        "Legal basis: Explicit consent (GDPR Article 9(2)(a))",
        "Data retention: 3 years after account deletion",
        "Third parties: ML Service (anonymized data only)"
    ));
}
```

---

### **Priority 3: REQUIRED for Health Data (GDPR Article 32)**

#### 3.1 Encryption at Rest ⚠️ **REQUIRED for Health Data**

**GDPR Article 32 requires "appropriate technical measures" including encryption for special category data (health data).**

**Current status:** ❌ No encryption at rest - **This is a GDPR violation**

**What to implement:**

**Option A: Database-Level Encryption (Recommended)**
```sql
-- PostgreSQL: Enable Transparent Data Encryption (TDE)
-- Or use encrypted columns
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Encrypt sensitive columns
ALTER TABLE users 
  ADD COLUMN email_encrypted BYTEA,
  ADD COLUMN phone_encrypted BYTEA;

-- Encrypt health data
ALTER TABLE patient_medical_profiles
  ADD COLUMN weight_encrypted BYTEA,
  ADD COLUMN mean_bp_encrypted BYTEA;
```

**Option B: Application-Level Encryption (Easier to implement)**

```java
// Create EncryptionService
@Service
public class DataEncryptionService {
    
    @Value("${encryption.secret-key}")
    private String secretKey;
    
    private SecretKeySpec secretKeySpec;
    private Cipher cipher;
    
    @PostConstruct
    public void init() throws Exception {
        secretKeySpec = new SecretKeySpec(
            secretKey.getBytes(StandardCharsets.UTF_8), "AES");
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
    }
    
    public String encrypt(String plainText) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    public String decrypt(String encryptedText) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] decrypted = cipher.doFinal(
            Base64.getDecoder().decode(encryptedText));
        return new String(decrypted);
    }
}
```

**Fields to encrypt:**
- ✅ Email (pseudonymize or encrypt)
- ✅ Phone number
- ✅ Address
- ✅ Birth date
- ✅ Medical profile (weight, blood pressure, cholesterol)
- ✅ Symptoms descriptions
- ✅ Family medical history

**Fields NOT to encrypt (needed for queries):**
- User ID (keep for joins)
- Created/Updated dates
- IsActive flag

**Implementation:**
1. Add `@PrePersist` and `@PostLoad` hooks to encrypt/decrypt automatically
2. Use JPA `@Converter` for transparent encryption

```java
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    
    @Autowired
    private DataEncryptionService encryptionService;
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        return encryptionService.encrypt(attribute);
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return encryptionService.decrypt(dbData);
    }
}

// Use in entity
@Convert(converter = EncryptedStringConverter.class)
@Column(name = "email")
private String email;
```

**Files to create:**
- `DiagnoCareService/src/main/java/com/homosapiens/diagnocareservice/core/security/DataEncryptionService.java`
- `DiagnoCareService/src/main/java/com/homosapiens/diagnocareservice/core/security/EncryptedStringConverter.java`
- `AuthService/src/main/java/com/homosapiens/authservice/core/security/DataEncryptionService.java`

---

#### 3.2 Pseudonymization (Recommended for Analytics)

**What is Pseudonymization?**
- Replace identifying data with pseudonyms (reversible)
- Allows data analysis without exposing identities
- Reduces GDPR risk (Article 25 - Data Protection by Design)

**When to use:**
- Sending data to ML service (anonymize symptoms)
- Analytics and reporting
- Data sharing with researchers

**Implementation:**

```java
@Service
public class PseudonymizationService {
    
    public String pseudonymizeEmail(String email) {
        // Replace email with hash (reversible with key)
        return "user_" + hashEmail(email) + "@pseudonymized.local";
    }
    
    public String pseudonymizeUserId(Long userId) {
        // Map real ID to pseudonym ID
        return "PSEUDO_" + hashId(userId);
    }
    
    // For ML service - send only anonymized symptoms
    public MLPredictionRequestDTO anonymizeForML(PredictionRequestDTO request) {
        MLPredictionRequestDTO anonymized = new MLPredictionRequestDTO();
        anonymized.setSymptoms(request.getSymptoms()); // OK - no PII
        anonymized.setAge(request.getAge()); // OK - aggregated
        anonymized.setGender(request.getGender()); // OK - aggregated
        // DO NOT send: email, name, phone, address, userId
        return anonymized;
    }
}
```

**Current issue:** ML service might receive user IDs - check and anonymize!

---

#### 3.3 Anonymization (For Deleted/Archived Data)

**What is Anonymization?**
- Irreversible removal of identifying information
- Data becomes non-personal (GDPR no longer applies)
- Use for: archived data, research data, analytics

**When to use:**
- After user deletion (if you need to keep data for research)
- Old predictions (after retention period)
- Analytics datasets

**Implementation:**

```java
@Service
public class DataAnonymizationService {
    
    public void anonymizeUserData(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;
        
        // Anonymize personal data
        user.setEmail("deleted_" + UUID.randomUUID() + "@anonymized.local");
        user.setFirstName("ANONYMOUS");
        user.setLastName("USER");
        user.setPhoneNumber(null);
        user.setAddress(null);
        user.setBirthDate(null);
        
        // Keep health data for research (already anonymized)
        // Predictions, symptoms can stay (no PII)
        
        userRepository.save(user);
    }
}
```

**Strategy:**
- **Option 1:** Complete deletion (recommended) - Delete everything
- **Option 2:** Anonymization - Keep health data, remove PII (for research)

---

#### 3.4 Encryption in Transit (HTTPS)

**Current status:** Check if HTTPS is enabled

**Required:**
- ✅ All API calls must use HTTPS (TLS 1.2+)
- ✅ Database connections should use SSL
- ✅ Kafka messages should be encrypted

**Configuration:**
```properties
# application.properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
```

---

#### 3.5 Right to Rectification
- Already implemented via `updateUser()` ✅

#### 3.6 Right to Object
- Allow users to object to certain data processing
- Add flag: `objectToDataProcessing`

#### 3.7 Data Breach Notification
- Implement breach detection
- Notification mechanism (email users within 72 hours)

---

## 📋 Implementation Checklist

### Phase 1: Critical (Week 1)
- [ ] Add consent fields to User entity
- [ ] Require privacy policy/terms acceptance on registration
- [ ] Implement complete data deletion (hard delete)
- [ ] Create UserDataDeletionService
- [ ] Update USER_DELETED Kafka handler

### Phase 2: Important (Week 2)
- [ ] Implement data export endpoint
- [ ] Create UserDataExportService
- [ ] Add data retention policy scheduler
- [ ] Create privacy policy document
- [ ] Create terms of service document

### Phase 3: Security & Encryption (Week 3) - **REQUIRED for Health Data**
- [ ] Implement encryption at rest for sensitive fields
- [ ] Create DataEncryptionService
- [ ] Add EncryptedStringConverter for JPA
- [ ] Verify HTTPS/TLS is enabled
- [ ] Implement pseudonymization for ML service
- [ ] Add anonymization service for archived data
- [ ] Add audit logging
- [ ] Implement data processing info endpoint
- [ ] Test all GDPR rights (access, portability, deletion, rectification)

---

## 🔐 Legal Basis for Processing Health Data

**GDPR Article 9(2)(a): Explicit Consent**

You MUST:
1. ✅ Get explicit consent (checkbox, not pre-checked)
2. ✅ Document consent (date, version)
3. ✅ Allow withdrawal of consent
4. ✅ Explain what data is processed and why

**Current status:** ❌ No explicit consent mechanism

---

## 📊 Data Inventory

### Personal Data Collected:
- ✅ Name (firstName, lastName)
- ✅ Email
- ✅ Phone number
- ✅ Address
- ✅ Birth date
- ✅ Gender
- ✅ Language preference

### Health Data (Special Category):
- ✅ Symptoms (SessionSymptom)
- ✅ Medical profile (age, weight, BMI, blood pressure, cholesterol, smoking, alcohol, family history)
- ✅ Disease predictions (Prediction)
- ✅ Check-ins (CheckIn)
- ✅ Medical reports (Report)
- ✅ Pathology results (PathologyResult)

### Technical Data:
- ✅ IP addresses (via logs)
- ✅ Authentication tokens
- ✅ Timestamps (created_at, updated_at)

---

## 🚨 Current GDPR Violations

1. **Article 6 (Lawfulness):** No explicit consent for processing
2. **Article 7 (Conditions for consent):** No consent mechanism
3. **Article 13 (Information to provide):** No privacy policy
4. **Article 15 (Right of access):** No data export
5. **Article 17 (Right to erasure):** Incomplete deletion (soft delete only)
6. **Article 20 (Data portability):** No export functionality
7. **Article 30 (Records of processing):** No documentation
8. **Article 32 (Security of processing):** ❌ **No encryption at rest for health data** - **CRITICAL VIOLATION**

---

## 💰 Estimated Implementation Effort

- **Phase 1 (Critical):** 2-3 days
- **Phase 2 (Important):** 2-3 days  
- **Phase 3 (Security & Encryption - REQUIRED):** 3-5 days

**Total: 8-11 days** for minimum compliance

**Note:** Encryption at rest is **REQUIRED** for health data under GDPR Article 32, not optional.

---

## 📝 Next Steps

1. **Immediate:** Implement consent management (Priority 1.1)
2. **Immediate:** Fix data deletion (Priority 1.2)
3. **This week:** Add data export (Priority 1.3)
4. **This week:** Create privacy policy (Priority 1.4)
5. **Next week:** Add retention policy and audit logging

---

## ⚠️ Legal Disclaimer

This document provides technical guidance only. **Consult with a GDPR legal expert** before going to production. Healthcare data processing requires additional compliance (e.g., HIPAA if serving US users, national health data regulations).

---

## 📚 References

- [GDPR Official Text](https://gdpr-info.eu/)
- [GDPR Article 9 - Special Categories](https://gdpr-info.eu/art-9-gdpr/)
- [ICO Guide to GDPR](https://ico.org.uk/for-organisations/guide-to-data-protection/guide-to-the-general-data-protection-regulation-gdpr/)
