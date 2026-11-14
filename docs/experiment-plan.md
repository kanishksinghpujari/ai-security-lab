# Experiment plan

## Research question
How much risk remains in an AI-assisted application after layered security testing, and what classes of risk does each tool cover?

## Controls
1. Pin the application commit under test.
2. Record tool version, invocation, configuration and runtime environment.
3. Run source scanners against the same commit.
4. Run dynamic scanners only against the local app bound to loopback.
5. Manually triage findings and deduplicate by root cause.
6. Fix confirmed issues in a separate commit.
7. Repeat identical commands against the hardened commit.
8. Preserve sanitized reports and note tool limitations.

## Seeded issues and test fixtures
- The opt-in lab-vulnerable profile contains an insecure direct object reference demonstration at GET /lab-vulnerable/orders/{id}. It is not loaded by default. The normal API order endpoint enforces owner/admin access.
- The ClamAV upload path uses the harmless EICAR test signature in CI to confirm that detected malware is rejected. The EICAR string is a standard antivirus test fixture, not a live virus.

## Tool coverage

| Tool | Primary layer | Notes |
|---|---|---|
| Strix | Authorized DAST | Local target only; needs an LLM provider |
| Cloudflare Security Audit Skill | Agent-assisted source audit | Requires a supported coding agent and OS-enforced sandbox |
| Trivy | Dependencies, image, IaC | Repository dependency/secret scan currently in CI |
| Nuclei | Template-based DAST | Explicit loopback target; seeded IDOR template |
| TruffleHog | Secrets | Full Git history scan in CI |
| NVIDIA SkillSpector | Agent skills | Scan the actual skill directories installed for the experiment |
| Semgrep | SAST | Java ruleset in CI |
| Codex Security | AI-assisted code security | CLI/SDK requires current product access and credentials |
| ClamAV | Malware | INSTREAM upload integration and EICAR regression workflow |
| Claude Code Security Review | PR review | Optional; do not run on untrusted PRs without protections |
| Blind LLM Agents | Reverse-engineering resistance | Separate release-artifact test, not app SAST |
| Microsoft PyRIT | LLM red teaming | Requires an LLM feature and target harness; deferred |

The matrix is a plan plus current integration status, not a claim that all tools have run or passed. These tools are not interchangeable.

## Safety boundaries
- Never scan third-party systems.
- Use synthetic credentials and disposable data.
- Do not enable lab-vulnerable on shared or public infrastructure.
- Keep the ClamAV port internal to Compose.
- Sanitize reports before committing; never publish credentials, cookies or private data.
