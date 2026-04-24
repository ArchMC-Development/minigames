"""FastAPI gateway.

Two endpoints:
  POST /v1/categorize — synchronous, runs the in-process orchestrator. Useful
    for single-pod deployments or CI. Matches the Kotlin
    `HttpHouseCategorizationProvider` contract.
  POST /v1/categorize/async — publishes to `stage1_stream` and returns 202.
    The Kotlin `RedisStreamHouseCategorizationProvider` can use either route;
    the async one lets a caller bypass the gateway entirely.

Operational endpoints:
  GET  /healthz  — liveness
  GET  /metrics  — prometheus
"""

from __future__ import annotations

import logging

import orjson
import redis
from fastapi import FastAPI, HTTPException
from fastapi.responses import Response
from prometheus_client import CONTENT_TYPE_LATEST, Counter, Histogram, generate_latest

from config import SETTINGS
from pipeline.orchestrator import run
from pipeline.schemas import CategorizationRequest, CategorizationResult, StageEnvelope

logger = logging.getLogger(__name__)

app = FastAPI(title="housing-ml gateway", version="0.1.0")

_redis: redis.Redis | None = None


def _get_redis() -> redis.Redis:
    global _redis
    if _redis is None:
        _redis = redis.Redis.from_url(SETTINGS.redis.url, decode_responses=True)
    return _redis


REQUESTS = Counter("housing_ml_requests_total", "categorization requests", ["route", "status"])
LATENCY = Histogram("housing_ml_latency_seconds", "in-process pipeline latency seconds")


@app.get("/healthz")
def healthz() -> dict[str, str]:
    return {"status": "ok", "pipeline_version": SETTINGS.pipeline.pipeline_version}


@app.get("/metrics")
def metrics() -> Response:
    return Response(generate_latest(), media_type=CONTENT_TYPE_LATEST)


@app.post("/v1/categorize", response_model=CategorizationResult)
def categorize(request: CategorizationRequest) -> CategorizationResult:
    try:
        with LATENCY.time():
            result = run(request)
        REQUESTS.labels("sync", "200").inc()
        return result
    except Exception as exc:
        REQUESTS.labels("sync", "500").inc()
        logger.exception("sync categorize failed for %s", request.houseId)
        raise HTTPException(status_code=500, detail=str(exc)) from exc


@app.post("/v1/categorize/async", status_code=202)
def categorize_async(request: CategorizationRequest) -> dict[str, str]:
    envelope = StageEnvelope(request=request, pipeline_version=SETTINGS.pipeline.pipeline_version)
    _get_redis().xadd(
        SETTINGS.redis.stage1_stream,
        {"envelope": envelope.model_dump_json()},
    )
    REQUESTS.labels("async", "202").inc()
    return {
        "accepted": request.houseId,
        "result_key": f"{SETTINGS.redis.result_key_prefix}{request.houseId}",
    }


def main() -> None:
    import uvicorn

    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")
    uvicorn.run(
        "gateway.server:app",
        host="0.0.0.0",
        port=SETTINGS.http_port,
        log_level="info",
    )


if __name__ == "__main__":
    main()
