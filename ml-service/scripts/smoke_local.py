"""End-to-end smoke test with the mock runner.

No GPU, no Redis, no Gemma download. Reads sample houses, runs the full
in-process orchestrator, and prints the derived categories. Passes CI with
just the base deps installed.

Usage:
    HOUSING_ML_MOCK=1 python scripts/smoke_local.py
"""

from __future__ import annotations

import json
import os
import sys
from pathlib import Path


def main() -> int:
    os.environ.setdefault("HOUSING_ML_MOCK", "1")

    # Make repo root importable when invoked as `python scripts/smoke_local.py`.
    sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

    from pipeline.orchestrator import run  # imported after env set + path fix
    from pipeline.schemas import CategorizationRequest

    # Local embedder path — sentence-transformers auto-downloads MiniLM on
    # first use (~80 MB). If you can't download, replace the dedup with a
    # trivial pass-through; the mock model already emits canonical names.
    samples_path = Path(__file__).resolve().parent.parent / "training" / "data" / "sample_houses.jsonl"

    all_ok = True
    with samples_path.open() as fh:
        for line in fh:
            line = line.strip()
            if not line:
                continue
            request = CategorizationRequest(**json.loads(line))
            result = run(request)
            primary = next((c for c in result.categories if c.scope == "IN_HOUSE"), None)
            label = primary.label if primary else "(none)"
            conf = f"{primary.confidence:.2f}" if primary else "-"
            all_ok = all_ok and bool(result.categories)
            print(
                f"{request.displayName:32s} -> {label:16s} (conf {conf}) "
                f"[topics: {', '.join(t.name for t in result.topics) or 'none'}]"
            )

    if not all_ok:
        print("FAIL: at least one sample produced no categories", file=sys.stderr)
        return 1
    print("\nOK: all samples categorized.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
