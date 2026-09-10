# Workshop 3: Dynamic Security Analysis (DAST) Findings

## 1. Scope & Execution Target
- **Target URL**: `http://localhost:8080`
- **Environment Profile**: `dev`
- **Testing Tools**: Postman, Newman, cURL, OWASP ZAP

---

## 2. Dynamic Vulnerabilities Identified at Runtime

### Finding 1: Unrestricted OpenAPI & Swagger Documentation Exposure
- **Endpoint**: `GET /swagger-ui.html`, `GET /swagger-ui/index.html`
- **Observable Behavior**: Swagger UI is accessible with HTTP 200/302.
- **Risk / Impact**: Exposes all endpoints, schema structures, and business operations to external observers.
- **Remediation**: Restrict Swagger and OpenAPI documentation strictly to the `dev` profile; disable globally in `application.yml`.

### Finding 2: Missing Security HTTP Response Headers
- **Affected Surface**: All HTTP responses across `/api/*`.
- **Observable Behavior**: Responses lack standard security headers (`X-Content-Type-Options`, `X-Frame-Options`, `Content-Security-Policy`).
- **Risk / Impact**: Leaves clients vulnerable to MIME-sniffing, clickjacking, or cross-site content injection.
- **Remediation**: Add standard security response headers in the web filter configuration.

### Finding 3: Missing Defensive Precondition Guards in Service Layer
- **Endpoint**: `POST /api/transfers`
- **Observable Behavior**: Rejection of same-account or invalid amount transactions must be enforced defensively at the service boundary.
- **Risk / Impact**: Bypassing DTO validation allows illegal financial operations.
- **Remediation**: Ensure `TransferService` explicitly validates preconditions and produces standardized, sanitized business error responses.
