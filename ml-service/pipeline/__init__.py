"""Multi-stage pipeline: insight extraction -> topic normalization -> classification.

Mirrors the App Store review summarization architecture from Apple ML research
(2024), adapted to house textual metadata instead of reviews. Each stage is a
pure function over its input DTO — the same code path powers the in-process
orchestrator and the per-stage Redis-streams workers.
"""
