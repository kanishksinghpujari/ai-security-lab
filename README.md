# AI Security Lab — two-version comparison

This repository now contains two independently runnable builds of the same Stockroom application. They share the same Java/Spring Boot version, API surface, demo data, UI, dependencies, and runtime configuration. The intended difference is the order-ownership authorization check.

- **V1 — vulnerable:** authenticated customers can read another customer's order through `GET /api/orders/{id}`. This is a deliberately seeded IDOR/Broken Object Level Authorization flaw.
- **V2 — hardened:** the same endpoint returns 404 when a customer requests an order they do not own; administrators can still read all orders.

Both versions retain the same upload pipeline (allowlisted file signatures, 5 MiB cap, ClamAV scan, fail-closed behavior). That keeps the experiment focused: do not attribute upload-security differences to the IDOR remediation.

> Safety: V1 is intentionally insecure. Run only on your own machine or an isolated CI runner. Compose publishes both apps on loopback only. Never expose V1 to a shared network or the public internet.

## Run both versions

Requirements: Docker Compose and enough memory for ClamAV's signature database.

```bash
export LAB_ADMIN_PASSWORD='local-admin-password'
export LAB_CUSTOMER_PASSWORD='local-customer-password'
docker compose -f compose.compare.yaml up --build -d
```

- V1: http://127.0.0.1:8081
- V2: http://127.0.0.1:8082
- ClamAV is internal to the Compose network and is not host-published.

Run the same customer request against both:

```bash
./scripts/attack-comparison.sh
```

Expected result: V1 returns HTTP 200 and exposes order 1002; V2 returns HTTP 404. The script fails if those expectations are not met.

Stop the lab:

```bash
docker compose -f compose.compare.yaml down
```

## Run the regression tests

```bash
./scripts/test-both-versions.sh
```

The V1 test intentionally asserts the vulnerability exists. The V2 test asserts cross-customer access is denied. These are different expected outcomes, not a shared test suite that passes by hiding the difference.

## Run Nuclei

```bash
nuclei -u http://127.0.0.1:8081 -t nuclei-templates/idor-v1.yaml \
  -H "Authorization: Basic $(printf 'lab-customer:%s' "$LAB_CUSTOMER_PASSWORD" | base64)"
``

The V1 template should report the seeded IDOR. The same template against V2 should produce no finding. The manual comparison script remains the authoritative simple regression check.

## Security tools and evidence

The existing CI continues to scan the original root application and its artifacts. The comparison-specific workflow builds both independent versions, runs both Maven test suites, and performs the same authenticated runtime probe against each. Tool outputs must be committed under `results/` only after the workflow actually runs; no result counts or pass claims are pre-filled.

Not all items in the original 12-tool list are comparable application scanners. Strix, Codex Security, Claude Code Security Review, and agent-based audits require credentials/product access; SkillSpector scans agent skills; ClamAV scans uploaded content; PyRIT needs an LLM-backed target; BLA addresses reverse-engineering resistance. The article must state these boundaries.

## Layout

- `versions/v1-vulnerable/`: standalone Maven project with the intentionally missing ownership check
- `versions/v2-hardened/`: standalone Maven project with the owner/admin authorization check
- `compose.compare.yaml`: isolated side-by-side runtime
- `scripts/`: repeatable test and attack scripts
- `nuclei-templates/idor-v1.yaml`: V1-only runtime detection template
- `docs/experiment-plan.md`: controlled comparison methodology
- `docs/article-draft.md`: updated article draft; measured results remain pending until CI evidence exists

The original root project and its workflows are retained for provenance. This comparison is a new, clearer experiment layered alongside that initial implementation.
