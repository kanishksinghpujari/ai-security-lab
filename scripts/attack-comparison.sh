#!/usr/bin/env bash
set -euo pipefail
: "${LAB_CUSTOMER_PASSWORD:?Export LAB_CUSTOMER_PASSWORD first}"
auth="lab-customer:${LAB_CUSTOMER_PASSWORD}"
v1_code="$(curl --silent --show-error -o /tmp/ai-security-lab-v1.json -w '%{http_code}' -u "$auth" http://127.0.0.1:8081/api/orders/1002)"
v2_code="$(curl --silent --show-error -o /tmp/ai-security-lab-v2.json -w '%{http_code}' -u "$auth" http://127.0.0.1:8082/api/orders/1002)"
printf 'V1 vulnerable: HTTP %s\n' "$v1_code"
printf 'V2 hardened:   HTTP %s\n' "$v2_code"
if [[ "$v1_code" != "200" || "$v2_code" != "404" ]]; then
  echo "Unexpected result. Confirm both services are healthy and inspect the response bodies." >&2
  exit 1
fi
echo "PASS: the same cross-customer order request succeeds in V1 and is denied in V2."
