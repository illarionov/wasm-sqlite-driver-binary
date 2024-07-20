# Wasm SQLite binaries

SQLite compiled into WebAssembly and bundled into JAR archives and Kotlin Multiplatform Resources for use in the [Wasm-sqlite-open-helper] project.

The code for building SQLite has been moved to this repository after multiple unsuccessful attempts to eliminate 
full recompilation of SQLite each time there were minor changes to the Gradle build scripts in the main project.

Currently, two modules with distinct build configurations are available:

- `sqlite-android-wasm-emscripten-icu-mt-pthread-346`: SQLite compilation with multithreading support
- `sqlite-android-wasm-emscripten-icu-346`: Single-threaded version

These compilations of SQLite have patches from Android AOSP applied and some WebAssembly extensions.
The Build configuration is similar to AOSP's, with multithreading and the Android-specific Localized collator enabled.

The ICU library is statically compiled, resulting in a SQLite binary size of about 30 megabytes.
This binary is loaded into RAM during execution, so the RAM requirements are quite high.

You can check the SQLite build configuration in the implementation of the modules.

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

You can also download a snapshot of the repository from the [Releases section](https://github.com/illarionov/wasm-sqlite-open-helper/releases) 

Add the dependencies:

```kotlin
dependencies {
    // Version with multithreading
    implementation("ru.pixnews.wasm-sqlite-open-helper:sqlite-android-wasm-emscripten-icu-mt-pthread-346:0.1-alpha10")
    
    // Single-threaded version
    implementation("ru.pixnews.wasm-sqlite-open-helper:sqlite-android-wasm-emscripten-icu-346:0.1-alpha10")
}
```

For native Kotlin Multiplatform targets (`iosArm64`, `iosSimulatorArm64`, `linuxX64`, `macosX64`, etc.),
Sqlite binaries are packaged and published in a format compatible with Kotlin Multiplatform Resources (not yet 
publicly announced).
To use it in your project, you can try the [Compose Multiplatform Resources] plugin, or check the 
[resources.gradle.kts] of the main project.

[Compose Multiplatform Resources]: https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-images-resources.html
[resources.gradle.kts]: https://github.com/illarionov/wasm-sqlite-open-helper/blob/main/build-logic/project/multiplatform/src/main/kotlin/resources.gradle.kts

## Development notes

To build the project, you will need to install the following software in addition to the JVM:

 - GNU Make 
   - on macOS: `brew install make`
 - [WABT](https://github.com/WebAssembly/wabt) (The WebAssembly Binary Toolkit)
   - on macOS: `brew install wabt`
   - on Ubuntu Linux: `apt install wabt`
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
./emsdk install 3.1.62
./emsdk activate 3.1.62
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
