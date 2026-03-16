# Configuration Reference - Complete Documentation

## Overview

Complete configuration reference for all services.

---

## AuthService Configuration

### application.properties

```properties
# Server
server.port=8081
spring.application.name=Auth-Service
server.servlet.contextPath=/api/v1/auth/

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/auth_db
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.jpa.hibernate.ddl-auto=update

# JWT
security.jwt.token.secret-key=${SECRET_KEY:dev-secret-key}
security.jwt.token.refresh-secret-key=${REFRESH_SECRET_KEY:dev-refresh-secret-key}
security.jwt.token.expiration=${TOKEN_EXPIRATION_TIME:3600000}
security.jwt.token.refresh-expiration=${REFRESH_TOKEN_EXPIRATION_TIME:604800000}

# Kafka
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:29092}

# Mail
spring.mail.host=${SMTP_HOST:}
spring.mail.port=${SMTP_PORT:587}
spring.mail.username=${SMTP_USERNAME}
spring.mail.password=${SMTP_PASSWORD}
app.mail.from=${SMTP_USERNAME:no-reply@diagnocare.com}

# OTP
app.otp.length=6
app.otp.expiration-minutes=10

# GDPR
app.consent.version=${CONSENT_VERSION:v1.0-2024}
encryption.secret-key=${ENCRYPTION_SECRET_KEY:}

# Eureka
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
```

---

## DiagnoCareService Configuration

### application.properties

```properties
# Server
server.port=8080
spring.application.name=DiagnoCare-Service

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/diagnocare_db
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.jpa.hibernate.ddl-auto=update

# Kafka
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:29092}
spring.kafka.consumer.group-id=diagnocare-user-sync

# ML Service
ml.service.url=${ML_SERVICE_URL:http://ml-prediction-service:5000}

# Check-In
app.checkin.scheduler-delay-ms=${CHECKIN_SCHEDULER_DELAY_MS:900000}
app.checkin.first-reminder-minutes=${CHECKIN_FIRST_REMINDER_MINUTES:1440}
app.checkin.second-reminder-minutes=${CHECKIN_SECOND_REMINDER_MINUTES:2880}
app.checkin.base-url=${CHECKIN_BASE_URL:http://localhost:3000/check-in}

# Encryption
encryption.secret-key=${ENCRYPTION_SECRET_KEY:}

# Eureka
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
```

---

## GatewayService Configuration

### application.properties

```properties
spring.application.name=gateway-service
server.port=8765

# Eureka
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka/
eureka.client.fetch-registry=true
eureka.client.register-with-eureka=true

# Auth Service
auth.service.uri=http://AUTH-SERVICE:8081/
auth.service.validate.token.endpoint=api/v1/auth/validate-token

# Routes
spring.cloud.gateway.routes[0].id=auth-service-route
spring.cloud.gateway.routes[0].uri=lb://AUTH-SERVICE
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/v1/auth/**

spring.cloud.gateway.routes[1].id=diagnocare-service-route
spring.cloud.gateway.routes[1].uri=lb://DIAGNOCARE-SERVICE
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/v1/diagnocare/**
spring.cloud.gateway.routes[1].filters[0]=AuthFilter
```

---

## Environment Variables Summary

### Required

- `ENCRYPTION_SECRET_KEY`: AES-256-GCM key (32 bytes, base64)
- `SECRET_KEY`: JWT access token secret (64 bytes, base64)
- `REFRESH_SECRET_KEY`: JWT refresh token secret (64 bytes, base64)
- `SMTP_USERNAME`: Email username
- `SMTP_PASSWORD`: Email password
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password

### Optional

- `TOKEN_EXPIRATION_TIME`: Access token expiration (ms, default: 3600000)
- `REFRESH_TOKEN_EXPIRATION_TIME`: Refresh token expiration (ms, default: 604800000)
- `CONSENT_VERSION`: Consent version (default: v1.0-2024)
- `ML_SERVICE_URL`: ML service URL (default: http://ml-prediction-service:5000)
- `CHECKIN_SCHEDULER_DELAY_MS`: Scheduler interval (ms, default: 900000)
- `CHECKIN_FIRST_REMINDER_MINUTES`: First reminder delay (minutes, default: 1440)
- `CHECKIN_SECOND_REMINDER_MINUTES`: Second reminder delay (minutes, default: 2880)
- `CHECKIN_BASE_URL`: Frontend check-in URL

---

## Secret Generation

### Encryption Key
```bash
openssl rand -base64 32
```

### JWT Secrets
```bash
openssl rand -base64 64
```

---

## See Also
- [Deployment](11-deployment.md)
- [Architecture Overview](01-architecture-overview.md)
