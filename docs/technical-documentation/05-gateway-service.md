# Gateway Service - Complete Documentation

## Overview

**GatewayService** is the API Gateway that serves as the single entry point for all client requests. It handles routing, authentication, and request/response transformation.

**Technology**: Spring Cloud Gateway  
**Port**: 8765  
**Service Discovery**: Eureka

---

## Architecture

### Responsibilities
1. **Request Routing**: Routes requests to appropriate microservices
2. **Authentication**: Validates JWT tokens before forwarding requests
3. **Service Discovery**: Uses Eureka to discover service instances
4. **Load Balancing**: Distributes requests across service instances
5. **Public Endpoints**: Allows unauthenticated access to public endpoints

---

## Routing Configuration

### Auth Service Route
```
Path: /api/v1/auth/**
Target: lb://AUTH-SERVICE
Filter: None (public endpoints)
```

### DiagnoCare Service Route
```
Path: /api/v1/diagnocare/**
Target: lb://DIAGNOCARE-SERVICE
Filter: AuthFilter (JWT validation required)
```

---

## Authentication Filter

### Public Endpoints (No Auth Required)
- `/api/v1/auth/login`
- `/api/v1/auth/register`
- `/api/v1/auth/refresh-token`
- `/api/v1/auth/validate-token`
- `/api/v1/auth/otp/**`
- Swagger paths (`/swagger-ui/**`, `/v3/api-docs/**`)

### Protected Endpoints (Auth Required)
- All `/api/v1/diagnocare/**` endpoints
- All other `/api/v1/auth/**` endpoints

### Filter Process
1. Check if path is public → Allow
2. Extract JWT from `Authorization: Bearer <token>` header
3. Call AuthService `/api/v1/auth/validate-token`
4. If valid → Forward request to target service
5. If invalid → Return 401 Unauthorized

---

## Configuration

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

## Error Handling

- **401 Unauthorized**: Invalid or missing JWT token
- **503 Service Unavailable**: Target service not found
- **500 Internal Server Error**: Gateway error

---

## See Also
- [Architecture Overview](01-architecture-overview.md)
- [Auth Service](03-auth-service.md)
