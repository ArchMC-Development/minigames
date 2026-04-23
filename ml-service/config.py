"""Central config — every knob an operator touches lives here.

Defaults intentionally point at a *small* Gemma variant. The whole pipeline is
designed around a model that can serve from a single modest GPU (or even CPU
with quantization) so a Minecraft network can actually run this in-house
without blowing an ML budget.
"""

from __future__ import annotations

import os
from dataclasses import dataclass, field


def _env(name: str, default: str) -> str:
    return os.environ.get(name, default)


@dataclass(frozen=True)
class RedisConfig:
    url: str = field(default_factory=lambda: _env("REDIS_URL", "redis://localhost:6379/0"))
    stage1_stream: str = "housing:categorize:stage1"
    stage2_stream: str = "housing:categorize:stage2"
    stage3_stream: str = "housing:categorize:stage3"
    result_key_prefix: str = "housing:categorize:result:"
    consumer_group: str = "housing-ml"
    # Block reads for up to this many ms — tune with volume.
    read_block_ms: int = 5000
    # After N failed deliveries a message is routed to the dead-letter stream.
    max_deliveries: int = 5
    deadletter_stream: str = "housing:categorize:deadletter"


@dataclass(frozen=True)
class ModelConfig:
    # `google/gemma-3-1b-it` is the default. A fine-tuned adapter produced by
    # `training/sft_train.py` is loaded on top via PEFT if present.
    base_model: str = field(default_factory=lambda: _env("HOUSING_ML_BASE_MODEL", "google/gemma-3-1b-it"))
    insight_adapter: str | None = field(default_factory=lambda: os.environ.get("HOUSING_ML_INSIGHT_ADAPTER"))
    topic_adapter: str | None = field(default_factory=lambda: os.environ.get("HOUSING_ML_TOPIC_ADAPTER"))
    classify_adapter: str | None = field(default_factory=lambda: os.environ.get("HOUSING_ML_CLASSIFY_ADAPTER"))
    embedding_model: str = field(
        default_factory=lambda: _env("HOUSING_ML_EMBED_MODEL", "sentence-transformers/all-MiniLM-L6-v2")
    )
    dtype: str = field(default_factory=lambda: _env("HOUSING_ML_DTYPE", "bfloat16"))
    device: str = field(default_factory=lambda: _env("HOUSING_ML_DEVICE", "auto"))
    load_in_4bit: bool = field(default_factory=lambda: _env("HOUSING_ML_4BIT", "0") == "1")
    max_new_tokens: int = 256
    temperature: float = 0.2


@dataclass(frozen=True)
class PipelineConfig:
    pipeline_version: str = "v1"
    model_version: str = field(default_factory=lambda: _env("HOUSING_ML_MODEL_VERSION", "base"))
    # Insights with confidence below this are dropped before stage 2.
    insight_confidence_floor: float = 0.35
    # Cosine-similarity above this merges two insights into one topic.
    dedup_similarity: float = 0.82
    # Maximum categories emitted per house.
    top_k_categories: int = 4


@dataclass(frozen=True)
class Settings:
    redis: RedisConfig = field(default_factory=RedisConfig)
    model: ModelConfig = field(default_factory=ModelConfig)
    pipeline: PipelineConfig = field(default_factory=PipelineConfig)
    http_port: int = 8080


SETTINGS = Settings()
