# Stage 1: Build JAR using Maven
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy parent and module pom files
COPY pom.xml ./
COPY ticket-order-service/pom.xml ./ticket-order-service/
COPY eticket-notification-service/pom.xml ./eticket-notification-service/

# Download dependencies
RUN mvn -B dependency:go-offline -f eticket-notification-service/pom.xml || true

# Copy source code and build
COPY eticket-notification-service/src ./eticket-notification-service/src
RUN mvn -B clean package -DskipTests -f eticket-notification-service/pom.xml

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add non-root user for security best practice
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/eticket-notification-service/target/eticket-notification-service-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
