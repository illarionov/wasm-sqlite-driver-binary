/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("GENERIC_VARIABLE_WRONG_DECLARATION", "UnstableApiUsage")

import ru.pixnews.wasm.builder.base.emscripten.EMSCRIPTEN_USE_PTHREADS_ATTRIBUTE
import ru.pixnews.wasm.builder.base.icu.ICU_DATA_PACKAGING_ATTRIBUTE
import ru.pixnews.wasm.builder.base.icu.ICU_DATA_PACKAGING_STATIC
import ru.pixnews.wasm.builder.sqlite.SqliteCodeGenerationCompilerSettings
import ru.pixnews.wasm.builder.sqlite.SqliteCompilerFlags
import ru.pixnews.wasm.builder.sqlite.SqliteExportedFunctions
import ru.pixnews.wasm.builder.sqlite.preset.setupAndroidExtensions
import ru.pixnews.wasm.builder.sqlite.preset.setupIcu
import ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.ext.fromSqliteBuild

/*
 * SQLite WebAssembly Build with Emscripten
 *  * Configuration similar to AOSP
 *  * Android-specific patches applied
 *  * Android-specific Localized collators
 *  * Multithreading using pthread
 *  * ICU statically compiled
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
    propertiesFileKey = "wsoh_sqlite_wasm_sqlite_android_wasm_emscripten_icu_mt_pthread_346_version",
    envVariableName = "WSOH_SQLITE_WASM_SQLITE_ANDROID_WASM_EMSCRIPTEN_ICU_MT_PTHREAD_346_VERSION",
).get()

dependencies {
    "wasmLibraries"(projects.icuWasm) {
        attributes {
            attribute(EMSCRIPTEN_USE_PTHREADS_ATTRIBUTE, true)
            attribute(ICU_DATA_PACKAGING_ATTRIBUTE, ICU_DATA_PACKAGING_STATIC)
        }
    }
}

sqlite3Build {
    val defaultSqliteVersion = versionCatalogs.named("libs").findVersion("sqlite").get().toString()

    builds {
        create("android-wasm-emscripten-icu-mt-pthread-346") {
            sqliteVersion = defaultSqliteVersion
            codeGenerationFlags = SqliteCodeGenerationCompilerSettings.codeGenerationFlagsMultithread
            emscriptenFlags = SqliteCodeGenerationCompilerSettings.emscriptenFlagsMultithread
            sqliteFlags = SqliteCompilerFlags.openHelperConfig(
                enableIcu = true,
                enableMultithreading = true,
            )
            exportedFunctions = SqliteExportedFunctions.openHelperExportedFunctionsMultithread
            setupIcu(project)
            setupAndroidExtensions(project)
        }
    }
}

sqliteConfigGenerator {
    configurations {
        create("android-wasm-emscripten-icu-mt-pthread-346") {
            fromSqliteBuild(objects, sqlite3Build)
        }
    }
}

kotlin {
    androidTarget()
    jvm()
    linuxX64()

    sourceSets {
        commonMain.dependencies {
            api(projects.sqliteBinaryApi)
        }
    }
}

android {
    namespace = "ru.pixnews.wasm.sqlite.binary.emscriptenicumtpthread346"
}
