/*
 * Copyright 2024-2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("GENERIC_VARIABLE_WRONG_DECLARATION", "UnstableApiUsage")

import at.released.gradle.chicory.aot.WasmAotGeneratorTask
import ru.pixnews.wasm.builder.sqlite.SqliteWasmBuildSpec
import ru.pixnews.wasm.builder.base.emscripten.EMSCRIPTEN_USE_PTHREADS_ATTRIBUTE
import ru.pixnews.wasm.builder.base.icu.ICU_DATA_PACKAGING_ATTRIBUTE
import ru.pixnews.wasm.builder.base.icu.ICU_DATA_PACKAGING_STATIC
import ru.pixnews.wasm.builder.sqlite.SqliteExportedFunctions
import ru.pixnews.wasm.builder.sqlite.preset.SqliteCodeGenerationFlags
import ru.pixnews.wasm.builder.sqlite.preset.config.OpenHelperConfig
import ru.pixnews.wasm.builder.sqlite.preset.setupAndroidExtensions
import ru.pixnews.wasm.builder.sqlite.preset.setupIcu
import ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.ext.fromSqliteBuild

/*
 * sqlite-android-wasm-emscripten-icu-348 compiled to .class files
 */
plugins {
    id("at.released.gradle.chicory.aot.generator.base")
    id("ru.pixnews.wasm.builder.sqlite.plugin")
    id("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.kotlin")
    id("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish")
    id("ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.generator")
    id("ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.ext.utils")
}

group = "ru.pixnews.wasm-sqlite-open-helper"
version = wasmSqliteVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "wsoh_sqlite_wasm_sqlite_android_wasm_emscripten_icu_aot_348_version",
    envVariableName = "WSOH_SQLITE_WASM_SQLITE_ANDROID_WASM_EMSCRIPTEN_ICU_AOT_348_VERSION",
).get()

dependencies {
    "wasmLibraries"(projects.icuWasm) {
        attributes {
            attribute(EMSCRIPTEN_USE_PTHREADS_ATTRIBUTE, false)
            attribute(ICU_DATA_PACKAGING_ATTRIBUTE, ICU_DATA_PACKAGING_STATIC)
        }
    }
}

val aotRootPackage = "ru.pixnews.wasm.sqlite.binary.aot"

sqlite3Build {
    val defaultSqliteVersion = versionCatalogs.named("libs").findVersion("sqlite").get().toString()

    builds {
        create("android-wasm-emscripten-icu-aot-348") {
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

sqliteConfigGenerator {
    configurations {
        create("android-wasm-emscripten-icu-aot-348") {
            rootPackage = aotRootPackage
            fromSqliteBuild(objects, sqlite3Build)
        }
    }
}

val aotGeneratorTask = tasks.register<WasmAotGeneratorTask>("generateAndroidWasmEmscriptenIcu348Aot") {
    wasmBinary = sqlite3Build.builds.named("android-wasm-emscripten-icu-aot-348").flatMap(SqliteWasmBuildSpec::strippedWasmOutput)
    rootPackage = "ru.pixnews.wasm.sqlite.binary.aot"
    moduleClassBaseName = "SqliteAndroidEmscriptenIcuAot348"
}

val outputClasses = files(aotGeneratorTask.flatMap(WasmAotGeneratorTask::outputClasses))

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.sqliteBinaryApi)
        }
        jvmMain.dependencies {
            api(libs.chicory.runtime)
            compileOnly(outputClasses)
        }
    }
}

java {
    sourceSets.named("jvmMain") {
        java.srcDir(aotGeneratorTask.flatMap(WasmAotGeneratorTask::outputSources))
        resources.srcDir(aotGeneratorTask.flatMap(WasmAotGeneratorTask::outputResources))
        resources.srcDir(outputClasses)
    }
}
