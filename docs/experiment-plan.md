# Controlled two-version security experiment

## Research question

When the same Stockroom application is built in two versions that differ only in order ownership enforcement, can the same tests and security tools demonstrate the security improvement without changing the target's features or runtime?

## Variants

| Dimension | V1 — vulnerable | V2 — hardened |
|---|---|---|
| Framework / Java | Spring Boot 3.5.12 / Java 21 | Same |
| API, UI, data fixtures | Same | Same |
| Authentication | Same demo HTTP Basic users | Same |
| Product authorization | Admin-only | Admin-only |
| Order authorization | Missing owner/admin check | Owner or ADMIN required |
| Upload controls | Same allowlist, size limit, ClamAV scan, fail-closed behavior | Same |
| Runtime | Loopback port 8081 | Loopback port 8082 |

The intentional treatment variable is the object-level authorization check in `GET /api/orders/{id}`. Do not introduce unrelated changes between variants during this experiment.

## Primary attack

Authenticate as `lab-customer` and request order 1002, which belongs to `another-customer`.

- V1 expected: HTTP 200 and response contains the other customer's order.
- V2 expected: HTTP 404 and no order data is disclosed.
- Admin control: `lab-admin` can read order 1002 in both versions.
- Owner control: `lab-customer` can read order 1001 in both versions.

The shell regression script and Nuclei template use the same seeded cross-customer request. V1 is the positive detection control; V2 is the negative control.

## Execution protocol

1. Record the exact branch commit and image digests.
2. Build both independent Maven projects from the same dependency baseline.
3. Run each version's unit/integration tests.
4. Start both on loopback-only ports with the same disposable credentials and shared internal ClamAV service.
5. Run the identical authenticated request against each.
6. Run Nuclei against V1 and V2 with the same template; expect a finding only on V1.
7. Run source/dependency/image scanners against both project directories with pinned tool versions and identical options.
8. Triage findings manually; deduplicate by root cause and distinguish seeded findings from unrelated dependency/configuration findings.
9. Save sanitized raw outputs, tool versions, commands, timestamps, commit SHA and image digest under `results/`.
10. Do not report a zero-finding scan as proof of security. Report only what each tool tested.

## Metrics to report

- Confirmed exploitable seeded issues per variant
- Scanner true positives against the seeded issue
- False positives after manual triage
- Missed seeded issue (false negative)
- Dependency and container findings, grouped by severity and fix availability
- Runtime and resource cost, if measured
- Regression test outcomes
- Limitations, unavailable tools and credential-gated tools

## Tool applicability

The 12 tools do not all test the same surface. Nuclei/Strix are runtime-oriented; Semgrep/Trivy inspect source and artifacts; TruffleHog searches history; ClamAV scans uploaded file contents; SkillSpector audits agent skills; PyRIT requires an LLM target; BLA addresses release-artifact reverse engineering. Credentialed AI products must be listed as not run when access is unavailable, not treated as passing.

## Safety

V1 is intentionally insecure. Both Compose services bind only to host loopback. Use synthetic data and disposable credentials. Never expose V1 to a public/shared network or scan third-party systems.
