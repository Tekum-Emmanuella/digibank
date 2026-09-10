# DigiBank Workshop 4 Container Review

## Analysis Metadata

- Analysis date: 2026-09-10
- Base branch: `feat/workshop-3-dynamic-security-analysis-of-digibank-at-runtime`
- Workshop 4 branch: `codex/workshop-4-from-workshop-3`
- Image name: `digibank:workshop4`
- Java baseline: 17
- Spring Boot baseline: 3.5.16

## Files Reviewed

- `pom.xml`
- `Dockerfile`
- `.dockerignore`
- `docker-compose.yml`
- `.github/workflows/digibank-security-pipeline.yml`
- `digibank-web/src/main/resources/application.yml`
- `digibank-web/src/main/resources/application-dev.yml`
- `digibank-web/src/main/resources/application-ci.yml`
- `.env.example`

## Findings And Remediation

| Area | Initial observation | Workshop 4 remediation |
| --- | --- | --- |
| Docker image size and build surface | The image expected a prebuilt jar from `digibank-web/target`. | Replaced with a multi-stage Dockerfile that builds inside a Maven stage and copies only the executable jar into the runtime image. |
| Runtime privileges | Runtime already used a non-root application user. | Preserved non-root execution and explicit ownership of the copied jar. |
| Build context | `.dockerignore` excluded only a small set of files. | Expanded exclusions for local env files, reports, DAST output, dependency reports, generated image metadata, documents, and all target directories. |
| Secrets in Compose | Compose contained literal database credentials. | Replaced with environment-variable placeholders and safe local defaults. |
| Container privileges | Compose did not enforce runtime hardening. | Added `no-new-privileges`, dropped app capabilities, read-only app filesystem, and tmpfs `/tmp`. |
| Dependency scan reliability | Dependency-Check was bound to ordinary Maven `verify`, which can fail when NVD credentials are absent. | Kept plugin configuration in the parent POM but skipped it by default; the security pipeline runs it explicitly when `NVD_API_KEY` is configured. |
| CI/CD traceability | Existing workflows covered build, quality, and DAST. | Added Workshop 4 security pipeline for dependency tree, Dependency-Check evidence, Docker image metadata/history, and Trivy scan output. |

## Verification Commands

```bash
mvn -B -ntp -Ddependency-check.skip=true clean verify
mvn -B -ntp -Ddependency-check.skip=true dependency:tree -DoutputFile=dependency-tree.txt
mvn -B -ntp -Ddependency-check.skip=false org.owasp:dependency-check-maven:check
docker build -t digibank:workshop4 .
docker image inspect digibank:workshop4 > docker-image-inspect.json
docker history digibank:workshop4 --no-trunc > docker-image-history.txt
trivy image --severity HIGH,CRITICAL --exit-code 1 digibank:workshop4
```

## Notes

- `NVD_API_KEY` must be stored as a GitHub repository secret before expecting complete OWASP Dependency-Check reports in CI.
- Real database credentials must be stored in a local `.env` file or deployment secret store, never committed.
- Swagger/OpenAPI remains disabled by default in `application.yml` and is only re-enabled for the development profile.

