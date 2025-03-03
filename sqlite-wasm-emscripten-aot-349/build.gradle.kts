/*
 * Copyright 2024-2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("GENERIC_VARIABLE_WRONG_DECLARATION", "UnstableApiUsage")

import ru.pixnews.wasm.builder.sqlite.SqliteExportedFunctions
import ru.pixnews.wasm.builder.sqlite.preset.SqliteCodeGenerationFlags
import ru.pixnews.wasm.builder.sqlite.preset.config.OpenHelperConfig
import ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.ext.fromSqliteBuild

/*
 * SQLite WebAssembly Build with Emscripten
 *  * No multithreading support
 */
plugins {
    id("at.released.wasm2class.plugin")
    id("ru.pixnews.wasm.builder.sqlite.plugin")
    id("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.kotlin")
    id("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.android-library")
    id("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish")
    id("ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.generator")
    id("ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.ext.utils")
}

group = "ru.pixnews.wasm-sqlite-open-helper"
version = wasmSqliteVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "wsoh_sqlite_wasm_sqlite_wasm_emscripten_aot_349_version",
    envVariableName = "WSOH_SQLITE_WASM_SQLITE_WASM_EMSCRIPTEN_AOT_349_VERSION",
).get()

sqlite3Build {
    val defaultSqliteVersion = versionCatalogs.named("libs").findVersion("sqlite").get().toString()

    builds {
        create("wasm-emscripten-aot-349") {
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

private val aotRootPackage = "ru.pixnews.wasm.sqlite.binary.aot"
private val sqliteBuild = sqlite3Build.builds["wasm-emscripten-aot-349"]

wasm2class {
    targetPackage = aotRootPackage
    modules {
        create("AotWasmEmscripten349") {
            wasm = sqliteBuild.strippedWasmOutput
        }
    }
}

sqliteConfigGenerator {
    configurations {
        create(sqliteBuild.name) {
            rootPackage = aotRootPackage
            fromSqliteBuild(objects, sqlite3Build)
            wasmFileName = "AotWasmEmscripten349.meta"
        }
    }
}

kotlin {
    androidTarget()
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.sqliteBinaryApi)
        }
    }
}

android {
    namespace = aotRootPackage
}
