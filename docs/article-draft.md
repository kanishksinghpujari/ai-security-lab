# I Built an App With AI. Then I Put 12 Security Tools Against It.

*An experiment in vibe coding, application security, and why one scanner will never be enough.*

AI coding tools have changed how quickly we can ship software. A developer can describe an application, generate a backend, add a UI, wire up Docker, and get something working in a fraction of the time it used to take.

But that creates a new bottleneck.

**The code can arrive faster than the security review.**

I wanted to see what happens when we take an AI-assisted application and run it through a set of open-source security tools—not just read their READMEs, but give them a real codebase and a running target.

The result is an experiment called **AI Security Lab**.

Repository: https://github.com/kanishksinghpujari/ai-security-lab

## The application: Stockroom

I built a small inventory and order-management application with Java 21 and Spring Boot. It has a lightweight browser dashboard, two demo roles, product management, order lookup, and file uploads.

The application is intentionally small. The point is not to build another SaaS product. The point is to create enough realistic attack surface to test different classes of security tooling.

The demo includes:

- A customer role and an administrator role
- Product listing and admin-only product creation
- Orders that belong to specific users
- A file-upload endpoint with type checks and malware scanning
- Docker-based local execution
- GitHub Actions security workflows

The application uses synthetic data and in-memory storage. It is a lab, not a production commerce system.

## I wanted a real vulnerability—not a scanner demo

A security scanner that reports zero findings might mean the code is clean. It might also mean the scanner never exercised the vulnerable path.

So I added a deliberately vulnerable IDOR example behind a separate Spring profile named `lab-vulnerable`.

In the normal application, a customer can read their own order, but cannot read another customer's order. The API returns 404 for an order the user does not own.

The vulnerable profile exposes a separate training route that authenticates the caller but forgets the ownership check. That is the exact kind of authorization mistake that can survive a superficial code review.

The vulnerable profile is disabled by default. The Nuclei workflow starts it only inside an isolated GitHub Actions runner, scans the local service, and stores the result as a workflow artifact. It is never exposed as a public deployment.

## Twelve tools, twelve different jobs

The original list contains tools that operate at very different layers. Treating them as twelve versions of the same scanner would make the experiment meaningless.

| Tool | What it is actually useful for |
|---|---|
| Strix | AI-assisted penetration testing against an authorized running application |
| Cloudflare Security Audit Skill | Multi-phase, agent-assisted source audit with independent finding verification |
| Trivy | Dependency, container, filesystem, IaC, misconfiguration and secret scanning |
| Nuclei | Template-driven checks against a running service |
| TruffleHog | Secret discovery and credential validation across Git history and other sources |
| NVIDIA SkillSpector | Security analysis of AI agent skills before installation |
| Semgrep | Static analysis for insecure source patterns |
| Codex Security | AI-assisted vulnerability investigation, validation and remediation |
| ClamAV | Malware detection for uploaded files |
| Claude Code Security Review | AI-assisted security review of pull-request diffs |
| Blind LLM Agents (BLA) | Defensive hardening of release artifacts against AI-assisted reverse engineering |
| Microsoft PyRIT | Adversarial testing of LLM-powered applications |

The important distinction: a Java source scanner cannot tell you whether an uploaded file contains malware. A skill scanner does not inspect Spring authorization logic. A binary-protection tool is not a substitute for DAST.

## The first real finding: a vulnerable dependency

The first Trivy filesystem run used Trivy 0.59.1 against the initial application commit.

It reported two HIGH-severity Spring Boot findings in version 3.4.5:

- CVE-2026-22731
- CVE-2026-22733

The scanner output listed Spring Boot 3.5.12 as a fixed version for both findings.

I upgraded the Spring Boot parent from 3.4.5 to 3.5.12 and reran the dependency and secret scan. That repeat workflow completed successfully.

This is a useful example of a scanner doing something concrete: it found a vulnerable dependency before the application was deployed, and the dependency was upgraded.

