# Sky Vault

[![Codecov](https://codecov.io/gh/AstroNexis/sky-observatory/branch/master/graph/badge.svg)](https://codecov.io/gh/AstroNexis/sky-observatory)
[![Build](https://github.com/AstroNexis/sky-observatory/actions/workflows/observatory.yml/badge.svg)](https://github.com/AstroNexis/sky-observatory/actions/workflows/observatory.yml)
[![Tests](https://github.com/AstroNexis/sky-observatory/actions/workflows/test.yml/badge.svg)](https://github.com/AstroNexis/sky-observatory/actions/workflows/test.yml)
[![Benchmark](https://github.com/AstroNexis/sky-observatory/actions/workflows/benchmark.yml/badge.svg)](https://github.com/AstroNexis/sky-observatory/actions/workflows/benchmark.yml)

An open-source astronomy SDK and Android sky viewer, built on top of
[SuperNOVAS](https://github.com/Sigmyne/SuperNOVAS) for the calculation
layer.

The catalog covers the main solar-system bodies right now. If you're
reading this later, that list has probably grown.

> App code should go through the `api` module, not into engine internals.
> That boundary is intentional.

## What's inside

| Module | What it does |
|---|---|
| `api` | Public SDK contracts - the surface callers should use |
| `engine` | Positioning, validation, coordinate math |
| `native` | JNI bridge to SuperNOVAS (C++) |
| `sky-observatory` | Android app - rendering, camera, touch input |
| `location` | Fused location provider integration |
| `benchmark` | Performance tests |
| `sample-test` | Demo / integration smoke-test app |

## Quick start

```java
EngineInitializer.register();
AstroSdk.initialize();
AstroEngine engine = AstroSdk.getEngine();
PositionResult result = engine.calculatePosition(target, observer, time);
```

That's it. Everything else - provider setup, native loading, object
graph wiring - happens behind the SDK boundary.

## Build & test

```bash
./gradlew :sky-observatory:assembleDebug
./gradlew test
./gradlew :api:test :engine:test :native:test
```

## Project structure

The repo is a standard Android multi-module Gradle project. Each module
has its own `build.gradle.kts`, unit tests live under `src/test/java`,
and the native code lives in `native/src/main/cpp/`.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Short version: keep PRs small,
keep app code on the `api` surface, and don't duplicate math that
already lives in the native layer.

## License

Apache 2.0 - see [LICENSE](LICENSE).
