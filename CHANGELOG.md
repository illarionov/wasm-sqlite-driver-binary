# Change Log

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

- Artifacts published for the Kolin LinuxX64 target as Multiplatform Resources

### Removed

- Removed build with debug symbols from published artifacts 

## [0.1-alpha05] — 2024-06-18

### Changed

- Add `malloc`, `free`, `pthread*` functions to the exported functions of the multithreaded build
- Bump ICU version to 75.1

## [0.1-alpha04] — 2024-06-02

### Changed

- SQlite binaries moved from https://github.com/illarionov/wasm-sqlite-open-helper to this repository
