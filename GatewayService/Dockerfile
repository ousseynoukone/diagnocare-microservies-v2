# Stage 1: Build the app
FROM maven:3.8.4-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the app
FROM openjdk:17.0.1-jdk-slim

WORKDIR /app
COPY --from=builder /app/target/*.jar /app/app.jar

# Expose the Eureka server port
EXPOSE 8765
CMD ["java", "-jar", "app.jar"]
