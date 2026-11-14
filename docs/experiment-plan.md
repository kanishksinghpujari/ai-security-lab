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

## Seeded issue
The opt-in lab-vulnerable profile contains an insecure direct object reference demonstration at GET /lab-vulnerable/orders/{id}. It is not loaded by default. The normal API order endpoint enforces owner/admin access.

## Tool coverage

| Tool | Primary layer | Notes |
|---|---|---|
| Strix | Authorized DAST | Local target only |
| Cloudflare Security Audit Skill | Agent-assisted source audit | Run in supported coding agent |
| Trivy | Dependencies, image, IaC | Scan repository and built image |
| Nuclei | Template-based DAST | Explicit loopback target |
| TruffleHog | Secrets | Scan working tree and Git history |
| NVIDIA SkillSpector | Agent skills | Scan skills used in experiment |
| Semgrep | SAST | Scan source tree |
| Codex Security | AI-assisted code security | Access/availability to verify |
| ClamAV | Malware | Upload scanning fixture/integration planned |
| Claude Code Security Review | PR review | Verify current supported workflow |
| Blind LLM Agents | Reverse-engineering resistance | Separate experiment, not app SAST |
| Microsoft PyRIT | LLM red teaming | Requires LLM feature and harness |

The matrix is a plan, not a claim that scans have run or passed. These tools are not interchangeable.

## Safety boundaries
- Never scan third-party systems.
- Use synthetic credentials and disposable data.
- Do not enable lab-vulnerable on shared or public infrastructure.
- Sanitize reports before committing; never publish credentials, cookies or private data.
