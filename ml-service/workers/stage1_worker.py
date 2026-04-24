"""Stage-1 worker: insight extraction. Scale this horizontally on GPU pods."""

from __future__ import annotations

import logging

from config import SETTINGS
from pipeline.schemas import StageEnvelope
from pipeline.stage1_insights import extract_insights
from workers.base import StageWorker

logger = logging.getLogger(__name__)


def handle(envelope: StageEnvelope) -> StageEnvelope:
    envelope.insights = extract_insights(envelope.request)
    envelope.model_version = SETTINGS.pipeline.model_version
    return envelope


def main() -> None:
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")
    StageWorker(
        name="stage1",
        input_stream=SETTINGS.redis.stage1_stream,
        output_stream_or_key=SETTINGS.redis.stage2_stream,
        handler=handle,
    ).run()


if __name__ == "__main__":
    main()
