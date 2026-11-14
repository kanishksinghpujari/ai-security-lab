# AI Security Lab

A reproducible, local-first experiment: build a Java application, run security tools against it, remediate confirmed findings, and publish evidence-backed results.

> Safety: This is a training lab. Test only systems you own or are authorized to assess. The intentionally vulnerable profile is disabled by default. Never expose it to the public internet.

## Stack
- Java 21, Spring Boot 3.4, Maven
- Spring Security
- Docker
- GitHub Actions

## Quick start
Requirements: JDK 21+ and Docker Compose.

1. Export disposable local credentials:
   export LAB_ADMIN_PASSWORD='replace-with-a-local-password'
   export LAB_CUSTOMER_PASSWORD='replace-with-another-local-password'
2. Run: docker compose up --build
3. Health: curl http://127.0.0.1:8080/actuator/health
4. API: curl -u lab-customer:$LAB_CUSTOMER_PASSWORD http://127.0.0.1:8080/api/products

The host port is published only on 127.0.0.1. Docker overrides the app's internal bind address so the container can receive traffic through that loopback-only port mapping.

## Profiles
- Default: safer baseline. Intentionally vulnerable routes are not loaded.
- lab-vulnerable: opt-in IDOR demonstration. Use only in an isolated local lab.

To run locally with Maven:
   export LAB_ADMIN_PASSWORD='replace-me'
   export LAB_CUSTOMER_PASSWORD='replace-me-too'
   mvn spring-boot:run

To enable the vulnerable profile locally, additionally set SPRING_PROFILES_ACTIVE=lab-vulnerable. Never use this profile on a shared host or public deployment.

## Tool coverage plan

| Tool | Intended coverage |
|---|---|
| Strix | Authorized dynamic application testing |
| Cloudflare Security Audit Skill | Agent-assisted source audit |
| Trivy | Dependencies, container images and IaC |
| Nuclei | Template-based testing of owned local targets |
| TruffleHog | Secrets in working tree and Git history |
| NVIDIA SkillSpector | Agent skill supply-chain analysis |
| Semgrep | Static application security analysis |
| Codex Security | AI-assisted vulnerability discovery, subject to access |
| ClamAV | Malware scanning for upload fixtures (integration planned) |
| Claude Code Security Review | PR review workflow, subject to current support |
| Blind LLM Agents | Separate reverse-engineering-resistance experiment |
| Microsoft PyRIT | LLM red teaming; requires an LLM feature/harness |

These tools are not interchangeable. The table is a plan, not a claim that scans have run or passed.

## Evidence policy
For every run, record the exact command, tool version, target commit, sanitized output, manual triage, false positives and limitations. Never fabricate counts or results. Never commit credentials or raw sensitive data.

## Layout
- src/: Spring Boot application
- docs/: architecture, methodology and article draft
- reports/: sanitized scan evidence
- .github/workflows/: CI checks

See docs/experiment-plan.md for methodology.
