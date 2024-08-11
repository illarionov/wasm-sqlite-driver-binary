# Change Log

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
