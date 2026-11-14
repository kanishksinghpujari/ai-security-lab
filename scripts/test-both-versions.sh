#!/usr/bin/env bash
set -euo pipefail
repo_root="$(cd "$(dirname "$0")/.." && pwd)"
for version in v1-vulnerable v2-hardened; do
  echo "== Testing ${version} =="
  (cd "$repo_root/versions/$version" && mvn -B test)
done
