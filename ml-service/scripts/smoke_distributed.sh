#!/usr/bin/env bash
# End-to-end Redis-streams smoke:
#   1. boots gateway + 3 workers + redis in mock mode (no GPU, no Gemma DL);
#   2. POSTs sample houses to the async endpoint;
#   3. polls the result keys; prints derived categories.
#
# Prereqs: docker-compose, curl, jq, redis-cli.

set -euo pipefail

cd "$(dirname "$0")/.."

export HOUSING_ML_MOCK=1

echo "==> bringing up stack in mock mode"
docker compose -f docker-compose.yml up -d redis gateway stage1 stage2 stage3

cleanup() { docker compose down; }
trap cleanup EXIT

echo "==> waiting for gateway"
for _ in {1..30}; do
  if curl -sf http://localhost:8080/healthz >/dev/null; then break; fi
  sleep 1
done

echo "==> submitting sample houses (async)"
while IFS= read -r line; do
  [ -z "$line" ] && continue
  curl -sf -X POST http://localhost:8080/v1/categorize/async \
    -H 'content-type: application/json' \
    --data "$line" | jq .
done < training/data/sample_houses.jsonl

echo "==> polling results"
for id in \
  a1b2c3d4-0000-0000-0000-000000000001 \
  a1b2c3d4-0000-0000-0000-000000000002 \
  a1b2c3d4-0000-0000-0000-000000000003 \
  a1b2c3d4-0000-0000-0000-000000000004 \
  a1b2c3d4-0000-0000-0000-000000000005
do
  for _ in {1..30}; do
    raw=$(redis-cli -u redis://localhost:6379/0 GET "housing:categorize:result:${id}" || true)
    if [ -n "${raw}" ] && [ "${raw}" != "(nil)" ]; then
      echo "${id}: $(echo "${raw}" | jq '.categories | map(.label) | join(",")')"
      break
    fi
    sleep 1
  done
done
