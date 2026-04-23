"""Sentence-embedding wrapper used by the stage-2 topic deduplicator."""

from __future__ import annotations

from functools import lru_cache

import numpy as np
from sentence_transformers import SentenceTransformer

from config import SETTINGS


@lru_cache(maxsize=1)
def get_encoder() -> SentenceTransformer:
    return SentenceTransformer(SETTINGS.model.embedding_model)


def encode(texts: list[str]) -> np.ndarray:
    if not texts:
        return np.zeros((0, 384), dtype=np.float32)
    vectors = get_encoder().encode(texts, normalize_embeddings=True, convert_to_numpy=True)
    return vectors.astype(np.float32)


def cosine_matrix(a: np.ndarray, b: np.ndarray) -> np.ndarray:
    if a.size == 0 or b.size == 0:
        return np.zeros((a.shape[0], b.shape[0]), dtype=np.float32)
    return a @ b.T
