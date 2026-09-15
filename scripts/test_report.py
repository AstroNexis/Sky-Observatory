#!/usr/bin/env python3
"""Summarize Gradle's JUnit XML test reports."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from xml.etree import ElementTree


def summarize(results_dir: Path) -> dict[str, int]:
    files = sorted(results_dir.rglob("*.xml"))
    if not files:
        raise ValueError(f"no XML test reports found below {results_dir}")

    summary = {
        "files": len(files),
        "tests": 0,
        "failures": 0,
        "errors": 0,
        "skipped": 0,
    }
    for path in files:
        root = ElementTree.parse(path).getroot()
        for suite in ([root] if root.tag == "testsuite" else root.findall(".//testsuite")):
            summary["tests"] += int(suite.attrib.get("tests", 0))
            summary["failures"] += int(suite.attrib.get("failures", 0))
            summary["errors"] += int(suite.attrib.get("errors", 0))
            summary["skipped"] += int(suite.attrib.get("skipped", 0))
    return summary


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("results_dir", type=Path)
    parser.add_argument("--json", action="store_true", help="write JSON instead of text")
    args = parser.parse_args()

    try:
        summary = summarize(args.results_dir)
    except (OSError, ElementTree.ParseError, ValueError) as error:
        print(f"error: {error}", file=sys.stderr)
        return 2

    if args.json:
        print(json.dumps(summary, indent=2, sort_keys=True))
    else:
        print(
            f"{summary['tests']} tests: {summary['failures']} failures, "
            f"{summary['errors']} errors, {summary['skipped']} skipped"
        )
    return 1 if summary["failures"] or summary["errors"] else 0


if __name__ == "__main__":
    raise SystemExit(main())
