# Multi-stage build for AgentOS services
# Stage 1 — Build with Maven
FROM maven:3.9-eclipse-temurin-21 AS builder

ARG SERVICE_DIR=model-gateway
WORKDIR /app
COPY $SERVICE_DIR/pom.xml .
RUN mvn dependency:go-offline -q || true
COPY $SERVICE_DIR/src ./src
RUN mvn package -Dmaven.test.skip=true -q

# Stage 2 — Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xmx512m -Xms256m"

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 9090
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]