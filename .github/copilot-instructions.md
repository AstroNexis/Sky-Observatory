# Copilot instructions for Sky Vault

## Repository overview

This repository is a multi-module Android astronomy project built around a shared SDK + renderer layout:

- `api/`: public Java API contracts, value objects, and SDK-facing types (`AstroSdk`, `AstroEngine`, coordinate models, results, exceptions).
- `engine/`: calculation layer for validation, coordinate conversion, position/ephemeris logic, visibility checks, and viewport projection. Engine internals are intentionally hidden behind `AstroSdk` and `EngineInitializer`.
- `native/`: JNI/C++ bridge to the SuperNOVAS-based calculation layer. The Java `NativeAstroCalculator` is the entry point for native runs.
- `sky-observatory/`: Android app module that renders sky data, manages camera/sensor state, and handles touch/gesture logic.
- `benchmark/`: performance benchmarks for engine/render/native paths.
- `sample-test/`: demo/test app for integration usage.

The project is organized around a clean SDK boundary: app code should use the `api` interfaces and `AstroSdk`, not direct engine internals.

## Build, test, and verification commands

The project uses Gradle with Kotlin DSL. CI is configured to run Java 17 and NDK `27.2.12479018`.

### Prerequisites

- Java 17
- Android SDK
- Android NDK `27.2.12479018`

If the NDK is missing locally:

```bash
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "ndk;27.2.12479018"
```

### Build

```bash
./gradlew :sky-observatory:assembleDebug
./gradlew :sky-observatory:assembleRelease
./gradlew :api:assembleDebug :engine:assembleDebug :native:assembleDebug
```

### Test

```bash
./gradlew test
./gradlew :api:test :engine:test :native:test
./gradlew connectedDebugAndroidTest
```

### Single-test execution

Use the Gradle `--tests` filter with the module task name:

```bash
./gradlew :api:testDebugUnitTest --tests "com.skyobservatory.api.AstroSdkTest"
./gradlew :engine:testDebugUnitTest --tests "com.skyobservatory.engine.PositionCalculatorTest"
./gradlew :native:testDebugUnitTest --tests "com.skyobservatory.native_bridge.NativeAstroCalculatorContractTest"
```

The repo does not define a separate lint task in Gradle; the source-of-truth verification commands are the module build and unit-test tasks above.

## High-level architecture

The key system boundary is the SDK startup path:

1. `EngineInitializer.register()` bootstraps the engine and registers it as the active `AstronomyProvider`.
2. `AstroSdk.initialize()` creates the `AstroEngine` from that provider.
3. App code then calls `AstroSdk.getEngine()` and works with `AstroEngine`, `PositionResult`, `SkySnapshot`, and related API types.
4. The engine delegates to validation, coordinate conversion, and native position calculations before producing renderable results.

This is the most important project pattern to preserve when making changes: keep app code on the `api` surface, and avoid reaching into `engine` package-private internals unless you are intentionally working inside the engine module.

The rendering layer in `sky-observatory` is a separate Android concern from the astronomical calculations. It consumes `api` outputs and uses camera, scene, and shader code to display celestial objects; it is not the place to re-implement core astronomy logic.

## Key conventions and repo-specific patterns

- `master` is the main branch used by CI and release automation; most workflows trigger on `master` and `release/**`.
- This repo is intentionally modular and early-stage; keep PRs small and focused, matching the guidance in `CONTRIBUTING.md`.
- Public SDK-facing types belong in `com.skyobservatory.api`; engine internals are package-private unless they are intentionally exposed through `EngineInitializer` or `AstroSdk`.
- Java is the primary language for SDK and engine code; Kotlin is used for a small subset of UI/touch logic in the app module.
- Gradle modules are referenced as `:module`, not as custom shell scripts or ad hoc build commands.
- The `api` and `engine` modules have unit tests under `src/test/java`, and the app module is Android-focused rather than a full web app.
- The project uses Android app + library conventions, not a monorepo backend service structure.

## Change guidance

When editing code in this repository, favor the existing module boundaries:

- Put new public SDK contracts in `api/`.
- Keep algorithmic or validation logic inside `engine/`.
- Keep native bridging inside `native/` and do not duplicate the math in Java.
- Keep Android rendering and input behavior inside `sky-observatory/`.

This keeps the project aligned with the existing design and avoids bypassing the SDK entry points.
