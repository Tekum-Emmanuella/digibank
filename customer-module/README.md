# Customer Module

## Overview

The Customer Module is a core domain component of the DigiBank application, responsible for managing customer data and operations. It follows Spring Boot clean architecture principles with clear separation of concerns across controller, service, and repository layers.

## Domain Model

### Customer Entity

The `Customer` entity represents a bank customer with the following attributes:

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | Primary Key, Auto-generated | Unique identifier |
| fullName | String | Not null, max 120 chars | Customer's full name |
| email | String | Not null, unique, max 150 chars | Customer's email address |
| phoneNumber | String | Not null, unique, max 30 chars | Customer's phone number |
| nationalId | String | Not null, max 30 chars | Customer's national ID |
| createdAt | LocalDateTime | Not null | Record creation timestamp |

## REST API Endpoints

Base URL: `/api/customers`

### Create Customer

**Endpoint:** `POST /api/customers`

Creates a new customer in the system.

**Request Body:** `CustomerRequest`

```json
{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "nationalId": "ID123456"
}
```

**Validation Rules:**
- `fullName`: Required, maximum 120 characters
- `email`: Required, must be valid email format
- `phoneNumber`: Required, 9 to 30 characters (max 30 characters)
- `nationalId`: Required, maximum 30 characters

**Response:** `201 Created`

```json
{
  "success": true,
  "message": "Customer created successfully",
  "data": {
    "id": 1,
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "nationalId": "ID123456",
    "createdAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Cases:**
- `409 Conflict` - Email already exists
- `409 Conflict` - Phone number already exists
- `400 Bad Request` - Validation errors

### Get Customer by ID

**Endpoint:** `GET /api/customers/{id}`

Retrieves a customer by their unique identifier.

**Path Parameters:**
- `id` (Long) - Customer ID

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Customer retrieved successfully",
  "data": {
    "id": 1,
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "nationalId": "ID123456",
    "createdAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Cases:**
- `404 Not Found` - Customer not found

### Get All Customers

**Endpoint:** `GET /api/customers`

Retrieves a list of all customers.

**Response:** `200 OK`

```json
{
  "success": true,
  "message": "Customers retrieved successfully",
  "data": [
    {
      "id": 1,
      "fullName": "John Doe",
      "email": "john.doe@example.com",
      "phoneNumber": "+1234567890",
      "nationalId": "ID123456",
      "createdAt": "2024-01-15T10:30:00"
    }
  ],
  "timestamp": "2024-01-15T10:30:00"
}
```

## Response Structure

All API responses are wrapped in a standardized `ApiResponse<T>` envelope:

| Field | Type | Description |
|-------|------|-------------|
| success | boolean | Indicates if the operation was successful |
| message | String | Human-readable status message |
| data | T | Response payload (varies by endpoint) |
| timestamp | LocalDateTime | Response generation timestamp |

## Architecture

```
customer-module/
├── src/
│   ├── main/
│   │   └── java/com/m2ibank/customer/
│   │       ├── controller/
│   │       │   └── CustomerController.java    # REST API endpoints
│   │       ├── dto/
│   │       │   ├── CustomerRequest.java       # Request DTO
│   │       │   └── CustomerResponse.java      # Response DTO
│   │       ├── entity/
│   │       │   └── Customer.java              # JPA Entity
│   │       ├── repository/
│   │       │   └── CustomerRepository.java    # Data access layer
│   │       └── service/
│   │           └── CustomerService.java       # Business logic
│   └── test/
│       └── java/com/m2ibank/customer/
│           └── service/
│               └── CustomerServiceTest.java   # Unit tests
└── pom.xml
```

## Business Logic

### Customer Creation
1. Validate email uniqueness (throws `BusinessException` if exists)
2. Validate phone number uniqueness (throws `BusinessException` if exists)
3. Create and persist new `Customer` entity
4. Return mapped `CustomerResponse`

### Customer Retrieval
- `getCustomerById`: Returns single customer or throws `ResourceNotFoundException`
- `getAllCustomers`: Returns list of all customers

### Exceptions

| Exception | HTTP Status | Scenario |
|-----------|-------------|----------|
| `BusinessException` | 409 Conflict | Duplicate email/phone |
| `ResourceNotFoundException` | 404 Not Found | Customer not found |
| `MethodArgumentNotValidException` | 400 Bad Request | Validation failures |

## Testing

### Unit Test Suite

The `CustomerServiceTest` provides 100% isolated unit tests using Mockito:

| Test Method | Description |
|-------------|-------------|
| `createCustomer_Success` | Verifies successful creation with proper DTO mapping |
| `createCustomer_DuplicateEmail_ThrowsException` | Validates email uniqueness constraint |
| `createCustomer_DuplicatePhoneNumber_ThrowsException` | Validates phone uniqueness constraint |
| `getCustomerById_NotFound_ThrowsException` | Validates exception on missing customer |
| `getAllCustomers_Success` | Verifies list retrieval and mapping |

### Running Tests

Execute unit tests with Maven:

```bash
# Run all tests in customer-module
mvn test -pl customer-module

# Run specific test class
mvn test -pl customer-module -Dtest=CustomerServiceTest

# Run with verbose output
mvn test -pl customer-module -Dtest=CustomerServiceTest -X
```

Test reports are generated in:
```
customer-module/target/surefire-reports/
```

## Dependencies

```xml
<dependencies>
    <!-- Internal -->
    <dependency>
        <groupId>com.m2ibank</groupId>
        <artifactId>common-module</artifactId>
    </dependency>
    
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Shift-Left Testing Practices

- **Unit Tests**: All service methods covered with Mockito-isolated tests
- **Validation**: Input validation at DTO level using Jakarta Validation
- **Exception Handling**: Domain-specific exceptions with appropriate HTTP status codes
- **Clean Architecture**: Clear separation between controller, service, and repository layers

## Related Modules

- `common-module` - Shared DTOs and exceptions (`ApiResponse`, `BusinessException`, `ResourceNotFoundException`)
- `account-module` - Customer accounts management
- `transfer-module` - Fund transfers between accounts
