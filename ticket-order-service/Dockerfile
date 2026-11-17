# Stage 1: Build JAR using Maven
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent and module pom files
COPY pom.xml ./
COPY ticket-order-service/pom.xml ./ticket-order-service/
COPY eticket-notification-service/pom.xml ./eticket-notification-service/

# Download dependencies
RUN mvn -B dependency:go-offline -f ticket-order-service/pom.xml || true

# Copy source code and build
COPY ticket-order-service/src ./ticket-order-service/src
RUN mvn -B clean package -DskipTests -f ticket-order-service/pom.xml

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add non-root user for security best practice
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/ticket-order-service/target/ticket-order-service-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
