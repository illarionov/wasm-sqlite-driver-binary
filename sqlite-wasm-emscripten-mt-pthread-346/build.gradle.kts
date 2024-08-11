/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
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
 *  * Multithreading using pthread
 */
plugins {
    id("ru.pixnews.wasm.builder.sqlite.plugin")
    id("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.kotlin")
    id("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish")
    id("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.android-library")
    id("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.wasm-resources")
    id("ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.generator")
    id("ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.ext.utils")
}

group = "ru.pixnews.wasm-sqlite-open-helper"
version = wasmSqliteVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "wsoh_sqlite_wasm_sqlite_wasm_emscripten_mt_pthread_346_version",
    envVariableName = "WSOH_SQLITE_WASM_SQLITE_WASM_EMSCRIPTEN_MT_PTHREAD_346_VERSION",
).get()

sqlite3Build {
    val defaultSqliteVersion = versionCatalogs.named("libs").findVersion("sqlite").get().toString()

    builds {
        create("wasm-emscripten-mt-pthread-346") {
            sqliteVersion = defaultSqliteVersion
            codeGenerationFlags = SqliteCodeGenerationFlags.codeGenerationFlagsMultithread
            emscriptenFlags = SqliteCodeGenerationFlags.emscriptenFlagsMultithread
                .filter { !it.startsWith("-sINITIAL_MEMORY=") }
                .toList() + "-sINITIAL_MEMORY=4194304"
            sqliteFlags = OpenHelperConfig.getBuildFlags(
                enableIcu = false,
                enableMultithreading = true,
            )
            exportedFunctions = SqliteExportedFunctions.openHelperExportedFunctionsMultithread
        }
    }
}

sqliteConfigGenerator {
    configurations {
        create("wasm-emscripten-mt-pthread-346") {
            fromSqliteBuild(objects, sqlite3Build)
        }
    }
}

kotlin {
    androidTarget()
    jvm()
    linuxArm64()
    linuxX64()

    sourceSets {
        commonMain.dependencies {
            api(projects.sqliteBinaryApi)
        }
    }
}

android {
    namespace = "ru.pixnews.wasm.sqlite.binary.emscriptenmtpthread346"
}
