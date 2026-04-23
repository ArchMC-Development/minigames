"""Run the full pipeline in-process.

Used by:
  - the FastAPI gateway's /v1/categorize endpoint (single-pod deployment);
  - smoke-test scripts;
  - eval (`training/eval.py`).

The distributed path uses the same three stage functions but glued together
by Redis streams in `workers/`.
"""

from __future__ import annotations

import time

from config import SETTINGS
from pipeline.schemas import CategorizationRequest, CategorizationResult
from pipeline.stage1_insights import extract_insights
from pipeline.stage2_topics import normalize_topics
from pipeline.stage3_classify import classify_categories


def run(request: CategorizationRequest) -> CategorizationResult:
    insights = extract_insights(request)
    topics = normalize_topics(request, insights)
    categories = classify_categories(request, topics)

    return CategorizationResult(
        houseId=request.houseId,
        inputContentHash=request.contentHash,
        pipelineVersion=SETTINGS.pipeline.pipeline_version,
        modelVersion=SETTINGS.pipeline.model_version,
        insights=insights,
        topics=topics,
        categories=categories,
        producedAt=int(time.time() * 1000),
    )
