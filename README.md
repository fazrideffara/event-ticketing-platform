# Event Ticketing Platform

[![Java Version](https://img.shields.io/badge/Java-21%20LTS-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-dc382d.svg)](https://redis.io/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft-red.svg)](https://kafka.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)]()

A distributed event ticketing and booking platform built with Spring Boot, PostgreSQL, Redis, and Apache Kafka. The system handles concert ticket booking, payment processing with idempotency protection, and automated e-ticket issuance with QR code generation and email notification.

---

## Tech Stack

- **Backend Framework:** Java 21, Spring Boot 3.3.5 (Spring Web, Spring Security, Spring Data JPA, Spring Kafka, Spring Mail)
- **Database:** PostgreSQL 16
- **Caching & Idempotency:** Redis 7
- **Message Broker:** Apache Kafka (KRaft mode)
- **QR Code Engine:** ZXing 3.5.3
- **API Documentation:** SpringDoc OpenAPI 2.6.0 (Swagger UI)
- **Containerization:** Docker & Docker Compose

---

## Services Overview

The project is structured as a Maven multi-module system consisting of two microservices:

1. **`ticket-order-service` (Port 8081):**
   - User authentication and authorization (JWT stateless).
   - Event catalog and ticket tier management with Redis caching.
   - Order booking and payment processing with pessimistic locking (`FOR UPDATE`) and Redis idempotency keys (`SETNX`).
   - Financial reporting and analytics.
   - Audit trail logging.

2. **`eticket-notification-service` (Port 8082):**
   - Asynchronous Kafka consumer listening to `ticket-orders.paid`.
   - Dynamic QR code generation with error correction (Level H).
   - Redis-backed e-ticket storage.
   - Automated responsive HTML email dispatch with QR code attachments.
   - Entrance gate check-in and validation endpoint.

---

## Getting Started

### Prerequisites

- **Java Development Kit (JDK) 21**
- **Apache Maven 3.9+**
- **Docker & Docker Compose** (optional, recommended for quick start)

---

### Method 1: Run with Docker Compose (Recommended)

To run the entire ecosystem (PostgreSQL, Redis, Kafka, and both services) with a single command:

```bash
docker compose up -d
```

To stop all running services:
```bash
docker compose down
```

---

### Method 2: Run Locally with Maven

1. **Start dependencies** (PostgreSQL, Redis, Apache Kafka).
   - Ensure a database named `ticket_db` exists on PostgreSQL (`localhost:5432`).
   - Run the initial migration script located at `database/init.sql`.

2. **Build the project:**
   ```bash
   mvn clean package -DskipTests
   ```

3. **Start the services:**
   - In terminal 1:
     ```bash
     cd ticket-order-service
     mvn spring-boot:run
     ```
   - In terminal 2:
     ```bash
     cd eticket-notification-service
     mvn spring-boot:run
     ```

---

## API Documentation (Swagger UI)

Interactive Swagger UI endpoints are available once the services are up:

- **Ticket Order Service:** [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- **ETicket Notification Service:** [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)

### Default Seeded Users
- **Admin:** `admin` / `AdminPass123!`
- **User:** `fazri` / `Password123!`

---

## Quick API Test Flow

1. **Login & Get JWT Token:**
   ```bash
   curl -X POST http://localhost:8081/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "fazri", "password": "Password123!"}'
   ```

2. **Browse Events:**
   ```bash
   curl -X GET http://localhost:8081/api/v1/events \
     -H "Authorization: Bearer <TOKEN>"
   ```

3. **Book Ticket (Create Order):**
   ```bash
   curl -X POST http://localhost:8081/api/v1/orders \
     -H "Authorization: Bearer <TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"eventId": "<EVENT_ID>", "tierId": "<TIER_ID>", "quantity": 1}'
   ```

4. **Pay Order (with Idempotency Key):**
   ```bash
   curl -X POST http://localhost:8081/api/v1/payments \
     -H "Authorization: Bearer <TOKEN>" \
     -H "X-Idempotency-Key: e827d091-8975-472d-8f24-2c1b2f7041a0" \
     -H "Content-Type: application/json" \
     -d '{"orderId": "<ORDER_ID>", "paymentMethod": "VIRTUAL_ACCOUNT"}'
   ```

5. **View Issued E-Ticket & QR Pass:**
   - **JSON Details:** `GET http://localhost:8082/api/v1/etickets/order/<ORDER_NUMBER>`
   - **Render HTML Pass in Browser:** `http://localhost:8082/api/v1/etickets/render/<TICKET_CODE>`
   - **Gate Verification Check-in:** `POST http://localhost:8082/api/v1/etickets/verify/<TICKET_CODE>`

---

## License

This project is licensed under the MIT License.
