"""Stage 3 — Category classification.

Takes the deduplicated topics and emits the final categories with scope
IN_HOUSE vs META. This is the stage players ultimately see.
"""

from __future__ import annotations

import logging

import orjson

from config import SETTINGS
from models.loader import get_runner
from pipeline.prompts import render_stage3_prompt
from pipeline.schemas import CategorizationRequest, HouseCategory, HouseTopic, Scope

logger = logging.getLogger(__name__)


def classify_categories(req: CategorizationRequest, topics: list[HouseTopic]) -> list[HouseCategory]:
    if not topics:
        return []

    prompt = render_stage3_prompt(req, topics)
    raw = get_runner("classify").generate(prompt)

    topic_name_set = {t.name for t in topics}
    results: list[HouseCategory] = []
    for line in raw.splitlines():
        line = line.strip()
        if not line or not line.startswith("{"):
            continue
        try:
            obj = orjson.loads(line)
        except orjson.JSONDecodeError:
            continue

        supporting = [str(x) for x in obj.get("supporting_topic_names", [])]
        # Only keep links to topics the stage actually saw — protects against
        # the model hallucinating a citation.
        supporting = [name for name in supporting if name in topic_name_set]

        try:
            category = HouseCategory(
                label=str(obj.get("label", "")).strip(),
                confidence=float(obj.get("confidence", 0.5)),
                scope=Scope(obj.get("scope", "IN_HOUSE")),
                supportingTopicNames=supporting,
            )
        except ValueError:
            continue

        if category.label:
            results.append(category)

    results.sort(key=lambda c: c.confidence, reverse=True)
    return results[: SETTINGS.pipeline.top_k_categories]
