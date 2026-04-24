"""Stage 2 — Dynamic topic modeling.

Two substeps, matching the App Store paper's dynamic topic modeling:
 1. the fine-tuned language model proposes canonical topic names per insight;
 2. embedding-based deduplication merges near-duplicate topic names within
    the realm so two insights that really describe the same thing collapse.

Step 2 is what makes categories *derived* instead of drawn from a fixed list:
novel themes surface the moment the model proposes them, no code change.
"""

from __future__ import annotations

import logging
import os
from collections import defaultdict

import orjson

from config import SETTINGS
from models.loader import get_runner

# Embedding deps are optional when running in mock mode (the deterministic
# stand-in already emits canonical names, so dedup is a no-op).
if not os.environ.get("HOUSING_ML_MOCK"):
    import numpy as np  # type: ignore

    from models.embeddings import cosine_matrix, encode  # type: ignore
else:  # pragma: no cover
    np = None  # type: ignore
    cosine_matrix = None  # type: ignore
    encode = None  # type: ignore
from pipeline.prompts import render_stage2_prompt
from pipeline.schemas import CategorizationRequest, HouseInsight, HouseTopic

logger = logging.getLogger(__name__)


def normalize_topics(req: CategorizationRequest, insights: list[HouseInsight]) -> list[HouseTopic]:
    if not insights:
        return []

    prompt = render_stage2_prompt(req, insights)
    raw = get_runner("topic").generate(prompt)

    proposed: list[HouseTopic] = []
    for line in raw.splitlines():
        line = line.strip()
        if not line or not line.startswith("{"):
            continue
        try:
            obj = orjson.loads(line)
        except orjson.JSONDecodeError:
            continue

        try:
            indices = [int(i) for i in obj.get("supporting_insight_indices", [])]
            indices = [i for i in indices if 0 <= i < len(insights)]
        except (TypeError, ValueError):
            indices = []

        proposed.append(
            HouseTopic(
                name=str(obj.get("name", "")).strip(),
                supportingInsightIndices=indices,
                aggregateConfidence=float(obj.get("aggregate_confidence", 0.5)),
            )
        )

    proposed = [t for t in proposed if t.name]
    if os.environ.get("HOUSING_ML_MOCK"):
        # Trivial dedup — exact-name match. Good enough for smoke tests.
        by_name: dict[str, HouseTopic] = {}
        for t in proposed:
            existing = by_name.get(t.name)
            if existing is None or t.aggregateConfidence > existing.aggregateConfidence:
                by_name[t.name] = t
        return sorted(by_name.values(), key=lambda t: t.aggregateConfidence, reverse=True)
    return _dedup_topics(proposed)


def _dedup_topics(topics: list[HouseTopic]) -> list[HouseTopic]:
    if len(topics) <= 1:
        return topics

    threshold = SETTINGS.pipeline.dedup_similarity
    vectors = encode([t.name for t in topics])
    sims = cosine_matrix(vectors, vectors)

    parent = list(range(len(topics)))

    def find(i: int) -> int:
        while parent[i] != i:
            parent[i] = parent[parent[i]]
            i = parent[i]
        return i

    def union(a: int, b: int) -> None:
        ra, rb = find(a), find(b)
        if ra != rb:
            parent[rb] = ra

    n = len(topics)
    for i in range(n):
        for j in range(i + 1, n):
            if sims[i, j] >= threshold:
                union(i, j)

    groups: dict[int, list[int]] = defaultdict(list)
    for i in range(n):
        groups[find(i)].append(i)

    merged: list[HouseTopic] = []
    for members in groups.values():
        anchor = max(members, key=lambda m: topics[m].aggregateConfidence)
        supporting = sorted({
            idx
            for m in members
            for idx in topics[m].supportingInsightIndices
        })
        conf = float(np.mean([topics[m].aggregateConfidence for m in members]))
        merged.append(
            HouseTopic(
                name=topics[anchor].name,
                supportingInsightIndices=supporting,
                aggregateConfidence=conf,
            )
        )

    merged.sort(key=lambda t: t.aggregateConfidence, reverse=True)
    return merged
