/*
 * Copyright 2024-2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("GENERIC_VARIABLE_WRONG_DECLARATION", "UnstableApiUsage")

import at.released.gradle.chicory.aot.WasmAotGeneratorTask
import ru.pixnews.wasm.builder.sqlite.SqliteExportedFunctions
import ru.pixnews.wasm.builder.sqlite.SqliteWasmBuildSpec
import ru.pixnews.wasm.builder.sqlite.preset.SqliteCodeGenerationFlags
import ru.pixnews.wasm.builder.sqlite.preset.config.OpenHelperConfig
import ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.ext.fromSqliteBuild

/*
 * SQLite WebAssembly Build with Emscripten
 *  * No multithreading support
 */
plugins {
    `java-base`
    id("at.released.gradle.chicory.aot.generator.base")
    id("ru.pixnews.wasm.builder.sqlite.plugin")
    id("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.kotlin")
    id("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish")
    id("ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.generator")
    id("ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.ext.utils")
}

group = "ru.pixnews.wasm-sqlite-open-helper"
version = wasmSqliteVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "wsoh_sqlite_wasm_sqlite_wasm_emscripten_aot_348_version",
    envVariableName = "WSOH_SQLITE_WASM_SQLITE_WASM_EMSCRIPTEN_AOT_348_VERSION",
).get()
val aotRootPackage = "ru.pixnews.wasm.sqlite.binary.aot"

sqlite3Build {
    val defaultSqliteVersion = versionCatalogs.named("libs").findVersion("sqlite").get().toString()

    builds {
        create("wasm-emscripten-348") {
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
        create("wasm-emscripten-348") {
            rootPackage = aotRootPackage
            fromSqliteBuild(objects, sqlite3Build)
        }
    }
}

val aotGeneratorTask = tasks.register<WasmAotGeneratorTask>("generateWasmEmscripten348Aot") {
    wasmBinary = sqlite3Build.builds.named("wasm-emscripten-348").flatMap(SqliteWasmBuildSpec::strippedWasmOutput)
    rootPackage = "ru.pixnews.wasm.sqlite.binary.aot"
    moduleClassBaseName = "SqliteEmscripten348"
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
