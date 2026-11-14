# I Built an App With AI. Then I Put 12 Security Tools Against It.

*Working draft — findings and performance claims are intentionally left blank until the experiment is run.*

AI coding tools make it easy to produce a working application quickly. But a working application is not necessarily a secure one. I wanted to test a more practical question: what happens when we take a small application, inspect it with a collection of security tools, fix the findings, and repeat the same checks?

## The experiment

I created AI Security Lab, a local-first Java/Spring Boot API with demo users, product endpoints and order ownership checks. The repository also includes an opt-in intentionally vulnerable IDOR example. That vulnerable profile is disabled by default and is meant only for an isolated local test environment.

## The tools

The experiment covers several different layers: Semgrep for static analysis, Trivy for dependencies and container configuration, TruffleHog for secrets, Nuclei and Strix for authorized runtime testing, and specialist tools for agent skills, malware and LLM red teaming.

These tools do not solve the same problem. A source scanner cannot prove that a running endpoint enforces authorization. A DAST scanner does not replace dependency inventory. A skill scanner is not an application scanner.

## What we measured

For every tool, I will record its version, exact command, target commit, raw sanitized output, confirmed findings, false positives, missed cases and runtime constraints.

### Results

[To be filled after running the tools. No counts or pass rates should be inserted until supported by saved reports.]

### What changed after remediation?

[Document each confirmed issue, the code change, the regression test and the rerun result.]

## What I learned

[Complete after the experiment. Distinguish tool coverage from tool effectiveness and disclose unavailable or incompatible tools.]

## Reproduction

Repository: https://github.com/kanishksinghpujari/ai-security-lab

All tests are limited to this local lab. No third-party systems are in scope.
