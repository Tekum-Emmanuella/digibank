# DigiBank — DevSecOps Core Banking System (Modular Monolith)

[![Build & Verification Pipeline](https://github.com/organization/digibank/actions/workflows/digibank-ci.yml/badge.svg)](https://github.com/organization/digibank/actions/workflows/digibank-ci.yml)
[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED.svg)](https://www.docker.com/)

DigiBank is an enterprise-grade core banking application built with Java 17 and Spring Boot 3 using a **Modular Monolith** architecture. Designed according to the **"Shift Left" DevSecOps philosophy**, the project emphasizes clean architectural boundaries, automated database migration, rigorous multi-tier testing (unit, integration, and Cucumber BDD), containerization, and continuous integration.

---

## 1. Architectural Overview

The application is structured as a Maven multi-module project to enforce domain decoupling, separation of concerns, and maintainability:

```text
digibank-parent/
├── pom.xml                     # Parent POM (dependency management & plugin orchestration)
├── common-module/              # Shared DTOs, custom business exceptions, API wrappers
├── customer-module/            # Customer domain: entities, repositories, services, controllers
├── account-module/             # Account management: balance calculations, debits, credits
├── transfer-module/            # Transaction domain: fund transfers, validations, ledger history
└── digibank-web/               # Aggregator application: Spring Boot main class, Thymeleaf UI,
                                # Flyway migrations, global exception advice, test harness
```

### Module Dependency Flow
```
               ┌────────────────┐
               │ digibank-web   │ (Aggregator / Runner / UI)
               └───────┬────────┘
                       │
      ┌────────────────┼────────────────┐
      ▼                ▼                ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│customer-mod. │ │ account-mod. │ │transfer-mod. │
└──────┬───────┘ └──────┬───────┘ └──────┬───────┘
       │                │                │
       └────────────────┼────────────────┘
                        ▼
               ┌────────────────┐
               │ common-module  │ (Base DTOs, ApiResponse, Exceptions)
               └────────────────┘
```

---

## 2. Prerequisites & Tooling

Ensure the following runtimes and tools are installed locally:

* **Java Development Kit (JDK)**: OpenJDK / Eclipse Temurin 17 or higher
* **Apache Maven**: Version 3.9+ (or use the provided `./mvnw` wrapper)
* **Docker Engine & Docker Compose**: Docker 24+ and Compose v2
* **Git**: Version 2.35+
* **PostgreSQL (Optional for local testing without Docker)**: Version 16+

---

## 3. Configuration & Profiles

DigiBank provides environment-specific profiles:

| Profile | Target Environment | Database | DDL Strategy | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| `dev` | Local development | PostgreSQL (`localhost:5432`) | `validate` (Flyway managed) | Local development with live DB |
| `test` | Unit & Integration tests | In-Memory H2 | `create-drop` / `validate` | Fast, isolated CI/automated tests |
| `docker` | Containerized runtime | PostgreSQL (`postgres:5432`) | `validate` (Flyway managed) | Production-like container cluster |

Key properties configured in `digibank-web/src/main/resources/application.yml`:
* **Server Port**: `8080`
* **Swagger/OpenAPI UI**: `/swagger-ui.html`
* **Flyway Migrations**: Enabled (`locations: classpath:db/migration`)
* **Security & Error Hardening**:
  * `server.error.include-stacktrace: never`
  * `server.error.include-binding-errors: never`

---

## 4. Maven Lifecycle & Build Commands

Execute these commands from the repository root:

### Clean and Build Modules
```bash
mvn clean compile
```

### Run All Unit and Integration Tests
```bash
mvn test
```

### Full Verification (Includes Integration Tests and Cucumber BDD Scenarios)
```bash
mvn clean verify
```

### Package the Executable Spring Boot JAR
```bash
mvn clean package -DskipTests=false
```
*The packaged executable JAR will be generated at:*  
`digibank-web/target/digibank-web-1.0.0-SNAPSHOT.jar`

### Run Locally via Spring Boot Maven Plugin
```bash
mvn -pl digibank-web spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 5. Docker & Container Orchestration

DigiBank provides a multi-stage `Dockerfile` and a `docker-compose.yml` for zero-configuration, reproducible local deployment.

### 5.1 Dockerfile Architecture
Built using Eclipse Temurin 17 Alpine for a minimal container attack surface:
```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS runtime
WORKDIR /app
COPY digibank-web/target/digibank-web-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
```

### 5.2 Build & Run with Docker Compose
To build the application image and start both PostgreSQL and DigiBank:

```bash
# 1. Package the project JAR
mvn clean package -DskipTests

# 2. Build and run containers in detached mode
docker compose up --build -d

# 3. View live application logs
docker compose logs -f digibank-app

# 4. Stop and tear down containers
docker compose down -v
```

### 5.3 Service Connectivity
| Service | Internal Port | Host Port | Credentials / URL |
| :--- | :--- | :--- | :--- |
| **digibank-app** | `8080` | `8080` | `http://localhost:8080` |
| **postgres** | `5432` | `5432` | User: `digiuser` / Password: `digipassword` / DB: `digibank_db` |

---

## 6. Database Migrations (Flyway)

All relational tables and seed baseline records are automated via Flyway migrations located in `digibank-web/src/main/resources/db/migration/`:

* `V1__create_schema.sql`: Sets up `customers`, `accounts`, and `transfers` tables with indexes, foreign keys, and unique constraints.
* `V2__insert_seed_data.sql`: Seeds initial demo customers and accounts for instant testing.

*Hibernate DDL generation is disabled in favor of `validate` to ensure consistent migrations across environments.*

---

## 7. API Endpoints & Swagger Documentation

Once the application is running, access the interactive OpenAPI documentation:
* **Interactive Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **OpenAPI Specification**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
* **Web Landing Page**: [http://localhost:8080/](http://localhost:8080/)

### Core Endpoints Summary

| Domain | Method | Endpoint | Description |
| :--- | :--- | :--- | :--- |
| **Customer** | `POST` | `/api/customers` | Register new customer (`@Valid` payload) |
| **Customer** | `GET` | `/api/customers/{id}` | Fetch customer by ID |
| **Customer** | `GET` | `/api/customers` | Retrieve all customers |
| **Account** | `POST` | `/api/accounts` | Open account for customer (`CURRENT` / `SAVINGS`) |
| **Account** | `GET` | `/api/accounts/{id}` | Get account details and balance |
| **Account** | `GET` | `/api/accounts/customer/{customerId}` | List accounts for customer |
| **Transfer** | `POST` | `/api/transfers` | Execute transactional balance transfer |
| **Transfer** | `GET` | `/api/transfers/account/{accountId}` | List transfer history for account |

---

## 8. Automated Testing Suite

The testing framework incorporates three layers:

1. **Unit Tests (Mockito + JUnit 5)**: Tests business validation, uniqueness checks, and debit/credit isolation without a database.
2. **Integration Tests (`@SpringBootTest` + `MockMvc`)**: Verifies HTTP status codes, JSON serialization, and error advice handling using the `test` in-memory H2 profile.
3. **Behavior Driven Development (BDD Cucumber)**:
   * Feature files: `src/test/resources/features/customer_management.feature`, `src/test/resources/features/transfer_management.feature`
   * Executed automatically during `mvn test` / `mvn verify`.

---

## 9. DevSecOps CI Pipeline

The continuous integration pipeline is defined in `.github/workflows/digibank-ci.yml`:
* Triggers automatically on `push` and `pull_request` against `main` and `develop` branches.
* Performs:
  1. JDK 17 setup with Maven dependency caching.
  2. Multi-module compilation and unit test execution (`mvn test`).
  3. Integration and Cucumber BDD verification (`mvn verify`).
  4. Packaging and artifact verification (`digibank-web.jar`).
  5. Docker build verification ensuring container build reproducibility.

---

## 10. Submission Artifacts & Verification Proofs

Teacher-required verification artifacts and submission screenshots are cataloged in the [`docs/evidence/`](docs/evidence/) directory:

* `docs/evidence/01_mvn_verify_build_success.txt` & `.png`: Successful build log and test completion proof.
* `docs/evidence/02_cucumber_bdd_report.png`: Cucumber scenario execution evidence.
* `docs/evidence/03_docker_compose_up.png`: Multi-container startup and database connection logs.
* `docs/evidence/04_thymeleaf_home_page.png`: Browser view of root landing page (`/`).
* `docs/evidence/05_swagger_ui_documentation.png`: Interactive API documentation (`/swagger-ui.html`).
* `docs/evidence/06_flyway_schema_history.png`: Database table view of applied Flyway migrations.
* `docs/evidence/07_api_execution_proofs.png`: Postman/cURL proofs for customer onboarding, account creation, transfers, and 400 validation error handling.