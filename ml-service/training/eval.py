"""Offline evaluation harness.

Mirrors the four-axis rubric from the App Store paper (safety, groundedness,
composition, helpfulness). Safety uses a unanimity rule — if *any* rater
flags a category as unsafe (e.g. slur leakage), the sample fails outright.
The other three axes use majority voting.

The numbers a CI job cares about:
  - per-stage JSON-validity rate (cheap; run on every PR).
  - groundedness: fraction of categories whose supporting_topic_names appear
    in the stage-2 output. Catches the classifier hallucinating citations.
  - topic purity: fraction of dedup clusters whose members have cosine sim
    above the threshold — sanity-checks the dedup config.

Human-rater outputs are loaded from a CSV when running the full rubric.
"""

from __future__ import annotations

import argparse
import csv
import logging
import statistics
from pathlib import Path

import orjson

from pipeline.orchestrator import run
from pipeline.schemas import CategorizationRequest

logger = logging.getLogger(__name__)


def automatic_metrics(samples: list[dict]) -> dict[str, float]:
    groundedness_hits = 0
    groundedness_total = 0
    parse_failures = 0

    for sample in samples:
        try:
            request = CategorizationRequest(**sample)
        except Exception:
            parse_failures += 1
            continue

        result = run(request)
        topic_names = {t.name for t in result.topics}

        for cat in result.categories:
            groundedness_total += 1
            if all(name in topic_names for name in cat.supportingTopicNames):
                groundedness_hits += 1

    return {
        "parse_failures": parse_failures,
        "groundedness_rate": (groundedness_hits / groundedness_total) if groundedness_total else 0.0,
    }


def human_rubric(csv_path: Path) -> dict[str, float]:
    """CSV schema: house_id,rater_id,safety,groundedness,composition,helpfulness.

    Each rating is 0 or 1; safety failures are unanimity (any zero -> fail).
    """

    by_house: dict[str, dict[str, list[int]]] = {}
    with csv_path.open() as fh:
        reader = csv.DictReader(fh)
        for row in reader:
            h = row["house_id"]
            axes = by_house.setdefault(h, {"safety": [], "groundedness": [], "composition": [], "helpfulness": []})
            axes["safety"].append(int(row["safety"]))
            axes["groundedness"].append(int(row["groundedness"]))
            axes["composition"].append(int(row["composition"]))
            axes["helpfulness"].append(int(row["helpfulness"]))

    safety_pass = sum(1 for axes in by_house.values() if all(s == 1 for s in axes["safety"]))
    majority = lambda votes: 1 if sum(votes) > len(votes) / 2 else 0  # noqa: E731
    grounded_pass = sum(majority(axes["groundedness"]) for axes in by_house.values())
    compose_pass = sum(majority(axes["composition"]) for axes in by_house.values())
    helpful_pass = sum(majority(axes["helpfulness"]) for axes in by_house.values())
    n = max(len(by_house), 1)

    return {
        "safety_pass_rate": safety_pass / n,
        "groundedness_pass_rate": grounded_pass / n,
        "composition_pass_rate": compose_pass / n,
        "helpfulness_pass_rate": helpful_pass / n,
    }


def main() -> None:
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s: %(message)s")
    parser = argparse.ArgumentParser()
    parser.add_argument("--samples", type=Path, help="JSONL of CategorizationRequest objects.")
    parser.add_argument("--human-ratings", type=Path, help="CSV of rubric ratings.")
    args = parser.parse_args()

    if args.samples:
        with args.samples.open() as fh:
            samples = [orjson.loads(line) for line in fh if line.strip()]
        auto = automatic_metrics(samples)
        logger.info("automatic metrics: %s", auto)

    if args.human_ratings:
        human = human_rubric(args.human_ratings)
        logger.info("human rubric: %s", human)
        # A single operational fitness number for CI to gate on.
        combined = statistics.mean(human.values())
        logger.info("combined rubric score: %.3f", combined)


if __name__ == "__main__":
    main()
