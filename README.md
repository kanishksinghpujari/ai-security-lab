# AI Security Lab

A reproducible, local-first experiment: build an application, run security tools against it, remediate confirmed findings, and publish evidence-backed results.

> Safety: This is a training lab. Test only systems you own or are authorized to assess. The intentionally vulnerable profile is disabled by default. Never expose it to the public internet.

## Stack
- Java 21, Spring Boot 3.5.12, Maven
- Spring Security with disposable demo users
- Static HTML/CSS/JavaScript dashboard
- ClamAV 1.4.6 for upload scanning
- Docker and GitHub Actions

## Quick start
Requirements: JDK 21+ and Docker Compose. Allow Docker enough memory for ClamAV's signature database (the ClamAV documentation recommends around 4 GB).

1. Export disposable local credentials:
   export LAB_ADMIN_PASSWORD='replace-with-a-long-local-password'
   export LAB_CUSTOMER_PASSWORD='replace-with-another-long-local-password'
2. Run: docker compose up --build
3. Open http://127.0.0.1:8080
4. Health: curl http://127.0.0.1:8080/actuator/health
5. API: curl -u lab-customer:$LAB_CUSTOMER_PASSWORD http://127.0.0.1:8080/api/products
6. Upload scan: curl -u lab-customer:$LAB_CUSTOMER_PASSWORD -F 'file=@notes.txt' http://127.0.0.1:8080/api/uploads

The host port is published only on 127.0.0.1. Docker overrides the app's internal bind address so the container can receive traffic through that loopback-only port mapping. ClamAV is reachable only on the internal Compose network; its TCP port is not published to the host.

## Demo accounts
- lab-customer: regular user; can read their own order and upload files.
- lab-admin: administrator; can create products and inspect all demo orders.

Set both passwords through environment variables. Do not reuse production credentials.

## Profiles
- Default: safer baseline. Intentionally vulnerable routes are not loaded.
- lab-vulnerable: opt-in IDOR demonstration. Use only in an isolated local lab.

To run locally with Maven, start ClamAV separately and set CLAMAV_HOST=127.0.0.1, then:
   export LAB_ADMIN_PASSWORD='replace-me'
   export LAB_CUSTOMER_PASSWORD='replace-me-too'
   mvn spring-boot:run

To enable the vulnerable profile locally, additionally set SPRING_PROFILES_ACTIVE=lab-vulnerable. Never use this profile on a shared host or public deployment.

## Security tool coverage

| Tool | Intended coverage | Current integration |
|---|---|---|
| Strix | Authorized AI-assisted DAST | Manual run; needs Docker and an LLM provider |
| Cloudflare Security Audit Skill | Multi-phase agent source audit | Manual agent workflow; sandbox required |
| Trivy | Dependencies, images, IaC, secrets | GitHub Actions filesystem dependency/secret scan |
| Nuclei | Template-based runtime testing | GitHub Actions local IDOR regression |
| TruffleHog | Secrets in Git history | GitHub Actions full-history scan |
| NVIDIA SkillSpector | Agent skill supply chain | Manual scan of installed skills |
| Semgrep | Static source analysis | GitHub Actions Java ruleset |
| Codex Security | AI-assisted vulnerability investigation | Manual CLI/SDK; requires access and credentials |
| ClamAV | Uploaded-file malware detection | API integration and EICAR regression workflow |
| Claude Code Security Review | AI-assisted PR review | Optional; only trusted PRs, requires API key |
| Blind LLM Agents (BLA) | Reverse-engineering resistance | Separate release-artifact experiment; not a vuln scanner |
| Microsoft PyRIT | LLM red teaming | Deferred until an LLM-backed assistant is added |

These tools are not interchangeable. The table describes current integration status, not a claim that all 12 have completed scans or passed.

## Evidence policy
For every run, record the exact command, tool version, target commit, sanitized output, manual triage, false positives and limitations. Never fabricate counts or results. Never commit credentials or raw sensitive data.

## Layout
- src/: Spring Boot API and dashboard
- docs/: architecture, methodology, tool notes and article draft
- nuclei-templates/: lab-specific Nuclei regression template
- reports/: sanitized scan evidence
- .github/workflows/: CI and security checks

See docs/experiment-plan.md and docs/tools.md for methodology and tool-specific notes.
