#!/usr/bin/env python3
"""Validate build artifacts expected by Sky Vault's CI workflows."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path


EXPECTED = {
    "observatory": (
        "sky-observatory/build/outputs/apk/debug/*.apk",
        "sky-observatory/build/outputs/apk/release/*.apk",
    ),
    "sample-test": ("sample-test/build/outputs/apk/debug/*.apk",),
    "benchmark": ("benchmark/build/outputs/aar/*.aar",),
    "sdk": (
        "api/build/outputs/aar/*.aar",
        "engine/build/outputs/aar/*.aar",
        "native/build/outputs/aar/*.aar",
    ),
}


def collect(root: Path, kind: str) -> list[dict[str, object]]:
    artifacts = []
    for pattern in EXPECTED[kind]:
        matches = sorted(root.glob(pattern))
        if not matches:
            raise FileNotFoundError(f"no artifact matched {pattern}")
        for path in matches:
            size = path.stat().st_size
            if size == 0:
                raise ValueError(f"artifact is empty: {path}")
            artifacts.append({"path": str(path), "bytes": size})
    return artifacts


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("kind", choices=sorted(EXPECTED))
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--json", action="store_true")
    args = parser.parse_args()

    try:
        artifacts = collect(args.root, args.kind)
    except (OSError, ValueError, FileNotFoundError) as error:
        print(f"error: {error}", file=sys.stderr)
        return 1

    if args.json:
        print(json.dumps({"kind": args.kind, "artifacts": artifacts}, indent=2))
    else:
        for artifact in artifacts:
            print(f"{artifact['path']}: {artifact['bytes']} bytes")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
