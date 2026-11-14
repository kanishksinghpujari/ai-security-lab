# Security tool integration guide

The source list below is mapped to the exact upstream repositories. Integrations are intentionally layered: not every tool scans the same artifact, and some require a coding agent, an LLM provider, or a distinct target type.

## Application and source security

### Strix — https://github.com/usestrix/strix
Autonomous AI-assisted penetration testing. Requires Docker and an LLM provider/API key. Run only against the local lab target or a local copy of the app. The upstream quick start installs the CLI and uses a target directory; the tool can also test a running application. Capture the exact target, model/provider, version and run artifacts.

### Cloudflare Security Audit Skill — https://github.com/cloudflare/security-audit-skill
A coding-agent skill for a multi-phase source audit: reconnaissance, coverage-led hunting, independent candidate validation, structured findings and report generation. Install using the upstream Skills CLI instructions. The skill requires a coding agent with parallel tool-using agents and an OS-enforced sandbox for executing target-controlled builds/tests. Do not run target-controlled code outside that sandbox.

### Semgrep — https://github.com/semgrep/semgrep
Static analysis for source patterns. The repository CI currently runs the Java ruleset through the Semgrep GitHub Action. Review alerts manually and record rule IDs and code locations.

### Codex Security — https://github.com/openai/codex-security
AI-assisted security investigation with CLI, TypeScript SDK and GitHub Action options. Upstream CLI currently requires supported Node.js and sign-in/API credentials. Use a bounded scan scope and cost/time limits; preserve the report and review proposed patches before applying them.

### Claude Code Security Review — https://github.com/anthropics/claude-code-security-review
AI-powered, diff-aware GitHub Action that reviews PR changes and can comment on findings. It requires a Claude API key. Upstream documentation warns that the action is not hardened against prompt injection: only run it on trusted PRs after reviewing contributor/workflow controls. We have not enabled it automatically on public fork PRs.

## Dependencies, images and secrets

### Trivy — https://github.com/aquasecurity/trivy
Scans dependencies, images, filesystems, IaC, secrets and misconfigurations. The current workflow uses a pinned Trivy container to scan repository dependencies and secrets for HIGH/CRITICAL findings. Image and IaC scans are follow-up jobs.

### TruffleHog — https://github.com/trufflesecurity/trufflehog
Discovers, classifies and can validate exposed credentials. Run against the Git history, not just the current working tree. Treat verified credentials as compromised: revoke/rotate them, do not paste them into issues or reports, and preserve only redacted evidence.

### ClamAV — https://github.com/Cisco-Talos/clamav
Malware scanning engine for uploaded files. It does not identify application authorization bugs or source-code vulnerabilities. The application does not yet expose an upload endpoint; add a controlled EICAR test fixture and clamd integration before claiming ClamAV coverage.

## Runtime testing

### Nuclei — https://github.com/projectdiscovery/nuclei
Template-driven vulnerability scanner for HTTP services and other supported targets. Run against the loopback-only lab URL, with a low request rate and a deliberately narrow scope. Do not run broad internet templates or scan third-party targets.

## Agent-skill and AI-specific security

### NVIDIA SkillSpector — https://github.com/NVIDIA/skillspector
Scans agent skills for prompt injection, data exfiltration, privilege escalation, dangerous code and supply-chain risks. It is a skill scanner, not a Java application scanner. Use it on the exact skill directories installed for the experiment. Static-only mode is available with the upstream --no-llm option.

### Microsoft PyRIT — https://github.com/microsoft/PyRIT
AI red-teaming framework with a CLI scanner, GUI and Python framework. It needs an AI model/application target. The current app has no LLM feature, so PyRIT is explicitly deferred until an inventory assistant and HTTP target adapter are added.

## Release-artifact protection

### BLA / Blind LLM Agents — https://github.com/qybaihe/bla
A defensive counter-reverse-engineering toolkit for release artifacts such as JS/TS bundles, Electron ASAR, WASM and native binaries. It is not a vulnerability scanner. This Java-only milestone has no suitable browser bundle or release artifact; evaluate it only after adding a frontend build, and compare runtime behavior before/after hardening.

## Evidence checklist

For every tool, save:
- upstream repo URL and version/tag
- installation method and command
- exact input scope and target commit
- raw report where redistribution permits, otherwise a sanitized summary
- confirmed findings, false positives, missed cases and limitations
- remediation commit and identical rerun command

Never report a tool as integrated or passing merely because its workflow file exists.
