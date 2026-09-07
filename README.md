# DigiBank — Digital Core Banking System

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED.svg)](https://www.docker.com/)

DigiBank is a production-ready digital banking application built with **Java 17** and **Spring Boot 3** using a **Modular Monolith** architecture. It provides core banking services including customer management, account operations, and secure fund transfers with transactional integrity.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Technology Stack](#technology-stack)
3. [Module Structure](#module-structure)
4. [Prerequisites](#prerequisites)
5. [Quick Start](#quick-start)
6. [Detailed Setup Instructions](#detailed-setup-instructions)
7. [Configuration & Profiles](#configuration--profiles)
8. [Running the Application](#running-the-application)
9. [Testing Guide](#testing-guide)
10. [API Documentation](#api-documentation)
11. [Database Schema](#database-schema)
12. [Troubleshooting](#troubleshooting)

---

## Architecture Overview

DigiBank follows a **Modular Monolith** architecture pattern that enforces domain boundaries while maintaining operational simplicity:

```
┌─────────────────────────────────────────────────────────────┐
│                    digibank-web                              │
│              (Aggregator / Entry Point)                      │
│  - DigiBankApplication (Spring Boot Main)                   │
│  - GlobalExceptionHandler (@RestControllerAdvice)          │
│  - DataInitializer (CommandLineRunner)                     │
│  - OpenApiConfig (Swagger/OpenAPI)                         │
│  - HomeController + Thymeleaf UI                          │
└──────────────────────┬────────────────────────────────────┘
                       │ depends on
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│   customer   │ │   account    │ │   transfer   │
│   -module    │ │   -module    │ │   -module    │
├──────────────┤ ├──────────────┤ ├──────────────┤
│ • Customer   │ │ • Account    │ │ • Transfer   │
│   Entity     │ │   Entity     │ │   Entity     │
│ • Customer   │ │ • Account    │ │ • Transfer   │
│   Service    │ │   Service    │ │   Service    │
│ • Customer   │ │ • Account    │ │ • Transfer   │
│   Controller │ │   Controller │ │   Controller │
└──────┬───────┘ └──────┬───────┘ └──────┬───────┘
       │                │                │
       └────────────────┼────────────────┘
                        ▼
              ┌────────────────┐
              │  common-module │
              ├────────────────┤
              │ • ApiResponse │
              │ • BusinessExc  │
              │ • ResourceNot  │
              └────────────────┘
```

---

## Technology Stack

| Layer | Technology | Version |
|-------|------------|---------|
| **Language** | Java | 17 (LTS) |
| **Framework** | Spring Boot | 3.3.x |
| **Data Access** | Spring Data JPA | 3.3.x |
| **Database** | PostgreSQL | 16 |
| **Test DB** | H2 Database | 2.x |
| **Build Tool** | Apache Maven | 3.9+ |
| **Documentation** | SpringDoc OpenAPI | 2.x |
| **Template Engine** | Thymeleaf | 3.x |
| **Containerization** | Docker & Docker Compose | 24+ |

---

## Module Structure

```
digibank/
├── pom.xml                                    # Parent POM (dependency management)
├── README.md                                  # This file
├── docker-compose.yml                         # Docker orchestration
│
├── common-module/
│   └── src/main/java/com/m2ibank/common/
│       ├── api/ApiResponse.java              # Unified REST response wrapper
│       └── exception/
│           ├── BusinessException.java        # Business rule violations
│           └── ResourceNotFoundException.java  # 404 Not Found
│
├── customer-module/
│   └── src/main/java/com/m2ibank/customer/
│       ├── entity/Customer.java              # JPA Entity
│       ├── dto/CustomerRequest.java            # Validation constraints
│       ├── dto/CustomerResponse.java           # Response payload
│       ├── repository/CustomerRepository.java
│       ├── service/CustomerService.java
│       └── controller/CustomerController.java
│
├── account-module/
│   └── src/main/java/com/m2ibank/account/
│       ├── entity/Account.java                 # JPA Entity
│       ├── entity/AccountType.java           # Enum: CURRENT, SAVINGS
│       ├── dto/AccountRequest.java
│       ├── dto/AccountResponse.java
│       ├── repository/AccountRepository.java
│       ├── service/AccountService.java         # debit/credit logic
│       └── controller/AccountController.java
│
├── transfer-module/
│   └── src/main/java/com/m2ibank/transfer/
│       ├── entity/Transfer.java                # JPA Entity
│       ├── dto/TransferRequest.java            # @Valid constraints
│       ├── dto/TransferResponse.java
│       ├── repository/TransferRepository.java
│       ├── service/TransferService.java        # @Transactional transfers
│       └── controller/TransferController.java
│
└── digibank-web/
    └── src/
        ├── main/
        │   ├── java/com/m2ibank/web/
        │   │   ├── DigiBankApplication.java       # @SpringBootApplication
        │   │   ├── config/OpenApiConfig.java    # Swagger config
        │   │   ├── controller/HomeController.java
        │   │   ├── exception/GlobalExceptionHandler.java
        │   │   └── bootstrap/DataInitializer.java
        │   └── resources/
        │       ├── application.yml              # Main config
        │       ├── application-dev.yml          # Dev profile
        │       ├── application-test.yml         # Test profile
        │       └── templates/index.html           # Thymeleaf
        └── test/
            └── java/com/m2ibank/web/
                └── controller/HomeControllerTest.java
```

---

## Prerequisites

### Required Software

1. **Java Development Kit (JDK) 17**
   ```bash
   # Verify installation
   java -version
   # Expected: openjdk version "17.0.x"
   ```

2. **Apache Maven 3.9+**
   ```bash
   # Verify installation
   mvn -version
   # Expected: Apache Maven 3.9.x
   ```

3. **Docker Engine 24+ and Docker Compose**
   ```bash
   # Verify installation
   docker --version
   docker compose version
   ```

4. **PostgreSQL 16** (optional - can use Docker)
   ```bash
   # Verify installation (if not using Docker)
   psql --version
   ```

5. **Git**
   ```bash
   # Verify installation
   git --version
   ```

### System Requirements

- **RAM**: Minimum 4GB (8GB recommended for Docker)
- **Disk**: 2GB free space
- **Ports**: 8080 (application), 5432 or 5433 (PostgreSQL)

---

## Quick Start

For the impatient - get running in 5 minutes:

```bash
# 1. Clone the repository
git clone <repository-url>
cd digibank

# 2. Start PostgreSQL with Docker
docker run -d --name digibank-pg \
  -e POSTGRES_DB=digibankdb \
  -e POSTGRES_USER=digibank \
  -e POSTGRES_PASSWORD=digibank123 \
  -p 5433:5432 \
  postgres:16-alpine

# 3. Build the application
mvn clean install -DskipTests

# 4. Run the application
export SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5433/digibankdb'
mvn -pl digibank-web spring-boot:run

# 5. Access the application
# Web UI: http://localhost:8080
# API Docs: http://localhost:8080/swagger-ui.html
# Health: http://localhost:8080/api/customers
```

---

## Detailed Setup Instructions

### Step 1: Database Setup

#### Option A: Using Docker (Recommended)

```bash
# Pull and run PostgreSQL 16
docker pull postgres:16-alpine

# Run container
docker run -d \
  --name digibank-pg \
  -e POSTGRES_DB=digibankdb \
  -e POSTGRES_USER=digibank \
  -e POSTGRES_PASSWORD=digibank123 \
  -p 5433:5432 \
  --health-cmd="pg_isready -U digibank -d digibankdb" \
  --health-interval=5s \
  postgres:16-alpine

# Verify container is running
docker ps --filter name=digibank-pg

# Check database connectivity
docker exec digibank-pg pg_isready -U digibank -d digibankdb

# View logs
docker logs digibank-pg
```

#### Option B: Using Local PostgreSQL

```bash
# Create database
psql -U postgres -c "CREATE DATABASE digibankdb;"

# Create user
psql -U postgres -c "CREATE USER digibank WITH PASSWORD 'digibank123';"

# Grant privileges
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE digibankdb TO digibank;"
```

### Step 2: Build the Application

```bash
# Navigate to project root
cd digibank

# Clean and compile
mvn clean compile

# Run all tests
mvn test

# Package application
mvn clean package -DskipTests

# Full build with tests
mvn clean verify
```

### Step 3: Verify Build Artifacts

```bash
# Check JAR was created
ls -lh digibank-web/target/digibank-web-*.jar

# Expected output:
# digibank-web/target/digibank-web-1.0.0-SNAPSHOT.jar
```

---

## Configuration & Profiles

### Profile Overview

| Profile | Database | DDL Auto | Use Case |
|---------|----------|----------|----------|
| `dev` (default) | PostgreSQL | `update` | Local development |
| `test` | H2 In-Memory | `create-drop` | Automated testing |

### Configuration Files

**Main Configuration** (`digibank-web/src/main/resources/application.yml`):
```yaml
spring:
  profiles:
    active: dev  # Default profile
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    open-in-view: false
  
  thymeleaf:
    cache: false

server:
  port: 8080
  error:
    include-message: never
    include-binding-errors: never
    include-stacktrace: never
```

**Development Profile** (`application-dev.yml`):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/digibankdb
    username: digibank
    password: digibank123
    driver-class-name: org.postgresql.Driver
```

**Test Profile** (`application-test.yml`):
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:digibanktestdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
```

---

## Running the Application

### Method 1: Using Maven (Development)

```bash
# Set database URL environment variable
export SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5433/digibankdb'

# Run with dev profile (default)
mvn -pl digibank-web spring-boot:run

# Run with specific profile
mvn -pl digibank-web spring-boot:run -Dspring-boot.run.profiles=dev

# Run with JVM arguments
mvn -pl digibank-web spring-boot:run \
  -Dspring-boot.run.jvmArguments="-Dspring.datasource.url=jdbc:postgresql://localhost:5433/digibankdb"
```

### Method 2: Using Packaged JAR

```bash
# Build first
mvn clean package -DskipTests

# Run JAR
java -jar digibank-web/target/digibank-web-1.0.0-SNAPSHOT.jar

# Run with profile
java -jar digibank-web/target/digibank-web-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev
```

### Method 3: Using Docker Compose

```bash
# Build and run with Docker Compose
docker compose up --build -d

# View logs
docker compose logs -f digibank-app

# Stop services
docker compose down

# Stop and remove volumes
docker compose down -v
```

### Verification Steps

```bash
# 1. Check application is running
curl http://localhost:8080/

# 2. Check API health
curl http://localhost:8080/api/customers

# 3. Check Swagger UI
curl http://localhost:8080/swagger-ui.html

# 4. Check OpenAPI docs
curl http://localhost:8080/api-docs
```

---

## Testing Guide

### Running Unit Tests

```bash
# Run all unit tests
mvn test

# Run tests for specific module
mvn -pl customer-module test
mvn -pl account-module test
mvn -pl transfer-module test

# Run with verbose output
mvn test -X

# Run specific test class
mvn test -Dtest=CustomerServiceTest
mvn test -Dtest=AccountServiceTest
mvn test -Dtest=TransferServiceTest
```

### Running Integration Tests

```bash
# Run integration tests (includes @SpringBootTest)
mvn verify

# Skip unit tests, run only integration tests
mvn verify -DskipUnitTests=true

# Run with test profile
mvn verify -Dspring.profiles.active=test
```

### Manual API Testing

#### Test 1: Create Customer

```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe",
    "email": "john@example.com",
    "phoneNumber": "+1234567890",
    "nationalId": "ID123456"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Customer created successfully",
  "data": {
    "id": 3,
    "fullName": "John Doe",
    "email": "john@example.com",
    "phoneNumber": "+1234567890",
    "nationalId": "ID123456",
    "createdAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

#### Test 2: Create Account

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "accountType": "CURRENT",
    "initialBalance": 50000.00
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Account created successfully",
  "data": {
    "id": 3,
    "accountNumber": "DB-A3F7B2C1",
    "balance": 50000.00,
    "accountType": "CURRENT",
    "customerId": 1,
    "createdAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

#### Test 3: Execute Transfer

```bash
curl -X POST http://localhost:8080/api/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "sourceAccountId": 1,
    "destinationAccountId": 2,
    "amount": 5000.00,
    "description": "Test transfer"
  }'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Transfer executed successfully",
  "data": {
    "id": 1,
    "sourceAccountId": 1,
    "destinationAccountId": 2,
    "amount": 5000.00,
    "description": "Test transfer",
    "createdAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

#### Test 4: Error Handling (Self-Transfer)

```bash
curl -X POST http://localhost:8080/api/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "sourceAccountId": 1,
    "destinationAccountId": 1,
    "amount": 100.00
  }'
```

**Expected Response (HTTP 400):**
```json
{
  "success": false,
  "message": "Source and destination accounts must be different",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

#### Test 5: Error Handling (Not Found)

```bash
curl http://localhost:8080/api/accounts/999
```

**Expected Response (HTTP 404):**
```json
{
  "success": false,
  "message": "Account not found with id: 999",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

#### Test 6: Validation Error

```bash
curl -X POST http://localhost:8080/api/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "sourceAccountId": 1,
    "destinationAccountId": 2,
    "amount": 0.00
  }'
```

**Expected Response (HTTP 400):**
```json
{
  "success": false,
  "message": "amount: Transfer amount must be greater than zero",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## API Documentation

### Interactive Documentation

Once the application is running:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api-docs

### Endpoint Summary

| Domain | Method | Endpoint | Description |
|--------|--------|----------|-------------|
| **Customer** | POST | `/api/customers` | Create customer |
| **Customer** | GET | `/api/customers/{id}` | Get customer by ID |
| **Customer** | GET | `/api/customers` | List all customers |
| **Account** | POST | `/api/accounts` | Create account |
| **Account** | GET | `/api/accounts/{id}` | Get account by ID |
| **Account** | GET | `/api/accounts/customer/{customerId}` | List customer accounts |
| **Transfer** | POST | `/api/transfers` | Execute transfer |
| **Transfer** | GET | `/api/transfers/account/{accountId}` | List account transfers |

---

## Database Schema

### Entity Relationship Diagram

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│    customers    │       │    accounts     │       │    transfers    │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id (PK)         │──┐    │ id (PK)         │       │ id (PK)         │
│ full_name       │  │    │ account_number  │       │ source_acct_id  │
│ email           │  └───>│ customer_id(FK) │<─────│ dest_acct_id(FK)│
│ phone_number    │       │ balance         │       │ amount          │
│ national_id     │       │ account_type    │       │ description     │
│ created_at      │       │ created_at    │       │ created_at      │
└─────────────────┘       └─────────────────┘       └─────────────────┘
```

### Demo Data

On startup, the application automatically seeds:

- **Alice Ndzi** (alice@m2ibank.com) with CURRENT account (150,000)
- **Brian Tchoumi** (brian@m2ibank.com) with SAVINGS account (90,000)

---

## Troubleshooting

### Issue: Port 5432 is already in use

**Solution:** Use a different port for PostgreSQL

```bash
# Use port 5433 instead
docker run -d --name digibank-pg \
  -e POSTGRES_DB=digibankdb \
  -e POSTGRES_USER=digibank \
  -e POSTGRES_PASSWORD=digibank123 \
  -p 5433:5432 \
  postgres:16-alpine

# Update application-dev.yml or set env variable
export SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5433/digibankdb'
```

### Issue: Application fails to connect to database

**Check:**
```bash
# Verify PostgreSQL is running
docker ps --filter name=digibank-pg

# Check connectivity
docker exec digibank-pg pg_isready -U digibank -d digibankdb

# View logs
docker logs digibank-pg
```

### Issue: Build fails with test errors

**Solution:** Skip tests temporarily
```bash
mvn clean package -DskipTests
```

Then run tests separately:
```bash
mvn test
```

### Issue: Demo data not appearing

**Check:** Database has old data
```bash
# Reset database
docker exec digibank-pg psql -U digibank -d digibankdb -c "TRUNCATE TABLE transfers, accounts, customers RESTART IDENTITY CASCADE;"

# Restart application
```

---

## License

This project is for educational purposes as part of the M2I Bank DevSecOps Workshop.

---

## Support

For issues or questions:
- Check the troubleshooting section above
- Review the API documentation at `/swagger-ui.html`
- Check application logs: `docker logs digibank-pg` or application logs
