# DiagnoCare Technical Documentation - Index

## Complete Documentation Suite

This directory contains comprehensive technical documentation for the DiagnoCare microservices platform.

---

## 📚 Documentation Files

### 1. [Architecture Overview](01-architecture-overview.md)
**Complete system architecture documentation**
- Microservices overview
- Technology stack
- Communication patterns
- Infrastructure components
- Security architecture
- Deployment architecture

### 2. [Database Schema](02-database-schema.md)
**Complete database documentation**
- Auth database schema (all tables)
- DiagnoCare database schema (all tables)
- Entity relationships
- Encryption & security
- Indexes & constraints
- Data synchronization

### 3. [Auth Service](03-auth-service.md)
**Authentication service complete documentation**
- API endpoints
- Authentication flows
- JWT token system
- User management
- GDPR features
- Kafka integration
- Security implementation

### 4. [DiagnoCare Service](04-diagnocare-service.md)
**Main business service documentation**
- API endpoints
- Prediction workflow
- Check-in system
- Medical data management
- GDPR features
- Kafka integration

### 5. [Gateway Service](05-gateway-service.md)
**API Gateway documentation**
- Routing configuration
- Authentication filter
- Service discovery
- Request/response handling

### 6. [ML Prediction Service](06-ml-prediction-service.md)
**Machine Learning service documentation**
- Flask architecture
- Prediction endpoints
- Model loading and training (outside container)
- Confusion matrices and evaluation
- Translation service
- NLP extraction

### 7. [API Endpoints](07-api-endpoints.md)
**Complete API reference**
- All endpoints (Auth + DiagnoCare)
- Request/response formats
- Authentication requirements
- Error codes

### 8. [Workflows](08-workflows.md)
**Business process flows**
- User registration flow
- Prediction workflow
- Check-in workflow
- User deletion/anonymization
- Email verification flow

### 9. [Kafka Events](09-kafka-events.md)
**Event-driven architecture**
- Event types
- Event payloads
- Producers & consumers
- Event flow diagrams

### 10. [GDPR Implementation](10-gdpr-implementation.md)
**GDPR compliance features**
- Consent management
- Data encryption
- Data export
- Anonymization
- Privacy policy

### 11. [Deployment](11-deployment.md)
**Deployment guide**
- Docker Compose setup
- Environment variables
- Service dependencies
- Health checks
- Production considerations

### 12. [Configuration](12-configuration.md)
**Configuration reference**
- All application.properties
- Environment variables
- Secret management
- Feature flags

---

## 🚀 Quick Start

1. **New to the project?** Start with [Architecture Overview](01-architecture-overview.md)
2. **Setting up?** See [Deployment](11-deployment.md)
3. **Understanding data?** See [Database Schema](02-database-schema.md)
4. **API integration?** See [API Endpoints](07-api-endpoints.md)
5. **Business logic?** See [Workflows](08-workflows.md)

---

## 📋 Documentation Structure

```
technical-documentation/
├── 00-INDEX.md                    # This file
├── 01-architecture-overview.md   # System architecture
├── 02-database-schema.md         # Database documentation
├── 03-auth-service.md             # AuthService details
├── 04-diagnocare-service.md       # DiagnoCareService details
├── 05-gateway-service.md          # GatewayService details
├── 06-ml-prediction-service.md    # ML Service details
├── 07-api-endpoints.md            # API reference
├── 08-workflows.md                # Business flows
├── 09-kafka-events.md             # Event system
├── 10-gdpr-implementation.md      # GDPR features
├── 11-deployment.md               # Deployment guide
└── 12-configuration.md            # Configuration reference
```

---

## 🔍 Finding Information

### By Topic

**Architecture & Design**
- [Architecture Overview](01-architecture-overview.md)
- [Database Schema](02-database-schema.md)

**Services**
- [Auth Service](03-auth-service.md)
- [DiagnoCare Service](04-diagnocare-service.md)
- [Gateway Service](05-gateway-service.md)
- [ML Prediction Service](06-ml-prediction-service.md)

**Integration**
- [API Endpoints](07-api-endpoints.md)
- [Kafka Events](09-kafka-events.md)
- [Workflows](08-workflows.md)

**Operations**
- [Deployment](11-deployment.md)
- [Configuration](12-configuration.md)

**Compliance**
- [GDPR Implementation](10-gdpr-implementation.md)

### By Role

**Developer**
- Start: [Architecture Overview](01-architecture-overview.md)
- Then: [API Endpoints](07-api-endpoints.md), [Workflows](08-workflows.md)
- Reference: [Database Schema](02-database-schema.md), [Configuration](12-configuration.md)

**DevOps Engineer**
- Start: [Deployment](11-deployment.md)
- Then: [Configuration](12-configuration.md)
- Reference: [Architecture Overview](01-architecture-overview.md)

**Product Manager**
- Start: [Architecture Overview](01-architecture-overview.md)
- Then: [Workflows](08-workflows.md)
- Reference: [API Endpoints](07-api-endpoints.md)

**Security/Compliance**
- Start: [GDPR Implementation](10-gdpr-implementation.md)
- Then: [Auth Service](03-auth-service.md)
- Reference: [Database Schema](02-database-schema.md)

---

## 📝 Documentation Standards

### Code Examples
- All code examples use actual project code
- Java examples use Spring Boot 3.x syntax
- Python examples use Flask conventions

### Diagrams
- Mermaid diagrams for flow charts
- Entity relationship diagrams
- Sequence diagrams for interactions

### Format
- Markdown format
- Consistent structure
- Cross-references between documents
- Table of contents in each document

---

## 🔄 Keeping Documentation Updated

When making changes:
1. Update relevant documentation files
2. Update this index if structure changes
3. Update cross-references
4. Test code examples

---

## 📞 Support

For questions or updates:
- Review relevant documentation section
- Check code comments
- See inline documentation in source files

---

**Last Updated**: March 2025  
**Version**: 1.1  
**Maintained By**: Development Team
