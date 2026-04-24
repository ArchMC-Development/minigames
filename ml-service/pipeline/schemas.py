"""Pydantic mirrors of the Kotlin DTOs.

Field names are kept in strict sync with the Kotlin data classes so the same
JSON round-trips without a translation layer. Any rename here is a wire-format
break — run the integration test before merging.
"""

from __future__ import annotations

from enum import Enum
from typing import Optional

from pydantic import BaseModel, Field


class CategorizationRequest(BaseModel):
    houseId: str
    contentHash: str
    displayName: str
    description: list[str] = Field(default_factory=list)
    tags: list[str] = Field(default_factory=list)
    npcText: list[str] = Field(default_factory=list)
    hologramText: list[str] = Field(default_factory=list)
    submittedAt: int


class Sentiment(str, Enum):
    POSITIVE = "POSITIVE"
    NEGATIVE = "NEGATIVE"
    NEUTRAL = "NEUTRAL"
    DESCRIPTIVE = "DESCRIPTIVE"


class HouseInsight(BaseModel):
    statement: str
    sentiment: Sentiment
    confidence: float
    sourceField: str


class HouseTopic(BaseModel):
    name: str
    supportingInsightIndices: list[int]
    aggregateConfidence: float


class Scope(str, Enum):
    IN_HOUSE = "IN_HOUSE"
    META = "META"


class HouseCategory(BaseModel):
    label: str
    confidence: float
    scope: Scope
    supportingTopicNames: list[str]


class CategorizationResult(BaseModel):
    houseId: str
    inputContentHash: str
    pipelineVersion: str
    modelVersion: str
    insights: list[HouseInsight]
    topics: list[HouseTopic]
    categories: list[HouseCategory]
    producedAt: int


class StageEnvelope(BaseModel):
    """What the Redis stream carries between stages.

    Stage N reads [stageN-1] output, writes [stageN] output, and hands the
    full envelope onward. The envelope accretes — stage 3 sees everything
    stages 1 and 2 produced, which is how the final result can cite the
    supporting topics and insights.
    """

    request: CategorizationRequest
    insights: Optional[list[HouseInsight]] = None
    topics: Optional[list[HouseTopic]] = None
    categories: Optional[list[HouseCategory]] = None
    pipeline_version: str = "v1"
    model_version: str = "base"
    attempt: int = 0
