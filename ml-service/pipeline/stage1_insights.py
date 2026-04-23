"""Stage 1 — Insight extraction.

Fine-tuned Gemma adapter distills the textual surface of a realm into atomic
statements. This mirrors the App Store paper's insight stage: one topic, one
statement, one sentiment label, no multi-claim sentences.
"""

from __future__ import annotations

import logging

import orjson

from config import SETTINGS
from models.loader import get_runner
from pipeline.prompts import render_stage1_prompt
from pipeline.schemas import CategorizationRequest, HouseInsight, Sentiment

logger = logging.getLogger(__name__)


def extract_insights(req: CategorizationRequest) -> list[HouseInsight]:
    prompt = render_stage1_prompt(req)
    raw = get_runner("insight").generate(prompt)
    floor = SETTINGS.pipeline.insight_confidence_floor

    insights: list[HouseInsight] = []
    for line in raw.splitlines():
        line = line.strip()
        if not line or not line.startswith("{"):
            continue
        try:
            obj = orjson.loads(line)
        except orjson.JSONDecodeError:
            logger.debug("stage1: dropping malformed line %r", line)
            continue

        try:
            insight = HouseInsight(
                statement=str(obj["statement"]).strip(),
                sentiment=Sentiment(obj.get("sentiment", "DESCRIPTIVE")),
                confidence=float(obj.get("confidence", 0.5)),
                sourceField=str(obj.get("source_field", "mixed")),
            )
        except (KeyError, ValueError) as exc:
            logger.debug("stage1: dropping invalid object %r (%s)", obj, exc)
            continue

        if insight.confidence >= floor and insight.statement:
            insights.append(insight)

    return insights
