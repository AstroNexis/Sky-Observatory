# Changelog

All notable changes to the [Sky Observatory](https://github.com/AstroNexis/sky-observatory) project will be
documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [Unreleased]

Upcoming release, expected arounid 15 November 2026.

### Fixed

- #72: Fixed inconsistent use of `isAboveHorizon` so the renderer treats the horizon boundary like the rest of the engine.

- #70: Fixed incorrect activity class paths in `AndroidManifest.xml`.

- #68: Preserved yaw at zenith in the `SkyCamera` view matrix.

- #75: Fixed apparent angular diameter calculation to use `asin`.

- #76: Fixed angular diameter calculation using `atan` instead of `asin` in `EphemerisCalculator`.

- #78: Fixed a bug causing planets to disappear at night.

- #81: Fixed incorrect package paths in consumer rules.

- #86: Fixed snapshot creation to reject null or empty targets.

- #92: Fixed position results to reject non-finite distances.

- #95: Fixed rise/set calculations missing horizon crossings at the start of the search day.

- #96: Fixed ignore horizon tangencies.

- #97: Fixed null and non-finite horizontal coordinates returned by the position provider.

- #99: Fixed asymmetric label rotation snapping when tilting the phone left or right.

- #102: Fixed label orientation near the zenith and nadir to use the default screen orientation.

- #104: Fixed camera label orientation instability during device shake.

- #106: Extended provider-result validation to ephemeris calculations, rejecting null or
  non-finite horizontal coordinates.

- #108: Made the native calculation boundary reject null JNI results with `NativeException`.

### Added

- #71: Added missing unit tests across the API and engine modules.

- #79: Added rotation of celestial labels with screen orientation.

### Removed

 - #59: Removed duplicate `LocationRepository` from the engine module. The location module already provides the same functionality.

### Changed

- #69: Replaced custom crash handler with the Crashx library.

- #100: Replaced the dark sky gradient with a uniform blue background.

- #104: Refactored label orientation handling to publish smoothed forward/up vectors as a single coherent snapshot and reset gravity-reference sign on abrupt direction changes. 


## [0.1.2-alpha] - 2026-09-24

Upcoming release, focusing on renderer reliability, astronomy visuals, and project maintenance.

### Fixed

 - #51: Fixed native builds to report a clear SuperNOVAS submodule message when the
   dependency has not been initialized.

 - #45: Fixed renderer updates not restarting correctly after activity resume, which could cause
   duplicate workers, stale callbacks, or queued snapshots from paused lifecycle generations.

 - #46: Fixed camera projection instability at zenith and nadir while preserving yaw and roll.

 - #47: Fixed renderer to skip `BELOW_HORIZON` objects before allocating or drawing GPU resources
   while retaining them in snapshots.
   
 - #50: Fixed observer validation to reject non-finite latitude, longitude, and altitude values.

### Added

 - #31: Added community files -- code of conduct, security policy, support guide, and GitHub issue/PR
   templates.

 - #30: Added moon-phase rendering using sun-direction illumination.

 - #49: Added build and benchmark reporting scripts.

### Changed

 - #39: Shared location repository functionality across the application.

 - #35: Consolidated celestial-object metadata and location services behind the shared SDK and
   engine modules, while simplifying renderer, scene, shader, and touch-system code.

 - #32, #34, #36, and #38: Updated Material, Android, and Gradle dependencies and the Gradle
   wrapper.

 - #37: Updated the GitHub Actions Java setup and workflow configuration.

## [0.1.1-alpha] - 2026-07-04

Bug-fix and touch-system overhaul release.

### Fixed

 - #5: Fixed crash handler blocking the main process by moving it to a separate process with a loop
   guard and increasing the sleep to 4 seconds.

 - #4: Fixed horizon ring rendering by disabling depth testing in the draw pass.

 - #3: Fixed Kotlin smart cast and compilation errors after migrating the touch system to the new
   modular architecture.

 - #1: Fixed pan/pinch handling by removing EMA lag and dropped move events and applying
   exponential smoothing instead.

### Added

 - #6: Added a modular touch system in Kotlin -- `FingerTracker`, `GestureRecognizer`,
   `TouchController`, `TouchStateManager` with a 3-state machine and pinch-only zoom.

 - Added FOV-scaled drag sensitivity: `getFovDeg()` exposed on the camera, wired to `TouchController`
   for uniform azimuth/altitude feel.

 - Added CI `workflow_dispatch` trigger on all workflows; concurrency groups and cancel-in-progress
   logic.

 - Added JaCoCo and Codecov integration for unit test coverage on `api`, `engine`, and `native` modules.

 - Added `CONTRIBUTING.md` with build and test instructions.

### Changed

 - Rewrote touch system from Java to Kotlin and moved it into its own `touch/` module.

 - Changed touch drag model to use inverted azimuth/altitude axes for a "grab the sky" feel, with
   uniform FOV-scaled sensitivity and no pinch rotation.

 - Updated workflows to use `ubuntu-latest`, Gradle 9.6.1, and centralized NDK version
   (`27.2.12479018`).

 - Updated `codecov.yml` to ignore benchmark, sample-test, and sky-observatory modules.

### Removed

 - Removed old `TouchController.java` from the camera package after Kotlin migration.


## [0.1.0-alpha] - 2026-07-02

Initial pre-release of Sky Vault, providing the foundation for observing celestial objects.

### Added

 - Added core API module (`:api`) with interface definitions for astronomy calculations, celestial
   objects, observers, coordinates, and ephemeris results.

 - Added engine module (`:engine`) implementing the default astronomy engine, ephemeris calculator,
   coordinate transformations, and viewport projections.

 - Added native bridge module (`:native`) with C++ JNI bindings to SuperNOVAS for high-precision
   astrometry.

 - Added main application module (`:sky-observatory`) with OpenGL rendering, camera controls, and
   touch input.

 - Added sample-test module (`:sample-test`) demonstrating SDK consumption.

 - Added benchmark module (`:benchmark`) for performance testing.

 - Added CI workflows for unit tests, SDK builds, instrumented tests (API 29 emulator), observatory
   APK builds, and benchmarks.

 - Added Dependabot configuration for Gradle and GitHub Actions dependency updates.

 - Added `LICENSE` (Apache 2.0), `README.md`, and `codecov.yml`.

 - Added initial `supernovas` git submodule for astronomical computation.
