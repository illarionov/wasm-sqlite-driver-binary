# Wasm SQLite binaries

This repository contains build scripts for compiling SQLite WebAssembly binaries using Gradle and Emscripten, 
and publishing them to a Maven-compatible repository for use in Kotlin Multiplatform projects.

You can find the latest snapshot of the Maven repository, along with archives containing the SQLite Wasm binaries,
in the [Releases section][Releases].

The code for building SQLite has been moved to this repository after multiple unsuccessful attempts to eliminate
full recompilation of SQLite each time there were minor changes to the Gradle build scripts in the main project. 
These binaries are primarily intended for use in the [Wasm-sqlite-open-helper] project.

## SQLite configurations

This project provides four different SQLite builds, differing in compilation settings:

* [sqlite-android-wasm-emscripten-icu-346](#Sqlite-android-wasm-emscripten-icu-346)
* [sqlite-android-wasm-emscripten-icu-mt-pthread-346](#Sqlite-android-wasm-emscripten-icu-mt-pthread-346)


* [sqlite-wasm-emscripten-346](#Sqlite-wasm-emscripten-346)
* [sqlite-wasm-emscripten-mt-pthread-346](#Sqlite-wasm-emscripten-mt-pthread-346)

### Sqlite-android-wasm-emscripten-icu-346

* Gradle dependency: `implementation("ru.pixnews.wasm-sqlite-open-helper:sqlite-android-wasm-emscripten-icu-346:0.2")`
* Debug binaries: [sqlite-android-wasm-emscripten-icu-346-3460000-debug-0.2.zip](https://github.com/illarionov/wasm-sqlite-driver-binary/releases/download/0.2/sqlite-android-wasm-emscripten-icu-346-3460000-debug-0.2.zip)

Single-threaded SQLite build with a configuration similar to that used in the Android system.

* Based on the AOSP SQLite configuration
* Includes Android-specific patches
* Supports Android-specific localized collators
* ICU is statically compiled
* No multithreading support

The ICU library is statically compiled, resulting in a SQLite binary size of about 30 megabytes.
This binary is loaded into RAM during execution, so the RAM requirements are quite high.

Compilation settings:
```
Emscripten flags:
-sALLOW_MEMORY_GROWTH
-sALLOW_TABLE_GROWTH
-sDYNAMIC_EXECUTION=0
-sENVIRONMENT=worker
-sERROR_ON_UNDEFINED_SYMBOLS=0
-sEXPORTED_RUNTIME_METHODS=wasmMemory
-sEXPORT_ES6
-sEXPORT_NAME=sqlite3InitModule
-sGLOBAL_BASE=4096
-sIMPORTED_MEMORY,
-sINITIAL_MEMORY=50331648
-sLLD_REPORT_UNDEFINED
-sMODULARIZE
-sNO_POLYFILL
-sSTACK_SIZE=512KB
-sSTANDALONE_WASM=0
-sSTRICT_JS=0
-sUSE_CLOSURE_COMPILER=0
-sUSE_ES6_IMPORT_META
-sWASM_BIGINT

SQLite flags:
-DBIONIC_IOCTL_NO_SIGNEDNESS_OVERLOAD
-DHAVE_MALLOC_H=1
-DHAVE_MALLOC_USABLE_SIZE
-DHAVE_USLEEP=1
-DNDEBUG=1
-DSQLITE_ALLOW_ROWID_IN_VIEW
-DSQLITE_DEFAULT_AUTOVACUUM=1
-DSQLITE_DEFAULT_FILE_FORMAT=4
-DSQLITE_DEFAULT_FILE_PERMISSIONS=0600
-DSQLITE_DEFAULT_JOURNAL_SIZE_LIMIT=1048576
-DSQLITE_DEFAULT_LEGACY_ALTER_TABLE
-DSQLITE_DEFAULT_MEMSTATUS=0
-DSQLITE_DEFAULT_UNIX_VFS=\"unix-excl\"
-DSQLITE_ENABLE_BATCH_ATOMIC_WRITE
-DSQLITE_ENABLE_BYTECODE_VTAB
-DSQLITE_ENABLE_DBPAGE_VTAB
-DSQLITE_ENABLE_DBSTAT_VTAB
-DSQLITE_ENABLE_FTS3
-DSQLITE_ENABLE_FTS3_BACKWARDS
-DSQLITE_ENABLE_FTS3_PARENTHESIS
-DSQLITE_ENABLE_FTS4
-DSQLITE_ENABLE_FTS5
-DSQLITE_ENABLE_ICU
-DSQLITE_ENABLE_JSON1
-DSQLITE_ENABLE_MEMORY_MANAGEMENT=1
-DSQLITE_ENABLE_RBU
-DSQLITE_ENABLE_RTREE
-DSQLITE_ENABLE_STAT4
-DSQLITE_ENABLE_STMTVTAB
-DSQLITE_HAVE_ISNAN
-DSQLITE_MAX_MMAP_SIZE=0
-DSQLITE_MAX_WORKER_THREADS=0
-DSQLITE_OMIT_BUILTIN_TEST
-DSQLITE_OMIT_COMPILEOPTION_DIAGS
-DSQLITE_OMIT_DEPRECATED
-DSQLITE_OMIT_LOAD_EXTENSION
-DSQLITE_OMIT_SHARED_CACHE
-DSQLITE_POWERSAFE_OVERWRITE=1
-DSQLITE_SECURE_DELETE
-DSQLITE_TEMP_STORE=3
-DSQLITE_THREADSAFE=0
-DSQLITE_WASM_ENABLE_C_TESTS
-Dfdatasync=fdatasync
-Wno-unused-parameter
-ftrivial-auto-var-init=pattern

Code generation flags:
-g3 -fPIC --minify 0 --no-entry -O2 -flto -Wno-limited-postlink-optimizations -fdebug-compilation-dir=/build 

Additional source files:
sqlite3-wasm.c callbacks-wasm.c sqlite3_android.cpp PhoneNumberUtils.cpp OldPhoneNumberUtils.cpp

Additional libraries: ICU75.1

ICU build flags:
--with-data-packaging=static -lm -O3 DU_HAVE_MMAP=0 -DUCONFIG_NO_FILE_IO -DUCONFIG_NO_FORMATTING -DUCONFIG_NO_LEGACY_CONVERSION -DUCONFIG_NO_TRANSLITERATION
```

### Sqlite-android-wasm-emscripten-icu-mt-pthread-346

* Gradle dependency: `implementation("ru.pixnews.wasm-sqlite-open-helper:sqlite-android-wasm-emscripten-icu-mt-pthread-346:0.2")`
* Debug binaries: [sqlite-android-wasm-emscripten-icu-mt-pthread-346-3460000-debug-0.2.zip](https://github.com/illarionov/wasm-sqlite-driver-binary/releases/download/0.2/sqlite-android-wasm-emscripten-icu-mt-pthread-346-3460000-debug-0.2.zip)

The same configuration as [sqlite-android-wasm-emscripten-icu-346](#sqlite-android-wasm-emscripten-icu-346), but with multithreading enabled.

Build flag differences:
```
Emscripten flags:

Additional code generation flags: -pthread
Additional Emscripten flags: -sSHARED_MEMORY
Additional SQLite flags: -DSQLITE_THREADSAFE=2
```

### Sqlite-wasm-emscripten-346

* Gradle dependency: `implementation("ru.pixnews.wasm-sqlite-open-helper:sqlite-wasm-emscripten-346:0.2")`
* Debug binaries: [sqlite-wasm-emscripten-346-3460000-debug-0.2.zip](https://github.com/illarionov/wasm-sqlite-driver-binary/releases/download/0.2/sqlite-wasm-emscripten-346-3460000-debug-0.2.zip)

Single-threaded SQLite configuration without ICU and Android extensions.

Compilation settings:
```
Emscripten flags:
-sALLOW_MEMORY_GROWTH
-sALLOW_TABLE_GROWTH
-sDYNAMIC_EXECUTION=0
-sENVIRONMENT=worker
-sERROR_ON_UNDEFINED_SYMBOLS=0
-sEXPORTED_RUNTIME_METHODS=wasmMemory
-sEXPORT_ES6
-sEXPORT_NAME=sqlite3InitModule
-sGLOBAL_BASE=4096
-sIMPORTED_MEMORY
-sINITIAL_MEMORY=4194304
-sLLD_REPORT_UNDEFINED
-sMODULARIZE
-sNO_POLYFILL
-sSTACK_SIZE=512KB
-sSTANDALONE_WASM=0
-sSTRICT_JS=0
-sUSE_CLOSURE_COMPILER=0
-sUSE_ES6_IMPORT_META
-sWASM_BIGINT

SQLite flags:
-DBIONIC_IOCTL_NO_SIGNEDNESS_OVERLOAD
-DHAVE_MALLOC_H=1
-DHAVE_MALLOC_USABLE_SIZE
-DHAVE_USLEEP=1
-DNDEBUG=1
-DSQLITE_ALLOW_ROWID_IN_VIEW
-DSQLITE_DEFAULT_AUTOVACUUM=1
-DSQLITE_DEFAULT_FILE_FORMAT=4
-DSQLITE_DEFAULT_FILE_PERMISSIONS=0600
-DSQLITE_DEFAULT_JOURNAL_SIZE_LIMIT=1048576
-DSQLITE_DEFAULT_LEGACY_ALTER_TABLE
-DSQLITE_DEFAULT_MEMSTATUS=0
-DSQLITE_DEFAULT_UNIX_VFS=\"unix-excl\"
-DSQLITE_ENABLE_BATCH_ATOMIC_WRITE
-DSQLITE_ENABLE_BYTECODE_VTAB
-DSQLITE_ENABLE_DBPAGE_VTAB
-DSQLITE_ENABLE_DBSTAT_VTAB
-DSQLITE_ENABLE_FTS3
-DSQLITE_ENABLE_FTS3_BACKWARDS
-DSQLITE_ENABLE_FTS3_PARENTHESIS
-DSQLITE_ENABLE_FTS4
-DSQLITE_ENABLE_FTS5
-DSQLITE_ENABLE_JSON1
-DSQLITE_ENABLE_MEMORY_MANAGEMENT=1
-DSQLITE_ENABLE_RBU
-DSQLITE_ENABLE_RTREE
-DSQLITE_ENABLE_STAT4
-DSQLITE_ENABLE_STMTVTAB
-DSQLITE_HAVE_ISNAN
-DSQLITE_MAX_MMAP_SIZE=0
-DSQLITE_MAX_WORKER_THREADS=0
-DSQLITE_OMIT_BUILTIN_TEST
-DSQLITE_OMIT_COMPILEOPTION_DIAGS
-DSQLITE_OMIT_DEPRECATED
-DSQLITE_OMIT_LOAD_EXTENSION
-DSQLITE_OMIT_SHARED_CACHE
-DSQLITE_POWERSAFE_OVERWRITE=1
-DSQLITE_SECURE_DELETE
-DSQLITE_TEMP_STORE=3
-DSQLITE_THREADSAFE=0
-DSQLITE_WASM_ENABLE_C_TESTS
-Dfdatasync=fdatasync
-Wno-unused-parameter
-ftrivial-auto-var-init=pattern

Code generation flags:
-g3 -fPIC --minify 0 --no-entry -O2 -flto -Wno-limited-postlink-optimizations -fdebug-compilation-dir=/build 

Additional source files:
sqlite3-wasm.c callbacks-wasm.c
```

### Sqlite-wasm-emscripten-mt-pthread-346

* Gradle dependency: `implementation("ru.pixnews.wasm-sqlite-open-helper:sqlite-wasm-emscripten-mt-pthread-346:0.2")`
* Debug binaries: [sqlite-wasm-emscripten-mt-pthread-346-3460000-debug-0.2.zip](https://github.com/illarionov/wasm-sqlite-driver-binary/releases/download/0.2/sqlite-wasm-emscripten-mt-pthread-346-3460000-debug-0.2.zip)

The same configuration as [sqlite-wasm-emscripten-346](#sqlite-wasm-emscripten-346), but with multithreading enabled.

Build flag differences:
```
Emscripten flags:

Additional code generation flags: -pthread
Additional Emscripten flags: -sSHARED_MEMORY
Additional SQLite flags: -DSQLITE_THREADSAFE=2
```

## Publication format

SQLite WebAssembly binary files are published to a Maven-compatible repository with Gradle Module Metadata for use
in Kotlin Multiplatform projects.

For Android target, `.wasm` files are packed as Android assets within AAR archives, so no additional Gradle plugins
are needed for their use.

For JVM target, `.wasm` files are packed as Java Resources, which can be handled with standard tools.

For native Kotlin Multiplatform targets (`iosArm64`, `iosSimulatorArm64`, `linuxX64`, `macosX64`, etc.), `.wasm`
binaries are packaged and published in a format compatible with Kotlin Multiplatform Resources (though this format
is not yet publicly announced).
To use it in your project, you can try the [Compose Multiplatform Resources] plugin, or check the [resources.gradle.kts] of the main project.

To simplify loading binaries across different platforms, a helper library "wasm-binary-reader" is available.

Gradle dependency:
```
implementation("ru.pixnews.wasm-sqlite-open-helper:wasm-binary-reader:0.2")
```

Sample usage:
```kotlin
import ru.pixnews.wasm.sqlite.binary.base.WasmSqliteConfiguration
import ru.pixnews.wasm.sqlite.binary.reader.WasmSourceReader
import ru.pixnews.wasm.sqlite.binary.reader.readOrThrow

// Implementation of thre Reader. Use AndroidAssetsWasmSourceReader(appContext.assets) on Android
val reader = WasmSourceReader 

// Build configuration from `sqlite-wasm-emscripten-346` module
val sqlite = SqliteWasmEmscripten346

val wasmBinary: ByteArray = reader.readBytesOrThrow(sqlite.sqliteUrl)
```

[Compose Multiplatform Resources]: https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-images-resources.html
[resources.gradle.kts]: https://github.com/illarionov/wasm-sqlite-open-helper/blob/main/build-logic/project/multiplatform/src/main/kotlin/resources.gradle.kts

## Installation

Release and snapshot versions are published to a temporary repository, since it is highly experimental.
File a bug report if you think it could be useful on Maven Central.

Add the following to your project's settings.gradle:

```kotlin
pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pixnews.ru")
            mavenContent {
                includeGroup("ru.pixnews.wasm-sqlite-open-helper")
            }
        }
    }
}
```

You can also download a snapshot of the repository from the [Releases section][Releases].

Add the required dependencies:

```kotlin
dependencies {
    implementation("ru.pixnews.wasm-sqlite-open-helper:wasm-binary-reader:0.2")
    implementation("ru.pixnews.wasm-sqlite-open-helper:sqlite-android-wasm-emscripten-icu-mt-pthread-346:0.2")
}
```

## Development notes

To build the project, you will need to install the following software in addition to the JVM:

 - GNU Make 
   - on macOS: `brew install make`
 - [WABT](https://github.com/WebAssembly/wabt) (The WebAssembly Binary Toolkit)
   - on macOS: `brew install wabt`
   - on Ubuntu Linux: `apt install wabt`
 - llvm-dwarfdump
   - on macOS: `brew install llvm`
 - [Emscripten SDK](https://emscripten.org/)
   - Check [this link](https://emscripten.org/docs/getting_started/downloads.html#installation-instructions-using-the-emsdk-recommended)
   for instructions on installing the Emscripten SDK.

`EMSDK` environment variable must point to the root of the installed SDK.
Version of the SDK used in the project must be activated (check the `emscripten` version
in [gradle/libs.versions.toml](gradle/libs.versions.toml)).

Alternatively, you can specify the Emscripten SDK root by setting the `emsdkRoot` project property.
You can do this for example in `~/.gradle/gradle.properties`:

```properties
emsdkRoot=/opt/emsdk
```

Install and activate the SDK version used in the project (not the latest one):

```shell
./emsdk install 3.1.64
./emsdk activate 3.1.64
source ./emsdk_env.sh
```

The first build may take quite a long time, since the ICU and SQLite libraries are build from source code.

## Contributing

Any type of contributions are welcome. Please see the [contribution guide](CONTRIBUTING.md).

## License

These services are licensed under Apache 2.0 License. Authors and contributors are listed in the
[Authors](AUTHORS) file.

```
Copyright 2024 wasm-sqlite-open-helper project authors and contributors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[Wasm-sqlite-open-helper]: https://github.com/illarionov/wasm-sqlite-open-helper
[Releases]: https://github.com/illarionov/wasm-sqlite-driver-binary/releases
