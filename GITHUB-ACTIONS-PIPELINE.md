# GitHub Actions CI/CD Pipeline - DigiBank Quality Pipeline

## Overview

The `.github/workflows/digibank-quality.yml` file implements the automated SAST (Static Application Security Testing) pipeline required by Workshop 2 (§2.17). This workflow runs on every push and pull request to `main`, `master`, or `develop` branches, automatically performing:

1. ✅ Build & Testing
2. ✅ OWASP Dependency-Check scanning
3. ✅ PITest mutation testing
4. ✅ SonarQube analysis (optional)

## Workflow Triggers

- **On Push**: Automatic execution when code is pushed to `main`, `master`, or `develop`
- **On Pull Request**: Automatic execution on PR creation or updates targeting these branches

## Pipeline Stages

### 1. Checkout & Setup (Automated)
- Checks out the repository code
- Installs JDK 17 (Temurin distribution)
- Caches Maven artifacts for faster builds

### 2. Build & Test
```bash
mvn clean verify
```
- Compiles all modules
- Runs unit tests (37 Cucumber scenarios)
- Generates Surefire test reports

### 3. Dependency-Check Scanning
```bash
mvn org.owasp:dependency-check-maven:check
```
- Scans all dependencies for known CVEs
- Requires `NVD_API_KEY` GitHub secret for fast scanning
- Reports: `target/dependency-check-report.html`
- Fails on vulnerabilities with CVSS ≥ 7.0 by default

### 4. PITest Mutation Testing
```bash
mvn org.pitest:pitest-maven:mutationCoverage
```
- Runs mutation analysis on all modules
- Validates test coverage quality
- Reports: `target/pit-reports/index.html`

### 5. SonarQube Analysis (Optional)
```bash
mvn sonar:sonar -Dsonar.login=${{ secrets.SONAR_TOKEN }}
```
- Analyzes code quality and security
- Requires GitHub secrets:
  - `SONAR_TOKEN`: Authentication token
  - `SONAR_HOST_URL`: SonarQube server URL (optional, defaults to localhost:9000)
- Only runs on push (not on PRs from forks)

## GitHub Secrets Configuration

To enable all features, configure these secrets in GitHub repository settings (**Settings → Secrets and variables → Actions**):

### Required (for Dependency-Check):
```
NVD_API_KEY: your-nvd-api-key-here
```

### Optional (for SonarQube integration):
```
SONAR_TOKEN: sqa_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
SONAR_HOST_URL: http://sonarqube.example.com:9000
```

## Artifacts

All analysis reports are automatically uploaded as downloadable artifacts:

1. **dependency-check-report** → `target/dependency-check-report.html`
2. **pitest-reports** → `target/pit-reports/**`
3. **surefire-reports** → `target/surefire-reports/**`

Access via: GitHub Actions → Run Details → Artifacts

## Workflow Configuration Details

| Setting | Value |
|---------|-------|
| Operating System | Ubuntu Latest |
| Java Version | 17 (Temurin) |
| Maven Caching | Enabled (faster builds) |
| Artifact Retention | 30 days |
| SonarQube Retry | Continue on error (optional) |

## Local Testing

Before pushing, run locally:

```bash
# Full pipeline locally
mvn clean verify
mvn org.owasp:dependency-check-maven:check
mvn org.pitest:pitest-maven:mutationCoverage

# View reports
firefox target/dependency-check-report.html
firefox {module}/target/pit-reports/index.html
```

## Expected Results

### Successful Pipeline Run
- ✅ Build: Success
- ✅ Tests: 37/37 passed
- ✅ Dependency-Check: 0 unresolved CVEs
- ✅ PITest: 62-100% mutation coverage
- ✅ SonarQube: 0 bugs, 0 vulns, 0 hotspots

### Common Failures

| Error | Cause | Solution |
|-------|-------|----------|
| `Dependency-Check: CVE found (CVSS≥7.0)` | Vulnerable dependency | Update dependency version or add suppression |
| `Build failed: Tests failed` | Test failure | Check test logs in workflow run |
| `SonarQube: Not authorized` | Missing token | Add `SONAR_TOKEN` to GitHub secrets |
| `NVD API: Rate limited` | Missing NVD API key | Add `NVD_API_KEY` to GitHub secrets |

## Viewing Reports in GitHub

1. Go to your repository's **Actions** tab
2. Click on the workflow run name
3. Scroll down to **Artifacts** section
4. Download the desired report:
   - `dependency-check-report.html` - Open in browser
   - `pitest-reports/` - Unzip and open `index.html`
   - `surefire-reports/` - View test results

## Integration with PR Reviews

The pipeline provides evidence for PR reviews:
- Automated security scanning before merge
- Dependency vulnerabilities reported
- Test coverage metrics via PITest
- SonarQube code quality indicators

Reviewers can:
1. Check workflow status (green checkmark = all SAST passed)
2. Download reports for detailed analysis
3. Approve PR after verification

## Pipeline Compliance with Workshop 2

This pipeline implements all requirements from Workshop 2 (§2.17):

| Requirement | Status | Implementation |
|-------------|--------|-----------------|
| Automated build & test | ✅ | `mvn clean verify` |
| Dependency scanning | ✅ | OWASP Dependency-Check |
| Mutation testing | ✅ | PITest with coverage reports |
| SonarQube integration | ✅ | Optional via secrets |
| Artifact upload | ✅ | GitHub Actions artifacts |
| JDK 17 | ✅ | Temurin distribution |

## Future Enhancements

Optional improvements for future workshops:
- Add SonarQube gate enforcement (fail on violations)
- Add container image scanning (Workshop 4)
- Add DAST integration (Workshop 3)
- Add artifact scanning (Workshop 4)
- Add Slack notifications on failures
- Add badge in README.md
