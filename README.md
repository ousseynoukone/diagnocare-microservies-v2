# 🏥 Diagnocare – Intelligent Healthcare Microservices Platform

Diagnocare is a microservices-based healthcare platform built with Spring Boot, Apache Kafka, and PostgreSQL. It includes intelligent disease prediction based on symptoms, online consultations, user authentication, and more.

---

## 🚀 Architecture Overview

The project follows a modular microservices architecture using Kafka for asynchronous communication and PostgreSQL for persistent storage.

### 📦 Services

| Service Name              | Description |
|--------------------------|------------|
| diagnocare-db            | PostgreSQL database service |
| gateway-service          | API Gateway for routing requests |
| auth-service             | Handles authentication & authorization |
| registry-service         | Eureka server for service discovery |
| diagnocare-service       | Core business logic |
| kafka-broker             | Kafka broker |
| kafka-ui                 | Kafka monitoring UI |
| pgadmin4                 | PostgreSQL GUI |
| ml-prediction-service    | Machine learning prediction service |

---

## 🧠 Core Features

- Symptom Analysis & Disease Prediction  
- Appointment Scheduling  
- JWT Authentication  
- API Gateway routing  
- Kafka-based communication  
- PostgreSQL persistence  

---

## 🛠️ Technologies Used

- Java 17 / Spring Boot  
- Spring Cloud  
- Apache Kafka  
- PostgreSQL  
- Docker  

---

## 🔌 Ports Reference

| Service        | Port |
|----------------|------|
| Gateway        | 8765 |
| Eureka         | 8761 |
| Kafka UI       | 8083 |
| pgAdmin        | 8888 |
| ML Service     | 5000 |

---

## 🌐 Access URLs

- Gateway → http://localhost:8765  
- Eureka → http://localhost:8761  
- Kafka UI → http://localhost:8083  
- pgAdmin → http://localhost:8888  

---

## ⚙️ Environment Variables

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/diagnocare
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
JWT_SECRET=your-secret-key
