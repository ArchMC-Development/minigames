"""Stage-2 worker: topic normalization + embedding dedup."""

from __future__ import annotations

import logging

from config import SETTINGS
from pipeline.schemas import StageEnvelope
from pipeline.stage2_topics import normalize_topics
from workers.base import StageWorker

logger = logging.getLogger(__name__)


def handle(envelope: StageEnvelope) -> StageEnvelope:
    envelope.topics = normalize_topics(envelope.request, envelope.insights or [])
    return envelope


def main() -> None:
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")
    StageWorker(
        name="stage2",
        input_stream=SETTINGS.redis.stage2_stream,
        output_stream_or_key=SETTINGS.redis.stage3_stream,
        handler=handle,
    ).run()


if __name__ == "__main__":
    main()
