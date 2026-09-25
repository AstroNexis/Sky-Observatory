# Sky Vault

Sky Vault is an open-source astronomy project that blends a modular SDK, native astrometry, and an Android sky viewer into one clean workflow. It is built for tracking celestial objects in real time, using high-precision calculations from SuperNOVAS and a renderer designed for observation-focused use.

This project is more than a simple Moon-and-Sun app. The current catalog includes solar-system bodies across the main planetary range, with the architecture in place to grow beyond that in future phases.

> Note: the project is designed around a clean API boundary. App code should use the public SDK in the api module instead of reaching directly into engine internals.

## Why this project exists

Sky Vault aims to combine a lightweight user experience with accurate astronomy data. The experience is intentionally minimal, but the calculation layer is more capable than the early project notes suggest.

The design is inspired by astronomy apps that help people read the sky in context, while keeping the project open and self-contained for experimentation and learning.

## Main capabilities

- Real-time position and visibility calculations
- Solar-system object tracking
- Observer-aware sky evaluation
- Native C++ integration through SuperNOVAS
- Modular Android app structure
- Benchmark and sample-test support for validation and demos

## Supported objects

The current catalog is not limited to the Moon and Sun. It includes these bodies:

- Sun
- Moon
- Mercury
- Venus
- Mars
- Jupiter
- Saturn
- Uranus
- Neptune

> Note: the current implementation focuses on solar-system bodies. Star and deep-sky catalog support is reserved for a future phase rather than a current feature.

## Project structure

The repository is organized into a few clear modules:

- [api](api) - public SDK contracts, value objects, and shared interfaces
- [engine](engine) - astronomy logic, validation, coordinate conversion, and position calculations
- [native](native) - JNI and C++ bridge to the SuperNOVAS calculation layer
- [sky-observatory](sky-observatory) - Android app with rendering and observation UI
- [benchmark](benchmark) - performance checks and comparison runs
- [sample-test](sample-test) - demo and integration-style testing

## SDK and engine startup

The project is structured around a small but important startup flow:

1. Register the engine provider.
2. Initialize AstroSdk.
3. Fetch the AstroEngine through the SDK surface.
4. Use the engine for calculations without depending on internal modules.

This keeps the app and any future integrations aligned with the public contract instead of the internals.

## SuperNOVAS

Sky Vault uses SuperNOVAS as its core astrometry engine. SuperNOVAS provides the precision needed for celestial calculations, and the Java layer wraps that behavior behind the project SDK.

## Building and testing

The repository uses Gradle and the Java toolchain described by the project configuration. Common commands include:

```bash
./gradlew :sky-observatory:assembleDebug
./gradlew :api:assembleDebug :engine:assembleDebug :native:assembleDebug
./gradlew test
./gradlew :api:test :engine:test :native:test
```

For a targeted unit-test run, use the module-specific task with a --tests filter.

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request or proposing changes.

## License

This project is licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for the full text.

## Project note

This repo is a practical astronomy toolkit with a clean SDK boundary and a strong emphasis on modularity. It is a good fit for experimentation, education, and building a focused sky-observation experience without locking the core logic into the UI layer.
