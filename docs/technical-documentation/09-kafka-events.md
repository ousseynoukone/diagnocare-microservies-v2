# Kafka Events - Complete Documentation

## Overview

DiagnoCare uses Apache Kafka for asynchronous event-driven communication between services.

**Mode**: KRaft (no Zookeeper)  
**Port**: 29092 (external), 9092 (internal)  
**Topics**: USER_REGISTERED, USER_UPDATE, USER_DELETED

---

## Event Types

### 1. USER_REGISTERED

**Topic**: `USER_REGISTERED`  
**Producer**: AuthService  
**Consumer**: DiagnoCareService (UserSyncConsumer)  
**Trigger**: User registration

**Payload**:
```json
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "lang": "en",
  "active": true
}
```

**Consumer Action**: Create user in DiagnoCare database

---

### 2. USER_UPDATE

**Topic**: `USER_UPDATE`  
**Producer**: AuthService  
**Consumer**: DiagnoCareService (UserSyncConsumer)  
**Trigger**: User profile update

**Payload**: Same as USER_REGISTERED

**Consumer Action**: Update user in DiagnoCare database

---

### 3. USER_DELETED

**Topic**: `USER_DELETED`  
**Producer**: AuthService  
**Consumer**: DiagnoCareService (UserSyncConsumer)  
**Trigger**: User account deletion

**Payload**: Same as USER_REGISTERED

**Consumer Action**: Anonymize user data (PII removed, health data preserved)

---

## Event Flow

### Producer (AuthService)

```java
@Service
public class KafkaProducer {
    public void sendMessage(String topic, String key, Object payload) {
        String jsonData = objectMapper.writeValueAsString(payload);
        kafkaTemplate.send(topic, key, jsonData);
    }
}
```

**Usage**:
```java
kafkaProducer.sendMessage(
    KafkaEvent.USER_REGISTERED.toString(),
    user.getId().toString(),
    userSyncEventDTO
);
```

### Consumer (DiagnoCareService)

```java
@KafkaListener(topics = {"USER_REGISTERED", "USER_UPDATE", "USER_DELETED"}, 
               groupId = "diagnocare-user-sync")
public void handleUserSync(ConsumerRecord<String, String> record) {
    UserSyncEventDTO event = objectMapper.readValue(record.value(), UserSyncEventDTO.class);
    
    if ("USER_DELETED".equals(record.topic())) {
        userDataAnonymizationService.anonymizeUserData(event.getId());
    } else {
        // Sync user data
        User user = userRepository.findById(event.getId()).orElse(new User());
        user.setEmail(event.getEmail());
        // ... update fields
        userRepository.save(user);
    }
}
```

---

## Event Payload Structure

### UserSyncEventDTO

```java
public class UserSyncEventDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String lang;
    private Boolean active;
}
```

---

## Error Handling

### Producer
- Logs errors but doesn't throw exceptions
- Prevents blocking user operations

### Consumer
- Logs errors but doesn't throw exceptions
- Prevents Kafka consumer from crashing
- Retries via Kafka consumer configuration

---

## Configuration

### AuthService (Producer)

```properties
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:29092}
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
```

### DiagnoCareService (Consumer)

```properties
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:29092}
spring.kafka.consumer.group-id=diagnocare-user-sync
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

---

## Monitoring

**Kafka UI**: http://localhost:8083

- View topics
- Monitor consumer groups
- View messages
- Check lag

---

## See Also
- [Architecture Overview](01-architecture-overview.md)
- [Auth Service](03-auth-service.md)
- [DiagnoCare Service](04-diagnocare-service.md)
