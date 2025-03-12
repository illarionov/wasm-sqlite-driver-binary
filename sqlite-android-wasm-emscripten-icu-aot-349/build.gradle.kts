/*
 * Copyright 2024-2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("GENERIC_VARIABLE_WRONG_DECLARATION", "UnstableApiUsage")

import at.released.builder.emscripten.EmscriptenAttribute.EMSCRIPTEN_USE_PTHREADS_ATTRIBUTE
import at.released.wasm.sqlite.binary.gradle.buildinfo.ext.fromSqliteBuild
import ru.pixnews.wasm.builder.base.icu.ICU_DATA_PACKAGING_ATTRIBUTE
import ru.pixnews.wasm.builder.base.icu.ICU_DATA_PACKAGING_STATIC
import ru.pixnews.wasm.builder.sqlite.SqliteExportedFunctions
import ru.pixnews.wasm.builder.sqlite.preset.SqliteCodeGenerationFlags
import ru.pixnews.wasm.builder.sqlite.preset.config.OpenHelperConfig
import ru.pixnews.wasm.builder.sqlite.preset.setupAndroidExtensions
import ru.pixnews.wasm.builder.sqlite.preset.setupIcu

/*
 * sqlite-android-wasm-emscripten-icu-349 compiled to .class files
 */
plugins {
    id("at.released.wasm2class.plugin")
    id("ru.pixnews.wasm.builder.sqlite.plugin")
    id("at.released.wasm.sqlite.binary.gradle.multiplatform.kotlin")
    id("at.released.wasm.sqlite.binary.gradle.multiplatform.publish")
    id("at.released.wasm.sqlite.binary.gradle.buildinfo.generator")
    id("at.released.wasm.sqlite.binary.gradle.buildinfo.ext.utils")
}

group = "at.released.wasm-sqlite-driver"
version = wasmSqliteVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "wsoh_sqlite_wasm_sqlite_android_wasm_emscripten_icu_aot_349_version",
    envVariableName = "WSOH_SQLITE_WASM_SQLITE_ANDROID_WASM_EMSCRIPTEN_ICU_AOT_349_VERSION",
).get()

dependencies {
    "wasmLibraries"(projects.icuWasm) {
        attributes {
            attribute(EMSCRIPTEN_USE_PTHREADS_ATTRIBUTE, false)
            attribute(ICU_DATA_PACKAGING_ATTRIBUTE, ICU_DATA_PACKAGING_STATIC)
        }
    }
}

sqlite3Build {
    val defaultSqliteVersion = versionCatalogs.named("libs").findVersion("sqlite").get().toString()

    builds {
        create("android-wasm-emscripten-icu-aot-349") {
            sqliteVersion = defaultSqliteVersion
            codeGenerationFlags = SqliteCodeGenerationFlags.codeGenerationFlags
            emscriptenFlags = SqliteCodeGenerationFlags.emscriptenFlags -
                    "-sERROR_ON_UNDEFINED_SYMBOLS" + "-sERROR_ON_UNDEFINED_SYMBOLS=0"
            additionalSourceFiles.from("../sqlite-android-common/sqlite/wasm/api/callbacks-wasm.c")
            sqliteFlags = OpenHelperConfig.getBuildFlags(
                enableIcu = true,
                enableMultithreading = false,
            )
            exportedFunctions = SqliteExportedFunctions.openHelperExportedFunctions
            setupIcu(project)
            setupAndroidExtensions(project)
        }
    }
}

private val aotRootPackage = "at.released.wasm.sqlite.binary.aot"
private val sqliteBuild = sqlite3Build.builds["android-wasm-emscripten-icu-aot-349"]

wasm2class {
    targetPackage = aotRootPackage
    modules {
        create("SqliteAndroidWasmEmscriptenIcuAot349") {
            wasm = sqliteBuild.strippedWasmOutput
        }
    }
}

sqliteConfigGenerator {
    configurations {
        create(sqliteBuild.name) {
            rootPackage = aotRootPackage
            fromSqliteBuild(objects, sqlite3Build)
            wasmFileName = "SqliteAndroidWasmEmscriptenIcuAot349.meta"
        }
    }
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.sqliteBinaryApi)
        }
    }
}
