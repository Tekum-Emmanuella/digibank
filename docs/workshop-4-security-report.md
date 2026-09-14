# DigiBank Workshop 4 - Containers, Dependencies, and Deployment Artifact Security

## Objective

This implementation secures DigiBank's dependencies, container image, runtime configuration, and CI/CD evidence chain on top of the Workshop 3 dynamic security branch.

## Implemented Controls

- Parent Maven Dependency-Check configuration retained with CVSS threshold 7, test/provided scopes skipped, OSS Index disabled, suppressions centralized, and `NVD_API_KEY` injected from the environment.
- Normal `mvn clean verify` no longer depends on live NVD availability; Dependency-Check is run explicitly by the Workshop 4 security workflow.
- Dockerfile converted to a multi-stage build so Maven, source files, and build caches are absent from the final runtime image.
- Runtime image keeps non-root execution through `appuser:appgroup`.
- `.dockerignore` now prevents local secrets, generated reports, DAST reports, target folders, documents, and image evidence files from entering the Docker build context.
- Docker Compose now injects credentials through environment variables, uses safe placeholders, and applies app-container hardening.
- GitHub Actions security pipeline now builds/tests the project, exports dependency tree evidence, runs Dependency-Check when `NVD_API_KEY` exists, builds the image, captures image metadata/history, runs Trivy, and uploads evidence artifacts.
- Structured review notes are available in `container-security/notes/container-review.md`.

## Expected Evidence

- `dependency-tree.txt`
- `dependency-check-report.html`
- `docker-image-inspect.json`
- `docker-image-history.txt`
- `trivy-image-report.txt`

## Local Validation Sequence

```bash
mvn -B -ntp -Ddependency-check.skip=true clean verify
mvn -B -ntp -Ddependency-check.skip=true dependency:tree -DoutputFile=dependency-tree.txt
docker build -t digibank:workshop4 .
docker image inspect digibank:workshop4 > docker-image-inspect.json
docker history digibank:workshop4 --no-trunc > docker-image-history.txt
```

Dependency-Check requires:

```bash
mvn -B -ntp -Ddependency-check.skip=false org.owasp:dependency-check-maven:check
```

Trivy requires:

```bash
trivy image --severity HIGH,CRITICAL --exit-code 1 digibank:workshop4
```

## Remaining Operational Requirement

Configure the GitHub repository secret `NVD_API_KEY` before relying on complete Dependency-Check reports in CI. This is an external prerequisite, not a code defect.

