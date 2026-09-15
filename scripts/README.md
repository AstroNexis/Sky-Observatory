# Developer scripts

These scripts use only the Python standard library and assume they are run from
the repository root.

## Benchmark reports

Capture the benchmark module's Android log output, then parse it into a stable
JSON result:

```bash
adb logcat -d -s NativeBenchmark:I EngineBenchmark:I RendererBenchmark:I \
  > benchmark.log
python3 scripts/benchmark_report.py parse benchmark.log \
  --output benchmark.json
```

Compare two parsed runs. The command exits with status 1 when any benchmark is
more than 10% slower or a position calculation did not succeed:

```bash
python3 scripts/benchmark_report.py compare baseline.json current.json \
  --threshold 0.10 --output benchmark-comparison.md
```

## Test reports

Summarize the JUnit XML reports emitted by Gradle:

```bash
python3 scripts/test_report.py api/build/test-results --json
```

Pass the narrowest report directory available, such as
`api/build/test-results`, when inspecting one module. The command exits with
status 1 if a report contains failures or errors.

## Build artifacts

Validate that the non-empty APKs or AARs expected by CI exist:

```bash
python3 scripts/validate_artifacts.py observatory
python3 scripts/validate_artifacts.py sdk --json
```

The supported artifact groups are `observatory`, `sample-test`, `benchmark`,
and `sdk`.
