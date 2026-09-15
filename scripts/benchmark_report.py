#!/usr/bin/env python3
"""Parse and compare Sky Vault benchmark logs."""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any


RESULT_RE = re.compile(
    r"(?P<name>[^ ]+) iterations=(?P<iterations>\d+) "
    r"total=(?P<total>\d+)ms avg=(?P<avg>\d+)us"
)
SUCCESS_RE = re.compile(
    r"calculatePosition\((?P<name>[^)]+)\) success=(?P<success>\d+)/(?P<iterations>\d+)"
)


def parse_log(path: Path) -> dict[str, Any]:
    """Return structured benchmark results from a captured logcat file."""
    results: dict[str, dict[str, Any]] = {}
    pending_success: dict[str, tuple[int, int]] = {}

    for line in path.read_text(encoding="utf-8").splitlines():
        success_match = SUCCESS_RE.search(line)
        if success_match:
            pending_success[success_match["name"]] = (
                int(success_match["success"]),
                int(success_match["iterations"]),
            )
            continue

        result_match = RESULT_RE.search(line)
        if not result_match:
            continue

        name = result_match["name"]
        iterations = int(result_match["iterations"])
        total_ns = int(result_match["total"]) * 1_000_000
        avg_ns = int(result_match["avg"]) * 1_000
        result: dict[str, Any] = {
            "name": name,
            "iterations": iterations,
            "total_ns": total_ns,
            "avg_ns": avg_ns,
        }
        if name.startswith("calculatePosition("):
            target = name.removeprefix("calculatePosition(").removesuffix(")")
            if target in pending_success:
                success, success_iterations = pending_success[target]
                if success_iterations != iterations:
                    raise ValueError(
                        f"{name}: success and timing iteration counts differ"
                    )
                result["success"] = success
                result["success_rate"] = success / iterations
        results[name] = result

    if not results:
        raise ValueError(f"no benchmark results found in {path}")

    return {
        "schema_version": 1,
        "source": str(path),
        "benchmarks": list(results.values()),
    }


def compare_results(
    baseline: dict[str, Any], current: dict[str, Any], threshold: float
) -> tuple[list[dict[str, Any]], list[str]]:
    """Compare average timings and return rows plus validation errors."""
    baseline_by_name = {item["name"]: item for item in baseline["benchmarks"]}
    current_by_name = {item["name"]: item for item in current["benchmarks"]}
    rows: list[dict[str, Any]] = []
    errors: list[str] = []

    for name in sorted(current_by_name):
        current_item = current_by_name[name]
        baseline_item = baseline_by_name.get(name)
        if baseline_item is None:
            errors.append(f"{name}: missing from baseline")
            continue

        baseline_avg = baseline_item["avg_ns"]
        current_avg = current_item["avg_ns"]
        if baseline_avg <= 0:
            errors.append(f"{name}: baseline average time must be positive")
            continue
        change = (current_avg - baseline_avg) / baseline_avg
        row = {
            "name": name,
            "baseline_avg_ns": baseline_avg,
            "current_avg_ns": current_avg,
            "change": change,
            "regression": change > threshold,
        }
        rows.append(row)
        if row["regression"]:
            errors.append(
                f"{name}: average time increased by {change:.1%} "
                f"(threshold {threshold:.1%})"
            )

        if "success_rate" in current_item and (
            current_item["success_rate"] < 1.0
        ):
            errors.append(
                f"{name}: benchmark success rate is "
                f"{current_item['success_rate']:.1%}"
            )

    for name in sorted(set(baseline_by_name) - set(current_by_name)):
        errors.append(f"{name}: missing from current results")

    return rows, errors


def write_json(data: Any, output: Path | None) -> None:
    rendered = json.dumps(data, indent=2, sort_keys=True) + "\n"
    if output is None:
        print(rendered, end="")
    else:
        output.write_text(rendered, encoding="utf-8")


def render_comparison(rows: list[dict[str, Any]]) -> str:
    lines = [
        "| Benchmark | Baseline (us) | Current (us) | Change | Status |",
        "|---|---:|---:|---:|---|",
    ]
    for row in rows:
        status = "REGRESSION" if row["regression"] else "OK"
        lines.append(
            f"| `{row['name']}` | {row['baseline_avg_ns'] / 1_000:.1f} "
            f"| {row['current_avg_ns'] / 1_000:.1f} "
            f"| {row['change']:+.1%} | {status} |"
        )
    return "\n".join(lines) + "\n"


def command_parse(args: argparse.Namespace) -> int:
    write_json(parse_log(args.log), args.output)
    return 0


def command_compare(args: argparse.Namespace) -> int:
    baseline = json.loads(args.baseline.read_text(encoding="utf-8"))
    current = json.loads(args.current.read_text(encoding="utf-8"))
    rows, errors = compare_results(baseline, current, args.threshold)

    if args.output:
        args.output.write_text(render_comparison(rows), encoding="utf-8")
    else:
        print(render_comparison(rows), end="")

    for error in errors:
        print(f"error: {error}", file=sys.stderr)
    return 1 if errors else 0


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    subparsers = parser.add_subparsers(required=True)

    parse = subparsers.add_parser("parse", help="parse a captured benchmark log")
    parse.add_argument("log", type=Path)
    parse.add_argument("-o", "--output", type=Path)
    parse.set_defaults(func=command_parse)

    compare = subparsers.add_parser(
        "compare", help="compare two parsed benchmark JSON files"
    )
    compare.add_argument("baseline", type=Path)
    compare.add_argument("current", type=Path)
    compare.add_argument(
        "--threshold",
        type=float,
        default=0.10,
        help="maximum allowed relative slowdown (default: 0.10)",
    )
    compare.add_argument("-o", "--output", type=Path)
    compare.set_defaults(func=command_compare)
    return parser


def main() -> int:
    arguments = build_parser().parse_args()
    try:
        return arguments.func(arguments)
    except (OSError, ValueError, json.JSONDecodeError) as error:
        print(f"error: {error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
