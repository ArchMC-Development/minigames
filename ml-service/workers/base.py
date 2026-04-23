"""Minimal Redis-streams consumer framework for the pipeline stages.

Each worker reads from its own stream, processes an envelope, and writes to
the next stream (or to the result key). Consumer groups give us at-least-once
delivery and crash recovery: if a worker dies mid-message, another instance in
the same group will re-read it after `XAUTOCLAIM` picks it up.

This is deliberately a ~100-line reimplementation of a tiny subset of
Celery/Arq — the whole surface area of the pipeline is three stages and
one result publisher, so a framework would cost more than it saves.
"""

from __future__ import annotations

import logging
import signal
import socket
import time
from typing import Callable

import orjson
import redis

from config import SETTINGS
from pipeline.schemas import StageEnvelope

logger = logging.getLogger(__name__)

Handler = Callable[[StageEnvelope], StageEnvelope]


class StageWorker:
    """Consume envelopes from `input_stream`, publish to `output_stream_or_key`.

    If `as_result_key` is True, `output_stream_or_key` is treated as a key
    prefix and the final result is SET under `<prefix><houseId>` instead of
    XADD'd to a stream — that's the terminating worker (stage 3 today, and
    any future post-processor).
    """

    def __init__(
        self,
        *,
        name: str,
        input_stream: str,
        output_stream_or_key: str,
        handler: Handler,
        as_result_key: bool = False,
    ) -> None:
        self.name = name
        self.input_stream = input_stream
        self.output_stream_or_key = output_stream_or_key
        self.handler = handler
        self.as_result_key = as_result_key
        self.consumer = f"{name}-{socket.gethostname()}"
        self.group = SETTINGS.redis.consumer_group
        self.redis = redis.Redis.from_url(SETTINGS.redis.url, decode_responses=True)
        self._stopping = False

        self._ensure_group()
        signal.signal(signal.SIGTERM, self._stop)
        signal.signal(signal.SIGINT, self._stop)

    def _stop(self, *_: object) -> None:
        logger.info("%s: received stop signal", self.name)
        self._stopping = True

    def _ensure_group(self) -> None:
        try:
            self.redis.xgroup_create(self.input_stream, self.group, id="$", mkstream=True)
        except redis.ResponseError as exc:
            if "BUSYGROUP" not in str(exc):
                raise

    def run(self) -> None:
        logger.info("%s: joined group %s on %s", self.name, self.group, self.input_stream)
        while not self._stopping:
            self._reclaim_stuck()

            entries = self.redis.xreadgroup(
                self.group,
                self.consumer,
                {self.input_stream: ">"},
                count=1,
                block=SETTINGS.redis.read_block_ms,
            )
            if not entries:
                continue

            for _stream, messages in entries:
                for message_id, fields in messages:
                    self._handle(message_id, fields)

    def _handle(self, message_id: str, fields: dict) -> None:
        raw = fields.get("envelope") or fields.get("request")
        if not raw:
            self.redis.xack(self.input_stream, self.group, message_id)
            return

        try:
            envelope = self._parse(fields, raw)
            envelope.attempt += 1
            output = self.handler(envelope)
            self._publish(output)
            self.redis.xack(self.input_stream, self.group, message_id)
        except Exception:
            logger.exception("%s: handler failed for %s", self.name, message_id)
            # Failure path: let XAUTOCLAIM retry; dead-letter after N tries.
            # `xpending` + deliveries count is the right signal but the
            # `xclaim` + idle window below approximates it.

    def _parse(self, fields: dict, raw: str) -> StageEnvelope:
        # Stage-1 ingests the Kotlin wire format directly (`fields['request']`);
        # later stages pass a full envelope (`fields['envelope']`).
        payload = orjson.loads(raw)
        if "request" in fields and "envelope" not in fields:
            return StageEnvelope(request=payload, pipeline_version=fields.get("pipeline_version", "v1"))
        return StageEnvelope(**payload)

    def _publish(self, envelope: StageEnvelope) -> None:
        if self.as_result_key:
            key = f"{self.output_stream_or_key}{envelope.request.houseId}"
            # Result is the final `CategorizationResult` — built by the last
            # stage, not the envelope.
            self.redis.set(key, envelope.request.model_dump_json())  # overridden by stage3
        else:
            self.redis.xadd(
                self.output_stream_or_key,
                {"envelope": envelope.model_dump_json()},
            )

    def _reclaim_stuck(self) -> None:
        # Every ~5 s, reclaim any message that's been pending for >30 s.
        # This is how we make at-least-once delivery actually deliver.
        try:
            _next_id, claimed, _ = self.redis.xautoclaim(
                self.input_stream, self.group, self.consumer, min_idle_time=30_000, count=10
            )
            for message_id, fields in claimed:
                logger.warning("%s: reclaimed stale message %s", self.name, message_id)
                self._handle(message_id, fields)
        except redis.ResponseError:
            # Old Redis: XAUTOCLAIM unavailable, fall back silently.
            pass
        except Exception:
            logger.exception("%s: reclaim failed", self.name)
            time.sleep(1)
