"""Stage-3 worker: category classification + final result publish.

This is the terminating worker: it does NOT hand off to another stream, it
writes the completed `CategorizationResult` to the key the Kotlin client is
polling. Matches the Redis-streams contract in
`RedisStreamHouseCategorizationProvider`.
"""

from __future__ import annotations

import logging
import socket
import time

import orjson
import redis

from config import SETTINGS
from pipeline.schemas import (
    CategorizationResult,
    StageEnvelope,
)
from pipeline.stage3_classify import classify_categories

logger = logging.getLogger(__name__)


class Stage3Worker:
    def __init__(self) -> None:
        self.redis = redis.Redis.from_url(SETTINGS.redis.url, decode_responses=True)
        self.stream = SETTINGS.redis.stage3_stream
        self.group = SETTINGS.redis.consumer_group
        self.consumer = f"stage3-{socket.gethostname()}"
        try:
            self.redis.xgroup_create(self.stream, self.group, id="$", mkstream=True)
        except redis.ResponseError as exc:
            if "BUSYGROUP" not in str(exc):
                raise

    def run(self) -> None:
        logger.info("stage3: joined group %s on %s", self.group, self.stream)
        while True:
            entries = self.redis.xreadgroup(
                self.group,
                self.consumer,
                {self.stream: ">"},
                count=1,
                block=SETTINGS.redis.read_block_ms,
            )
            if not entries:
                continue
            for _stream, messages in entries:
                for message_id, fields in messages:
                    try:
                        self._handle(fields)
                    finally:
                        self.redis.xack(self.stream, self.group, message_id)

    def _handle(self, fields: dict) -> None:
        raw = fields.get("envelope")
        if not raw:
            return

        envelope = StageEnvelope(**orjson.loads(raw))
        categories = classify_categories(envelope.request, envelope.topics or [])

        result = CategorizationResult(
            houseId=envelope.request.houseId,
            inputContentHash=envelope.request.contentHash,
            pipelineVersion=envelope.pipeline_version,
            modelVersion=envelope.model_version,
            insights=envelope.insights or [],
            topics=envelope.topics or [],
            categories=categories,
            producedAt=int(time.time() * 1000),
        )

        key = f"{SETTINGS.redis.result_key_prefix}{result.houseId}"
        self.redis.set(key, result.model_dump_json(), ex=3600)


def main() -> None:
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")
    Stage3Worker().run()


if __name__ == "__main__":
    main()
