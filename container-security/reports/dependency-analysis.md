# DigiBank Dependency Analysis

## Scope

This report supports Workshop 4 container and dependency hardening for the branch `codex/workshop-4-from-workshop-3`, created from `feat/workshop-3-dynamic-security-analysis-of-digibank-at-runtime`.

## Runtime Dependency Groups

| Area | Main dependencies | Security note |
| --- | --- | --- |
| Web/API runtime | Spring Boot Web, embedded Tomcat | Spring Boot is pinned at `3.5.16`; Tomcat is explicitly overridden to `10.1.59` to keep the embedded servlet container patched. |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL JDBC | PostgreSQL credentials are injected through environment variables and are no longer hardcoded in Compose. |
| Validation | Hibernate Validator, Jakarta Validation | Request DTO validation remains active to reduce malformed runtime input. |
| Logging | Spring Boot logging stack, Logback | Sensitive error handling from Workshop 3 is preserved; reports and logs are excluded from the container build context. |
| Testing only | JUnit, Mockito, Cucumber, H2 | Test/provided scopes are excluded from Dependency-Check enforcement because they are not shipped in the production runtime image. |

## Dependency-Check Configuration

The parent `pom.xml` centralizes OWASP Dependency-Check with:

- CVSS failure threshold set to `7`.
- Test and provided scopes skipped.
- NVD API key read from the `NVD_API_KEY` environment variable.
- Suppressions centralized in `dependency-check-suppressions.xml`.
- Normal Maven verification protected from external NVD availability by defaulting `dependency-check.skip` to `true`.

Security scans are still enforced explicitly by CI with `-Ddependency-check.skip=false` when `NVD_API_KEY` is configured.

## Container Dependency Boundary

The Dockerfile now uses a Maven builder image only during compilation. The final image is based on `eclipse-temurin:17-jre-alpine` and receives only `digibank-web-*.jar`, which prevents Maven, source files, test classes, Git metadata, and build reports from being shipped in the runtime layer.

## Evidence Commands

```bash
mvn -B -ntp -Ddependency-check.skip=true clean verify
mvn -B -ntp -Ddependency-check.skip=true dependency:tree -DoutputFile=dependency-tree.txt
mvn -B -ntp -Ddependency-check.skip=false org.owasp:dependency-check-maven:check
docker build -t digibank:workshop4 .
docker image inspect digibank:workshop4
docker history digibank:workshop4 --no-trunc
trivy image --severity HIGH,CRITICAL --exit-code 1 digibank:workshop4
```
