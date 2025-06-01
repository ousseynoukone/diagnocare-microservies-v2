# 🏥 Diagnocare – Intelligent Healthcare Microservices Platform

Diagnocare is a microservices-based healthcare platform built with **Spring Boot**, **Apache Kafka**, and **PostgreSQL**. It includes intelligent disease prediction based on symptoms, online consultations, user authentication, and more.

---

## 🚀 Architecture Overview

The project follows a modular **microservices architecture**, using **Kafka for asynchronous communication**, and **PostgreSQL for persistent storage**. Below are the core services:

| Service Name     | Description |
|------------------|-------------|
| `diagnocare-db`  | PostgreSQL database service for persistent storage |
| `gateway-service`| API Gateway for routing requests to backend services |
| `auth-service`   | Manages user authentication and authorization |
| `registry-service`| Eureka Server for service discovery |
| `diagnocare-service` | Main business logic: symptom analysis and disease prediction |
| `kafka-broker`   | Kafka broker for event-driven messaging |
| `kafka-ui`       | Web UI for monitoring Kafka topics and brokers |
| `pgadmin4`       | GUI for managing PostgreSQL |

---

## 🧠 Core Features

- 🩺 **Symptom Analysis & Disease Prediction**
- 📅 **Appointment Scheduling**
- 🔐 **User Authentication (JWT-based)**
- 🧭 **API Gateway with routing and load balancing**
- 📨 **Asynchronous communication via Apache Kafka**
- 💾 **Data persistence with PostgreSQL**
- 📡 **Service discovery using Eureka**
- 🛠 **Centralized Kafka UI for topic monitoring**

---

## 🛠️ Technologies Used

- Java 17 / Spring Boot
- Spring Cloud (Eureka, Gateway)
- Spring Security + JWT
- Apache Kafka
- PostgreSQL
- Docker / Docker Compose
- pgAdmin4 / Kafka UI

---

## 🧪 Running the Project

### 🐳 Prerequisites

- Docker & Docker Compose installed
- Java 17+
- Maven

### ▶️ Start Services with Docker Compose

```bash
docker-compose up --build
