"""Deterministic heuristic stand-in for the LoRA-tuned Gemma.

Activated by `HOUSING_ML_MOCK=1`. Lets you smoke-test the full pipeline —
Redis streams, workers, gateway, Kotlin round-trip — without downloading
any model weights. Output shape matches the real fine-tuned model exactly
(same prompt -> same JSONL schema), so the parsers in `pipeline/stage*`
exercise the production code path.

DO NOT ship this as a production path. Category quality is trivial.
"""

from __future__ import annotations

import json
import re
from typing import Iterable

# Very small keyword->category table. Enough to prove the pipeline works end
# to end on realistic house text. Extend freely for local demos.
KEYWORDS: dict[str, list[str]] = {
    "Parkour":   ["parkour", "jump", "obstacle", "ninja", "hop"],
    "PvP":       ["pvp", "fight", "arena", "duel", "kitpvp", "battle"],
    "Roleplay":  ["rp", "roleplay", "village", "kingdom", "town", "medieval"],
    "Build":     ["build", "showcase", "creative", "architecture"],
    "Hub":       ["hub", "lobby", "spawn", "portal"],
    "Adventure": ["adventure", "quest", "dungeon", "explore", "mystery"],
    "Art":       ["pixel", "art", "gallery", "statue", "mural"],
    "Redstone":  ["redstone", "circuit", "contraption", "piston", "computer"],
    "Hangout":   ["hangout", "chill", "friends", "chat", "party"],
    "Minigame":  ["minigame", "game", "bedwars", "skywars", "sumo"],
}

META_LABELS = {
    "Friends-Only": ["friends", "private", "whitelist"],
    "Showcase":     ["showcase", "gallery", "exhibit"],
    "For-Sale":     ["for sale", "buy", "purchase"],
}


def _assemble_input_text(prompt: str) -> str:
    # Pipeline prompts carry the full user section after "<|user|>".
    user = prompt.split("<|user|>", 1)[-1].split("<|assistant|>", 1)[0]
    return user.lower()


def _stage1(prompt: str) -> str:
    text = _assemble_input_text(prompt)
    lines: list[str] = []
    seen: set[str] = set()
    for category, keywords in {**KEYWORDS, **META_LABELS}.items():
        for kw in keywords:
            if kw in text and kw not in seen:
                seen.add(kw)
                lines.append(json.dumps({
                    "statement": f"Realm mentions {kw} which implies {category}-style content.",
                    "sentiment": "DESCRIPTIVE",
                    "confidence": 0.82,
                    "source_field": "mixed",
                }))
                break
    return "\n".join(lines)


def _stage2(prompt: str) -> str:
    # Parse the numbered insights back out of the prompt body.
    body = prompt.split("Insights:", 1)[-1]
    entries = re.findall(r"\[(\d+)\]\s+\(\w+\)\s+(.+)", body)
    topic_to_indices: dict[str, list[int]] = {}
    for idx_str, stmt in entries:
        idx = int(idx_str)
        lowered = stmt.lower()
        matched: str | None = None
        for category, keywords in {**KEYWORDS, **META_LABELS}.items():
            if any(kw in lowered for kw in keywords):
                matched = category
                break
        if matched:
            topic_to_indices.setdefault(matched, []).append(idx)

    lines = [
        json.dumps({
            "name": topic,
            "supporting_insight_indices": indices,
            "aggregate_confidence": 0.78,
        })
        for topic, indices in topic_to_indices.items()
    ]
    return "\n".join(lines)


def _stage3(prompt: str) -> str:
    body = prompt.split("Topics:", 1)[-1]
    topics = re.findall(r"-\s+([^(]+)\s+\(conf", body)
    lines = []
    for topic in topics:
        t = topic.strip()
        scope = "META" if t in META_LABELS else "IN_HOUSE"
        conf = 0.85 if scope == "IN_HOUSE" else 0.6
        lines.append(json.dumps({
            "label": t,
            "confidence": conf,
            "scope": scope,
            "supporting_topic_names": [t],
        }))
    return "\n".join(lines)


class MockLlmRunner:
    """Drop-in replacement for `models.loader.LlmRunner`."""

    def __init__(self, adapter_env_var: str | None) -> None:
        self.adapter_env_var = adapter_env_var

    def generate(self, prompt: str, *, max_new_tokens: int | None = None, temperature: float | None = None) -> str:
        if self.adapter_env_var == "insight":
            return _stage1(prompt)
        if self.adapter_env_var == "topic":
            return _stage2(prompt)
        if self.adapter_env_var == "classify":
            return _stage3(prompt)
        return ""
