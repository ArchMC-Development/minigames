"""Helpers for formatting SFT and DPO training examples.

Format is the *same* prompt template the pipeline uses at inference time — see
`pipeline/prompts.py`. Do not introduce train-inference skew here.
"""

from __future__ import annotations

from typing import Iterable, TypedDict

import orjson

from pipeline.prompts import (
    render_stage1_prompt,
    render_stage2_prompt,
    render_stage3_prompt,
)
from pipeline.schemas import (
    CategorizationRequest,
    HouseCategory,
    HouseInsight,
    HouseTopic,
)


class SftExample(TypedDict):
    prompt: str
    completion: str


class DpoExample(TypedDict):
    prompt: str
    chosen: str
    rejected: str


def stage1_sft(request: CategorizationRequest, insights: Iterable[HouseInsight]) -> SftExample:
    return {
        "prompt": render_stage1_prompt(request),
        "completion": _as_jsonl(
            {
                "statement": ins.statement,
                "sentiment": ins.sentiment.value,
                "confidence": ins.confidence,
                "source_field": ins.sourceField,
            }
            for ins in insights
        ),
    }


def stage2_sft(
    request: CategorizationRequest,
    insights: list[HouseInsight],
    topics: Iterable[HouseTopic],
) -> SftExample:
    return {
        "prompt": render_stage2_prompt(request, insights),
        "completion": _as_jsonl(
            {
                "name": t.name,
                "supporting_insight_indices": t.supportingInsightIndices,
                "aggregate_confidence": t.aggregateConfidence,
            }
            for t in topics
        ),
    }


def stage3_sft(
    request: CategorizationRequest,
    topics: list[HouseTopic],
    categories: Iterable[HouseCategory],
) -> SftExample:
    return {
        "prompt": render_stage3_prompt(request, topics),
        "completion": _as_jsonl(
            {
                "label": c.label,
                "confidence": c.confidence,
                "scope": c.scope.value,
                "supporting_topic_names": c.supportingTopicNames,
            }
            for c in categories
        ),
    }


def _as_jsonl(objects: Iterable[dict]) -> str:
    return "\n".join(orjson.dumps(o).decode("utf-8") for o in objects)
