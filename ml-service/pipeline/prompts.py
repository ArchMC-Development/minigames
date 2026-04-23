"""Prompt templates for each fine-tuned stage.

Kept in one file so the training data formatters in `training/data_templates.py`
can import the identical strings — train-inference skew hides in mismatched
prompts more than anywhere else.
"""

from __future__ import annotations

from pipeline.schemas import CategorizationRequest, HouseInsight, HouseTopic


STAGE1_SYSTEM = (
    "You extract atomic descriptive statements from a Minecraft realm's textual fields.\n"
    "Each statement must cover ONE observable aspect of the realm (build theme, gameplay\n"
    "mode, aesthetic, intended audience, notable features). Output strict JSON lines —\n"
    "one object per line with keys: statement, sentiment, confidence, source_field.\n"
    "sentiment ∈ {POSITIVE, NEGATIVE, NEUTRAL, DESCRIPTIVE}. confidence ∈ [0,1].\n"
    "Do not invent facts that are not implied by the input."
)


STAGE2_SYSTEM = (
    "You convert raw insights about a Minecraft realm into canonical topic names.\n"
    "Collapse synonyms (e.g. 'parkour course' and 'obstacle jumping' -> 'Parkour').\n"
    "Output JSON lines with keys: name, supporting_insight_indices, aggregate_confidence.\n"
    "Prefer common, player-recognizable topic names. Never output more than 8 topics."
)


STAGE3_SYSTEM = (
    "You assign Minecraft-realm categories from a list of normalized topics.\n"
    "Each category MUST declare scope=IN_HOUSE if it describes the realm itself\n"
    "(gameplay, aesthetic, build type) or scope=META if it describes meta/social aspects\n"
    "(friends-only, staff showcase, for-sale). IN_HOUSE categories are what players\n"
    "search on; META is deprioritized. Output JSON lines with keys:\n"
    "label, confidence, scope, supporting_topic_names."
)


def render_stage1_prompt(req: CategorizationRequest) -> str:
    body = _bullet_section("Display name", [req.displayName])
    body += _bullet_section("Description", req.description)
    body += _bullet_section("Tags", req.tags)
    body += _bullet_section("NPC dialog", req.npcText)
    body += _bullet_section("Hologram lines", req.hologramText)
    return f"<|system|>\n{STAGE1_SYSTEM}\n<|user|>\n{body}\n<|assistant|>\n"


def render_stage2_prompt(req: CategorizationRequest, insights: list[HouseInsight]) -> str:
    numbered = "\n".join(
        f"[{i}] ({ins.sentiment}) {ins.statement}" for i, ins in enumerate(insights)
    )
    return (
        f"<|system|>\n{STAGE2_SYSTEM}\n<|user|>\n"
        f"Realm: {req.displayName}\nInsights:\n{numbered}\n<|assistant|>\n"
    )


def render_stage3_prompt(req: CategorizationRequest, topics: list[HouseTopic]) -> str:
    numbered = "\n".join(
        f"- {t.name} (conf {t.aggregateConfidence:.2f})" for t in topics
    )
    return (
        f"<|system|>\n{STAGE3_SYSTEM}\n<|user|>\n"
        f"Realm: {req.displayName}\nTopics:\n{numbered}\n<|assistant|>\n"
    )


def _bullet_section(label: str, items: list[str]) -> str:
    filtered = [x for x in items if x and x.strip()]
    if not filtered:
        return f"{label}: (none)\n"
    bullets = "\n".join(f"- {x}" for x in filtered)
    return f"{label}:\n{bullets}\n"
