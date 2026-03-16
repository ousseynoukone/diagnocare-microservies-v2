# Deployment Guide - Complete Documentation

## Overview

DiagnoCare is deployed using Docker Compose for local development and can be containerized for production.

---

## Prerequisites

- Docker & Docker Compose
- Java 17+
- Maven 3.8+
- Python 3.x (for ML service)
- 8GB+ RAM recommended

---

## Docker Compose Services

### 1. Databases

#### auth-postgres
- **Image**: postgres:15
- **Port**: 5432 (internal)
- **Database**: auth_db
- **Volumes**: auth_postgres_data

#### diagnocare-postgres
- **Image**: postgres:15
- **Port**: 5433 (internal)
- **Database**: diagnocare_db
- **Volumes**: diagnocare_postgres_data

### 2. Services

#### registry-service
- **Port**: 8761
- **Purpose**: Eureka service discovery

#### auth-service
- **Port**: 8081 (internal)
- **Context**: /api/v1/auth
- **Dependencies**: auth-postgres, registry-service

#### diagnocare-service
- **Port**: 8080 (internal)
- **Context**: /api/v1/diagnocare
- **Dependencies**: diagnocare-postgres, registry-service, ml-prediction-service

#### gateway-service
- **Port**: 8765 (exposed)
- **Purpose**: API Gateway
- **Dependencies**: auth-service, diagnocare-service

#### ml-prediction-service
- **Port**: 5000 (internal)
- **Technology**: Flask/Python
- **Volumes**: Models and translation data

### 3. Infrastructure

#### kafka
- **Port**: 29092 (external), 9092 (internal)
- **Mode**: KRaft
- **Volumes**: kafka_data

#### kafka-ui
- **Port**: 8083
- **Purpose**: Kafka monitoring

#### pgadmin4
- **Port**: 8888
- **Purpose**: Database management UI

---

## Environment Variables

Create `.env` file in project root:

```properties
# Database
AUTH_DB_NAME=auth_db
AUTH_DB_USERNAME=postgres
AUTH_DB_PASSWORD=your_password
AUTH_DIAGNOCARE_DB_PORT=5432

DIAGNOCARE_DB_NAME=diagnocare_db
DB_USERNAME=postgres
DB_PASSWORD=your_password
DIAGNOCARE_DB_PORT=5433

# JWT
SECRET_KEY=your_jwt_secret_key
REFRESH_SECRET_KEY=your_refresh_secret_key

# Encryption
ENCRYPTION_SECRET_KEY=your_encryption_key_base64

# Mail
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_app_password

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# ML Service
ML_SERVICE_URL=http://ml-prediction-service:5000

# Check-In
CHECKIN_SCHEDULER_DELAY_MS=900000
CHECKIN_FIRST_REMINDER_MINUTES=1440
CHECKIN_SECOND_REMINDER_MINUTES=2880
CHECKIN_BASE_URL=http://localhost:3000/check-in

# Consent
CONSENT_VERSION=v1.0-2024
```

---

## Deployment Steps

### 1. Generate Secrets

```bash
# Encryption key (32 bytes, base64)
openssl rand -base64 32

# JWT secrets (64 bytes, base64)
openssl rand -base64 64
```

### 2. Configure Environment

Copy `.env.example` to `.env` and fill in values.

### 3. Start Services

```bash
docker-compose up --build
```

### 4. Verify Services

- **Gateway**: http://localhost:8765
- **Eureka**: http://localhost:8761
- **Kafka UI**: http://localhost:8083
- **pgAdmin**: http://localhost:8888

---

## Health Checks

All services include health check endpoints:

- **AuthService**: `/api/v1/auth/health` (if implemented)
- **DiagnoCareService**: `/api/v1/diagnocare/health` (if implemented)
- **ML Service**: `/health`

---

## Production Considerations

### 1. Security
- Use strong secrets
- Enable HTTPS
- Restrict database ports
- Use secrets management (Vault, AWS Secrets Manager)

### 2. Scalability
- Scale services horizontally
- Use database read replicas
- Implement caching (Redis)
- Use load balancer

### 3. Monitoring
- Application logs (ELK, Splunk)
- Metrics (Prometheus, Grafana)
- Distributed tracing (Jaeger, Zipkin)

### 4. Backup
- Database backups (automated)
- Volume backups
- Disaster recovery plan

---

## Troubleshooting

### Services Not Starting
- Check Docker logs: `docker-compose logs <service-name>`
- Verify environment variables
- Check port conflicts

### Database Connection Issues
- Verify database is healthy: `docker-compose ps`
- Check connection strings
- Verify credentials

### Kafka Issues
- Check Kafka logs
- Verify KRaft configuration
- Check topic creation

---

## See Also
- [Configuration](12-configuration.md)
- [Architecture Overview](01-architecture-overview.md)
