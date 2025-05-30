# Change Log

## [0.7] — 2025-03-12

#### 🚀 New Feature

- sqlite-android-wasm-emscripten-icu-aot-349 and sqlite-wasm-emscripten-aot-349 published to Maven Central

#### 💥 Breaking Change

- Package changed to at.released.wasm-sqlite-driver

## [0.6] — 2025-03-11

#### 💥 Breaking Change

- Library for publishing binaries as resources moved to https://github.com/illarionov/cassettes-kmp

#### 💅 Polish

- Emscripten build plugin moved to https://github.com/illarionov/emscripten-gradle-plugin

## [0.5.1] — 2025-03-04

#### 🐛 Bug Fix

- Rollback Kotlin to 2.1.10 due to  KT-75393

## [0.5] — 2025-03-04

#### 🚀 New Feature

- SQLite 3.49.1 builds with ICU 76.1 and Emscripten 4.0.4
- New modules sqlite-wasm-emscripten-aot-349 and sqlite-android-wasm-emscripten-icu-aot-349 with SQLite precompiled to .class using [wasm2class-gradle-plugin](https://github.com/illarionov/wasm2class-gradle-plugin) to use in Android and Kotlin/Java JVM projects

#### 💥 Breaking Change

- SQLite 3.48 build configurations have been discontinued

## [0.4] — 2025-01-25

#### 🚀 New Feature

- SQLite 3.48.0 builds with ICU 76.1 and Emscripten 4.0.1

#### 💥 Breaking Change

- SQLite 3.46 build configurations have been discontinued

#### 🤖 Dependencies

- Emscripten 4.0.1
- SQLite 3.48.0
- ICU 76.1
- Android Gradle Plugin 8.3.7
- Kotlin 2.1.0
- Kotlinx-io 0.6.0
- Other dependencies used in the build pipeline have also been updated.

## [0.3] — 2024-08-14

### Changed

- Update SQLite to 3.46.1
- Add -SQLITE_OMIT_UTF16 to non-Android builds

## [0.2] — 2024-08-11

### Added

- Linux Arm64 target
- JS, iOS, macOS targets to multithreaded builds

## [0.1] — 2024-08-10

Initial version

### Added

- sqlite-wasm-emscripten-346 and sqlite-wasm-emscripten-mt-pthread-346 builds
- binary archives with assemblies in releases

### Changed

- Binaries are now published in Android Assets for the Android target 
- For native platforms, binaries are now published in a common module
- Various fixes and Version updates

## [0.1-alpha10] — 2024-07-20

### Changed

- okio replaced with kotlinx-io

## [0.1-alpha09] — 2024-07-19

### Added

- Ios target

## [0.1-alpha08] — 2024-07-17

### Added

- MacOS target

### Changed

- `wasm-binary-reader` updated for MacOS target

## [0.1-alpha07] — 2024-07-05

### Added

- `wasm-binary-reader` library to simplify reading WASM binaries from application resources in a multi-platform project

### Changed

- Removed `requireSharedMemory` field from `WasmSqliteConfiguration`

## [0.1-alpha06] — 2024-07-03

### Added

- Artifacts published for the Kotlin LinuxX64 target as Multiplatform Resources

### Removed

- Removed build with debug symbols from published artifacts 

## [0.1-alpha05] — 2024-06-18

### Changed

- Add `malloc`, `free`, `pthread*` functions to the exported functions of the multithreaded build
- Bump ICU version to 75.1

## [0.1-alpha04] — 2024-06-02

### Changed

- SQlite binaries moved from https://github.com/illarionov/wasm-sqlite-open-helper to this repository
