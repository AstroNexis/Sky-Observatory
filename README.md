<p align="center">
  <img src="/sky-observatory/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="Sky Vault" width="200" />
</p>

<div align="center">

[![Codecov](https://codecov.io/gh/AstroNexis/sky-observatory/branch/master/graph/badge.svg)](https://codecov.io/gh/AstroNexis/sky-observatory)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/AstroNexis/sky-observatory/blob/main/LICENSE)
[![Build](https://github.com/AstroNexis/sky-observatory/actions/workflows/observatory.yml/badge.svg)](https://github.com/AstroNexis/sky-observatory/actions/workflows/observatory.yml)
[![Tests](https://github.com/AstroNexis/sky-observatory/actions/workflows/test.yml/badge.svg)](https://github.com/AstroNexis/sky-observatory/actions/workflows/test.yml)
[![Benchmark](https://github.com/AstroNexis/sky-observatory/actions/workflows/benchmark.yml/badge.svg)](https://github.com/AstroNexis/sky-observatory/actions/workflows/benchmark.yml)

</div>

# Sky Vault

<br clear="all">

[Sky Vault](https://github.com/AstroNexis/sky-observatory) is an open-source app for observing celestial objects in the sky. The project includes a user interface and uses [SuperNOVAS](https://github.com/Sigmyne/SuperNOVAS/) as its core astronomical computation library.

Sky Vault is inspired by existing applications such as **Sky Map** and **Star Walk**. The purpose of this project is to create a personal, minimalistic application for sky observation.

<br clear="all">

## Downloads

- **Latest Alpha Build**: Download from [Actions](https://github.com/AstroNexis/sky-observatory/actions/)
- **Latest Stable Build**: Download from [Releases](https://github.com/AstroNexis/sky-observatory/releases)

[<img src="https://raw.githubusercontent.com/Kunzisoft/Github-badge/main/get-it-on-github.png" alt="Get it on GitHub" height="80">](https://github.com/AstroNexis/sky-observatory/releases/latest)

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Progress](#progress)
- [Project Structure](#project-structure)
- [SuperNOVAS](#supernovas)
- [Contributing](#contributing)
- [License](#license)

-----------------------------------------------------------------------------

## Introduction

Sky Vault is an open-source astronomy application designed for observing celestial objects in the night sky. The project is inspired by existing applications such as Sky Map and Star Walk, with the goal of creating a personal, minimalistic tool for sky observation.

Currently, the application is available for download only through GitHub via the Actions or Releases pages. The project is currently licensed under **Apache-2.0**, though this may be changed to **MIT** in the future:-).

## Features

- Real-time identification of celestial objects
- Offline operation
- Support for basic astronomical objects
- Minimalistic user interface

## Progress

Currently, the application can observe two celestial objects: the Moon and the Sun. In the near future, additional features will be developed, including:

- Observation of constellations
- Live effects
- And more...

## Project Structure

The project is divided into 6 main modules:

1. [/api](/api) -- Module containing API interface definitions for the application.

2. [/benchmark](/benchmark) -- Module for performance testing and benchmarking of project components.

3. [/engine](/engine) -- The core of the application, containing main processing logic and calculations.

4. [/native](/native) -- Module containing native components written in C++ (for integration with the SuperNOVAS library).

5. [/sample-test](/sample-test) -- Module containing sample code or tests for the application.

6. [/sky-observatory](/sky-observatory) -- Main Android/Java application module, containing source code for the user interface and application logic.

## SuperNOVAS

Sky Vault uses **SuperNOVAS** as its core astronomical computation library. SuperNOVAS is a C/C++ open-source library that provides high-precision astrometry calculations.

## Contributing

Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to contribute to this project.

## License

This project is licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for details.
