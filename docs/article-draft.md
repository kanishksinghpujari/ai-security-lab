# I Built Two Versions of the Same App. Then I Tried to Break Both.

*An evidence-driven comparison of a deliberately vulnerable application and its hardened counterpart.*

AI coding tools make it possible to ship a working application quickly. But working software and secure software are not the same thing. I wanted to run a controlled experiment: build the same application twice, deliberately leave a security flaw in the first version, fix it in the second, and attack both with the same tools.

The project is **AI Security Lab**: https://github.com/kanishksinghpujari/ai-security-lab

## The experiment: same app, two builds

The application is Stockroom, a small inventory and order-management service built with Java 21 and Spring Boot 3.5.12. Both builds use the same API surface, UI, demo data, dependencies, authentication mechanism, Docker runtime, and upload pipeline.

The only intended difference is the authorization check on order reads.

| | V1 — vulnerable | V2 — hardened |
|---|---|---|
| Customer reads own order | Allowed | Allowed |
| Customer reads another customer's order | **Allowed (IDOR/BOLA)** | **Denied (404)** |
| Admin reads any order | Allowed | Allowed |
| Upload controls | Same | Same |
| Framework and dependencies | Same baseline | Same baseline |

This is important. If the versions differ in many unrelated ways, a scanner result cannot be attributed to the security change.

## The vulnerability we planted

Order 1001 belongs to `lab-customer`. Order 1002 belongs to `another-customer`.

In V1, the endpoint authenticates the caller but does not verify that the requested order belongs to that caller. A customer can change the numeric ID and retrieve another customer's order.

In V2, the same endpoint checks the order owner and allows an administrator override. A non-owner receives 404, avoiding disclosure that another customer's order exists.

Both variants have separate Maven projects and can be started side by side on loopback-only ports: V1 on 8081 and V2 on 8082. The vulnerable service is never published to the internet.

## The attack protocol

The comparison uses the same credentials, request, test data, and scanner template against both versions:

```bash
curl -u "lab-customer:$LAB_CUSTOMER_PASSWORD" \
  http://127.0.0.1:8081/api/orders/1002

curl -u "lab-customer:$LAB_CUSTOMER_PASSWORD" \
  http://127.0.0.1:8082/api/orders/1002
```

Expected behavior: V1 returns 200 with the seeded order; V2 returns 404. The repository includes a script that asserts those outcomes and independent test suites that explicitly encode the expected behavior for each version.

Nuclei is used as a positive control against V1: it should detect the known IDOR. The same template against V2 should produce no finding. A scanner's silence is not proof of security, so the negative result is paired with direct authorization tests.

## The tools—and what they can actually tell us

The project includes a wider tool evaluation, but the tools have different jobs:

- **Nuclei / Strix:** runtime testing of an authorized local target.
- **Semgrep:** suspicious source-code patterns.
- **Trivy:** dependencies, filesystem configuration and container images.
- **TruffleHog:** secrets in Git history.
- **ClamAV:** malware scanning for uploaded files.
- **SkillSpector / Cloudflare audit skill:** agent-skill and source-audit workflows.
- **Codex Security / Claude Code Security Review:** AI-assisted code review requiring product access and credentials.
- **PyRIT:** LLM red teaming; not applicable until the app has an LLM-backed feature.
- **Blind LLM Agents:** release-artifact reverse-engineering resistance, not ordinary application vulnerability scanning.

I will not describe a tool as passing if it was not run. Credential-gated or inapplicable tools will be clearly marked as such.

## Results: leave room for evidence, not guesses

This section is intentionally a results template until the comparison workflows finish. The numbers below must come from saved CI artifacts and manual triage, not assumptions.

| Measurement | V1 | V2 |
|---|---:|---:|
| Seeded IDOR exploit result | Pending CI | Pending CI |
| Nuclei result | Pending CI | Pending CI |
| Maven authorization tests | Pending CI | Pending CI |
| Confirmed unrelated findings | Pending triage | Pending triage |
| False positives / missed cases | Pending triage | Pending triage |

For each run, the repository records the tool version, exact command, target commit, sanitized output, manual disposition, and limitations. A clean scan means only that the scanner did not report an issue within its coverage.

## What this experiment can show

If the expected behavior is confirmed, the most defensible conclusion is narrow but useful: the owner check prevents the seeded cross-customer order disclosure, and the same runtime test can distinguish the vulnerable and hardened variants.

It does not prove that V2 is invulnerable. It does not mean every one of the 12 tools can evaluate every risk. It shows why security needs multiple layers: code review, authorization tests, dependency and image scans, secret detection, runtime testing, and file scanning each cover different failure modes.

## Reproduce it

Follow the README to build both versions, run the same attack, and stop the lab. V1 is intentionally insecure and must remain local.

Repository: https://github.com/kanishksinghpujari/ai-security-lab

The goal is not to produce a dramatic scanner score. It is to make the comparison repeatable, preserve the evidence, and be honest about what each tool did—and did not—find.
