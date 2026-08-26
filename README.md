# digibank# DigiBank - Modular Monolithic Core Banking System

DigiBank is an educational core banking platform designed and built following DevSecOps and Shift-Left principles for course UCC152-2.

---

## 🏛 Architecture Overview

The project is designed as a **Modular Monolith** organized into Maven sub-modules:

* `digibank-parent`: Root POM managing global versions and dependencies.
* `digibank-web`: Application assembly, main entry point, Thymeleaf landing page, OpenAPI/Swagger config, and global exception handling.
* `common-module`: Shared generic responses (`ApiResponse<T>`), common exceptions, and utility classes.
* `customer-module`: Customer lifecycle, registration, and uniqueness validation.
* `account-module`: Account management, unique account number generation, and balance debit/credit controls.
* `transfer-module`: Fund transfers with transactional integrity, self-transfer blocking, and audit history.

---

## 🛠 Prerequisites

* **Java JDK**: Version 17
* **Apache Maven**: Version 3.9+
* **PostgreSQL**: Version 15 or 16
* **Docker & Docker Compose**: Recent stable versions

---

## 🚀 Quick Start & Build

### 1. Build and Test Entire Project
```bash
mvn clean install