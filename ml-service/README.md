# housing-ml

Self-hosted multi-stage LLM pipeline that auto-categorizes ArchMC realms from
their textual fields. Architecture adapted from Apple ML Research's
[App Store review summarization pipeline](https://machinelearning.apple.com/research/app-store-review)
— same three-stage shape, same LoRA + DPO training recipe, distilled onto a
small open model (Gemma 3 1B by default) so the whole thing fits on a single
modest GPU.

## Pipeline

```
                   ┌─────────────────────────────────────────────┐
   Kotlin side     │          persistentgames/housing-api        │
  (Minecraft)      │  HouseTextExtractor -> CategorizationRequest│
                   └───────────────┬─────────────────────────────┘
                                   │ Redis stream: housing:categorize:stage1
                                   ▼
         ┌──────────────────────────────────────────────────┐
         │ Stage 1 — Insight Extraction   [Gemma + LoRA]    │
         │ atomic statements, sentiment, confidence         │
         └──────────────────────────────┬───────────────────┘
                                        │ housing:categorize:stage2
                                        ▼
         ┌──────────────────────────────────────────────────┐
         │ Stage 2 — Dynamic Topic Modeling                 │
         │ LLM-proposed names + embedding dedup             │
         └──────────────────────────────┬───────────────────┘
                                        │ housing:categorize:stage3
                                        ▼
         ┌──────────────────────────────────────────────────┐
         │ Stage 3 — Category Classification                │
         │ IN_HOUSE vs META, top-k                          │
         └──────────────────────────────┬───────────────────┘
                                        │ SET housing:categorize:result:<houseId>
                                        ▼
                            Kotlin polls & caches on PlayerHouse
```

Every boundary between stages is a durable Redis stream. This is the App
Store design: each stage runs at its own replica count, on its own machine
class, with its own adapter — the pipeline is **distributed by default**.

## Running locally

Full stack with real Gemma weights:

```bash
docker compose up --build
```

No-GPU, no-download smoke test using the deterministic mock runner:

```bash
# In-process pipeline only (fastest — ~1s, needs ~80 MB one-time for the
# MiniLM embedder used by stage 2 dedup):
HOUSING_ML_MOCK=1 python scripts/smoke_local.py

# Full distributed stack with Redis streams + workers, still mock weights:
HOUSING_ML_MOCK=1 bash scripts/smoke_distributed.sh
```

Example sample data lives in `training/data/sample_houses.jsonl` — five
realms covering Parkour, Roleplay, Redstone, PvP, and Hangout so you can see
the classifier split IN_HOUSE vs META categories.

From the Kotlin side point `HOUSING_ML_URL=http://localhost:8080` and set
`HOUSING_ML_TRANSPORT=http` to use the synchronous gateway, or leave it
unset to use the Redis-streams transport.

## Fine-tuning

Each stage gets its own LoRA adapter:

```bash
# Stage 1: insight extractor
python -m training.sft_train \
  --stage insight \
  --data training/data/insights.jsonl \
  --output adapters/insight

# Optional: DPO refinement from human-edited pairs
python -m training.dpo_train \
  --stage insight \
  --base adapters/insight \
  --data training/data/insights.dpo.jsonl \
  --output adapters/insight-dpo
```

Swap `--stage` for `topic` / `classify`. At inference, point the worker at
the adapter via `HOUSING_ML_{INSIGHT,TOPIC,CLASSIFY}_ADAPTER`.

## Evaluating

```bash
python -m training.eval \
  --samples training/data/eval.jsonl \
  --human-ratings training/data/human_ratings.csv
```

The rubric mirrors the App Store paper's four axes: safety (unanimity),
groundedness, composition, helpfulness (majority). Groundedness is also
checked automatically by verifying every `supporting_topic_names` entry the
classifier cites actually came out of stage 2.

## Swapping the base model

`HOUSING_ML_BASE_MODEL` accepts any HF causal-LM ID. Tested defaults:

- `google/gemma-3-1b-it` — fast, single-GPU, good for most realm text.
- `google/gemma-3-4b-it` — higher quality; bump the LoRA rank to 32.

For CPU-only, set `HOUSING_ML_4BIT=1` and switch the Dockerfile base to
`python:3.11-slim`.

## Operating

- **Liveness**: `GET /healthz` on the gateway.
- **Metrics**: `GET /metrics` exports Prometheus counters (`housing_ml_requests_total`)
  and a latency histogram. Autoscale each stage on Redis stream pending count
  (see `deploy/k8s-stage1.yaml`).
- **Dead letters**: messages retried more than `RedisConfig.max_deliveries`
  land on `housing:categorize:deadletter`.
- **Cache invalidation**: the content hash on `CategorizationRequest` is
  SHA-256 over every textual field. Kotlin short-circuits when the hash
  matches the last stored result, so re-categorizing a million unchanged
  houses is free.