A green scan is not proof that an application is secure. It means the scanner did not report a finding within that scan's scope and database.

The first Trivy run also emitted Rego parser errors while loading remote misconfiguration checks. I recorded that as a scanner/check-bundle compatibility issue rather than an application vulnerability. The workflow was later upgraded to Trivy 0.76.2 and expanded to include filesystem misconfiguration and built-image scans; those newer runs are tracked separately.

## Nuclei: testing the running application

Semgrep and Trivy inspect source and artifacts. They do not prove that a running endpoint enforces the right authorization rule.

For that, I added a small Nuclei template that requests the seeded vulnerable order route using the demo customer's credentials.

The Nuclei regression workflow successfully detected the intentionally exposed IDOR route in the isolated vulnerable profile.

That is the behavior I wanted to see: the test target contains a known issue, the scanner identifies it, and the workflow preserves the evidence.

The secure endpoint is covered separately by application tests: a customer can read order 1001, cannot read order 1002, and an administrator can read either.

## ClamAV: uploads are a different security problem

I also added a file-upload endpoint because application security is not only about code and HTTP routes.

The endpoint accepts a small allowlist of file types, checks file signatures, enforces a 5 MiB size limit, and streams file contents to ClamAV using its INSTREAM protocol.

Two behaviors matter:

1. A clean file can be accepted.
2. If ClamAV is unavailable, the application fails closed instead of treating the file as safe.

The dedicated workflow uses a harmless clean text fixture and the standard EICAR antivirus test signature to verify both outcomes. The workflow result is tracked separately from the earlier completed scans.

## What is already automated—and what still needs credentials

The repository now has GitHub Actions workflows for:

- Maven build and authorization regression tests
- Semgrep Java static analysis
- Trivy filesystem and container-image scans
- Nuclei testing of the seeded IDOR route
- TruffleHog scanning of full Git history
- ClamAV upload regression testing
- SkillSpector scanning of the Cloudflare audit skill in static-only mode

Not every tool can be run in the same CI job.

Strix needs Docker and an LLM provider. Cloudflare's audit skill expects a coding agent with parallel sub-agents and an OS-enforced sandbox. Codex Security requires its CLI/product access and credentials. Claude Code Security Review requires a Claude API key and should only run on trusted pull requests because of its prompt-injection considerations.

BLA protects release artifacts against AI-assisted reverse engineering; it is not a conventional vulnerability scanner. PyRIT needs an LLM-backed feature and a target harness. This application does not yet include an LLM assistant, so PyRIT is a planned extension rather than a claimed result.

That is not a weakness in the experiment. It is the point: security tools have boundaries, and a credible evaluation needs to state them.

## What I learned so far

The most useful lesson is that a security pipeline should be layered.

- Use SAST to catch suspicious source patterns.
- Use dependency and image scanning to catch known vulnerable components.
- Use secret scanning to prevent credential leaks.
- Use DAST to test the behavior of a running service.
- Use malware scanning for uploaded content.
- Use skill scanning before trusting agent instructions.
- Use LLM red teaming only when there is an LLM-powered feature to attack.
- Use release-artifact hardening when reverse engineering is part of the threat model.

And most importantly, keep the evidence. A scanner's exit code is not a security report. Record the version, command, target commit, raw sanitized output, false positives, confirmed findings, and the remediation that followed.

## Reproduce the experiment

Clone the repository and follow the README. The default application runs on loopback only. The intentionally vulnerable profile is opt-in and should only be used in the isolated lab.

https://github.com/kanishksinghpujari/ai-security-lab

This is an evolving experiment. The results above distinguish completed runs from integrations that are still pending; I will update the article as the remaining workflows finish.

The goal isn't to prove that twelve tools make an application invulnerable. It's to understand which risks each tool can see, which ones it cannot, and how to build a security workflow that catches more than a single scanner ever could.
