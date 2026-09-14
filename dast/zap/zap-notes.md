# Workshop 3: Dynamic Security Analysis (DAST) Findings Log

## 1. Environment & Target
- **Target URL**: `http://localhost:8080`
- **Active Profile**: `dev`
- **Database**: PostgreSQL 16 (`digibankdb`)
- **Testing Methods**: cURL, Postman Collection, Newman Runner, OWASP ZAP

---

## 2. Dynamic Vulnerability Sheets

### Vulnerability 1: Unrestricted Exposure of Interactive Documentation
- **Endpoint**: `GET /swagger-ui.html`
- **Observation**: Swagger UI and OpenAPI documentation are freely reachable without access restriction.
- **Classification**: Information Exposure / Uncontrolled Attack Surface.
- **Impact**: Provides external actors with a complete map of exposed endpoints, request schemas, and parameter formats.
- **Remediation**: Restrict Swagger and OpenAPI documentation exclusively to the `dev` profile; disable globally in `application.yml`.

### Vulnerability 2: Absence of Standard HTTP Security Headers
- **Endpoint**: All routes (`/api/*`, `/`)
- **Observation**: HTTP responses lack standard protective headers (`X-Content-Type-Options`, `X-Frame-Options`, `Content-Security-Policy`).
- **Classification**: Missing Security Headers.
- **Impact**: Increases vulnerability to MIME-type sniffing, clickjacking, and cross-site framing.
- **Remediation**: Configure security response headers via an HTTP filter or Spring Security configuration.

### Vulnerability 3: Risk of Overly Descriptive Error Feedback
- **Endpoint**: `GET /api/customers/{id}`, `POST /api/transfers`
- **Observation**: Error responses return varying granular messages that can assist attackers in user and account enumeration.
- **Classification**: Improper Error Handling / Information Disclosure.
- **Impact**: Allows attackers to differentiate between nonexistent resources and existing business constraints.
- **Remediation**: Unify and sanitize error envelopes using a standardized `ApiError` format without leaking internal identifiers.
