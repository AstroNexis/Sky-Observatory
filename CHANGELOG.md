# Changelog

All notable changes to the [Sky Vault](https://github.com/AstroNexis/sky-observatory) project will be
documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [Unreleased]

Upcoming release, focusing on community infrastructure and project configuration.

### Added

 - #31: Community files -- code of conduct, security policy, support guide, and GitHub issue/PR
   templates.

 - Project configuration files: `.editorconfig`, `.gitattributes`, and `.mailmap` for cross-platform
   consistency.

 - `sky-observatory/proguard-rules.pro` for release-build minification.

### Changed

 - Expanded `.gitignore` to cover compiled artifacts (`*.jar`, `*.aar`), editor temp files, and
   profiler output.

 - Populated the previously empty `CHANGELOG.md` and `benchmark/consumer-rules.pro`.


## [0.1.1-alpha] - 2026-07-04

Bug-fix and touch-system overhaul release.

### Fixed

 - #5: Crash handler no longer blocks the main process; moved to a separate process with a loop
   guard and increased sleep to 4 seconds.

 - #4: Horizon ring now renders correctly by disabling depth test in the draw pass.

 - #3: Touch system resolved Kotlin smart cast and compilation errors after migrating to the new
   modular architecture.

 - #1: Removed EMA lag and dropped move events from pan/pinch; exponential smoothing applied
   instead.

 - `jacoco` coverage task configuration and test compilation errors from CI.

 - APK signing: added `--ks-type JKS` flag to all workflows to prevent PKCS12 loading errors;
   enabled signature schemes v2 and v3 across all workflows.

 - Codecov integration: fixed permissions and upload configuration.

### Added

 - #6: Modular touch system redesigned in Kotlin -- `FingerTracker`, `GestureRecognizer`,
   `TouchController`, `TouchStateManager` with a 3-state machine and pinch-only zoom.

 - FOV-scaled drag sensitivity: `getFovDeg()` exposed on the camera, wired to `TouchController`
   for uniform azimuth/altitude feel.

 - CI `workflow_dispatch` trigger on all workflows; concurrency groups and cancel-in-progress
   logic.

 - JaCoCo and Codecov integration for unit test coverage on `api`, `engine`, and `native` modules.

 - `CONTRIBUTING.md` with build and test instructions.

### Changed

 - Touch system rewritten from Java to Kotlin, moved into its own `touch/` module.

 - Touch drag model: inverted azimuth/altitude axes for a "grab the sky" feel; uniform
   sensitivity, FOV-scaled, no pinch rotation.

 - Workflows updated to use `ubuntu-latest`, Gradle 9.6.1, and centralized NDK version
   (`27.2.12479018`).

 - `codecov.yml` now ignores benchmark, sample-test, and sky-observatory modules.

### Removed

 - Old `TouchController.java` in the camera package after Kotlin migration.


## [0.1.0-alpha] - 2026-07-02

Initial pre-release of Sky Vault, providing the foundation for observing celestial objects.

### Added

 - Core API module (`:api`) with interface definitions for astronomy calculations, celestial
   objects, observers, coordinates, and ephemeris results.

 - Engine module (`:engine`) implementing the default astronomy engine, ephemeris calculator,
   coordinate transformations, and viewport projections.

 - Native bridge module (`:native`) with C++ JNI bindings to SuperNOVAS for high-precision
   astrometry.

 - Main application module (`:sky-observatory`) with OpenGL rendering, camera controls, and
   touch input.

 - Sample-test module (`:sample-test`) demonstrating SDK consumption.

 - Benchmark module (`:benchmark`) for performance testing.

 - CI workflows for unit tests, SDK builds, instrumented tests (API 29 emulator), observatory
   APK builds, and benchmarks.

 - Dependabot configuration for Gradle and GitHub Actions dependency updates.

 - `LICENSE` (Apache 2.0), `README.md`, and `codecov.yml`.

 - Initial `supernovas` git submodule for astronomical computation.