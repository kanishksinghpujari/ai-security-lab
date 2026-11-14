# Initial Trivy run — 2026-11-14

## Scope and evidence

- Repository: kanishksinghpujari/ai-security-lab
- Initial scan commit: a13d724e4efe620d82c985dff62d482bab7770f2
- Scanner: Trivy 0.59.1, filesystem scan
- Scan command: filesystem scan with vulnerability, misconfiguration and secret scanners; HIGH and CRITICAL severities
- GitHub Actions run: https://github.com/kanishksinghpujari/ai-security-lab/actions/runs/43568224010

## Findings

Trivy reported two HIGH findings against Spring Boot 3.4.5 in the Actuator starter:
- CVE-2026-22731 — fixed in Spring Boot 3.5.12 or 4.0.4 according to the scanner database at scan time.
- CVE-2026-22733 — fixed in Spring Boot 3.5.12 or 4.0.4 according to the scanner database at scan time.

The scan also logged Rego policy parse errors while loading remote misconfiguration checks. This is a scanner/check-bundle compatibility issue and is recorded as a limitation, not as an application vulnerability.

## Remediation

The Spring Boot parent was upgraded to 3.5.12 in commit 98e2b26538abdb70d99f06b547b1c0a351fcbdff.

The repeat Trivy workflow at commit 390a6c3e323f98532fcff4e6bb0eb9d9ba7218e3 completed successfully:
https://github.com/kanishksinghpujari/ai-security-lab/actions/runs/43568346268

Important scope note: the successful repeat CI workflow scans dependencies and secrets. Misconfiguration scanning was removed from this pinned older Trivy job after its remote policy bundle produced parser errors. Container-image and IaC misconfiguration scans remain future work.

## Interpretation

This is one concrete example of the experiment's value: the scanner found a dependency issue before the app was deployed, and the dependency was upgraded. A green repeat run is not proof that the application is secure; it only says this scanner did not report a HIGH/CRITICAL dependency or secret finding within this scan scope.
