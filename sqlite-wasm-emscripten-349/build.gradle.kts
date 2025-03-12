/*
 * Copyright 2024-2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("GENERIC_VARIABLE_WRONG_DECLARATION", "UnstableApiUsage")

import at.released.wasm.sqlite.binary.gradle.buildinfo.ext.fromSqliteBuild
import ru.pixnews.wasm.builder.sqlite.SqliteExportedFunctions
import ru.pixnews.wasm.builder.sqlite.preset.SqliteCodeGenerationFlags
import ru.pixnews.wasm.builder.sqlite.preset.config.OpenHelperConfig

/*
 * SQLite WebAssembly Build with Emscripten
 *  * No multithreading support
 */
plugins {
    id("ru.pixnews.wasm.builder.sqlite.plugin")
    id("at.released.wasm.sqlite.binary.gradle.multiplatform.kotlin")
    id("at.released.wasm.sqlite.binary.gradle.multiplatform.publish")
    id("at.released.wasm.sqlite.binary.gradle.multiplatform.android-library")
    id("at.released.wasm.sqlite.binary.gradle.multiplatform.wasm-resources")
    id("at.released.wasm.sqlite.binary.gradle.buildinfo.generator")
    id("at.released.wasm.sqlite.binary.gradle.buildinfo.ext.utils")
}

group = "at.released.wasm-sqlite-driver"
version = wasmSqliteVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "wsoh_sqlite_wasm_sqlite_wasm_emscripten_349_version",
    envVariableName = "WSOH_SQLITE_WASM_SQLITE_WASM_EMSCRIPTEN_349_VERSION",
).get()

sqlite3Build {
    val defaultSqliteVersion = versionCatalogs.named("libs").findVersion("sqlite").get().toString()

    builds {
        create("wasm-emscripten-349") {
            sqliteVersion = defaultSqliteVersion
            codeGenerationFlags = SqliteCodeGenerationFlags.codeGenerationFlags
            emscriptenFlags = SqliteCodeGenerationFlags.emscriptenFlags
                .filter { !it.startsWith("-sINITIAL_MEMORY=") }
                .filter { it != "-sERROR_ON_UNDEFINED_SYMBOLS" }
                .toList() + listOf(
                "-sINITIAL_MEMORY=4194304",
                "-sERROR_ON_UNDEFINED_SYMBOLS=0",
            )

            additionalSourceFiles.from("../sqlite-android-common/sqlite/wasm/api/callbacks-wasm.c")
            sqliteFlags = OpenHelperConfig.getBuildFlags(
                enableIcu = false,
                enableMultithreading = false,
            ) + "-DSQLITE_OMIT_UTF16"
            exportedFunctions = SqliteExportedFunctions.openHelperExportedFunctions
        }
    }
}

sqliteConfigGenerator {
    configurations {
        create("wasm-emscripten-349") {
            fromSqliteBuild(objects, sqlite3Build)
        }
    }
}

kotlin {
    androidTarget()
    jvm()
    js {
        browser()
        nodejs()
    }
    linuxArm64()
    linuxX64()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    macosX64()
    macosArm64()
    mingwX64()

    sourceSets {
        commonMain.dependencies {
            api(projects.sqliteBinaryApi)
        }
    }
}

android {
    namespace = "at.released.wasm.sqlite.binary.emscripten349"
}
