# Contributing to Sky Vault

First off, thank you for considering contributing to Sky Vault! It's a personal, minimalistic
astronomy project, and community contributions help keep it alive. However...

> We are not accepting *most* PRs at the moment.

**Why?** The project is still in early development — only the Sun and Moon are currently
observable, and the architecture is actively evolving. Adding more features now will just move
the goalposts! When the core is stable we'll gladly accept more help.

## Before You Start

**Please [open an issue](https://github.com/AstroNexis/sky-observatory/issues) (or file a feature
request) before embarking on any major changes or feature additions.** We may have a different
vision for the direction of the app and it would be a pity to do work that we can't accept and
would be wasted.

Bug fixes, dependency upgrades, and documentation improvements are generally welcome without
prior discussion.

## A Note on Response Times

It is likely we'll be slow to respond to issues and PR requests. Depending on what else is going
on it might be days, it might be months. I do apologize for that — life is busy.

Thanks for your contributions! They're definitely appreciated even if our slowness to respond
might make it seem otherwise.

## Types of Contributions

Despite the temporary moratorium on new features, we're always grateful for:

- **Bug fixes** — Simple, focused, few-line fixes are very easy for us to approve.
- **Dependency upgrades** — Keeping things up to date is always welcome.
- **Documentation** — Improvements to docs, README, comments, etc.
- **Feature additions** — Please open an issue first (see above).

**Pro-tip:** Small, focused PRs are easier for us to approve!
If your PR does too much it might get stalled because even if 90% of it is welcome there might
be 10% that we're not happy with. So keep them small if you can. Plus, we'll be able to review
them faster.

## Development Setup

### Prerequisites

- Android Studio (latest stable recommended)
- Android SDK (API level 26–35)
- NDK `27.2.12479018`
- Java 17 toolchain

Android Studio can set up most of this for you.

### Project Structure

The project is divided into 6 modules:

- **api/** — API interface definitions
- **benchmark/** — Performance testing and benchmarking
- **engine/** — Core processing logic and astronomical calculations
- **native/** — C++ components (SuperNOVAS integration)
- **sample-test/** — Sample code and integration tests
- **sky-observatory/** — Main Android app module (UI and application logic)

See the [root README](README.md) for a more detailed overview.

## Building

All commands below should be run from the project root.

### Debug APK

```bash
./gradlew :sky-observatory:assembleDebug
```

The APK can be found in `sky-observatory/build/outputs/apk/debug/`.

### SDK Modules

```bash
./gradlew :api:assembleDebug :engine:assembleDebug :native:assembleDebug
```

> **Note:** The native module requires NDK `27.2.12479018`. Install it via Android Studio's
> SDK Manager or with:
> ```bash
> $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "ndk;27.2.12479018"
> ```

## Running Tests

### Unit Tests

```bash
./gradlew test
```

To target specific SDK modules:

```bash
./gradlew :api:test :engine:test :native:test
```

### Instrumented Tests

Requires a connected device or emulator (API 29 recommended):

```bash
./gradlew connectedDebugAndroidTest
```

## Submitting Changes

1. Fork the repository and create a branch from `master`.
2. Make your changes, keeping commits focused and atomic.
3. Run the unit tests to make sure you didn't break anything.
4. If you have multiple commits, please squash them into one.
5. Open a Pull Request with a clear description of what you changed and why.

## Coding Style

We follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
for Java, and standard Kotlin conventions for Kotlin files:

- 100 character line wrap
- Do **not** prefix member variables with `m`
- Java 17 / Kotlin 2.0 features are available