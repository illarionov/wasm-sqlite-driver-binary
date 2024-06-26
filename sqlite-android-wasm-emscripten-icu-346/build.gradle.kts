/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("GENERIC_VARIABLE_WRONG_DECLARATION", "UnstableApiUsage")

import ru.pixnews.wasm.builder.base.emscripten.EMSCRIPTEN_USE_PTHREADS_ATTRIBUTE
import ru.pixnews.wasm.builder.base.icu.ICU_DATA_PACKAGING_ATTRIBUTE
import ru.pixnews.wasm.builder.base.icu.ICU_DATA_PACKAGING_STATIC
import ru.pixnews.wasm.builder.sqlite.SqliteCodeGenerationOptions
import ru.pixnews.wasm.builder.sqlite.SqliteConfigurationOptions
import ru.pixnews.wasm.builder.sqlite.SqliteExportedFunctions
import ru.pixnews.wasm.builder.sqlite.preset.setupAndroidExtensions
import ru.pixnews.wasm.builder.sqlite.preset.setupIcu
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish.WasmResourcesLinuxX64

/*
 * SQLite WebAssembly Build with Emscripten
 *  * Based on the AOSP SQLite configuration
 *  * Android-specific patches applied
 *  * Android-specific Localized collators
 *  * ICU statically compiled
 *  * No multithreading support
 */
plugins {
    id("ru.pixnews.wasm.builder.sqlite.plugin")
    id("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.kotlin")
    id("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish")
}

group = "ru.pixnews.wasm-sqlite-open-helper"
version = wasmSqliteVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "wsoh_sqlite_wasm_sqlite_android_wasm_emscripten_icu_346_version",
    envVariableName = "WSOH_SQLITE_WASM_SQLITE_ANDROID_WASM_EMSCRIPTEN_ICU_346_VERSION",
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
        create("android-icu") {
            sqliteVersion = defaultSqliteVersion
            codeGenerationOptions = SqliteCodeGenerationOptions.codeGenerationOptions
            emscriptenConfigurationOptions = SqliteCodeGenerationOptions.emscriptenConfigurationOptions -
                    "-sERROR_ON_UNDEFINED_SYMBOLS" + "-sERROR_ON_UNDEFINED_SYMBOLS=0"
            additionalSourceFiles.from("../sqlite-android-common/sqlite/wasm/api/callbacks-wasm.c")
            sqliteConfigOptions = SqliteConfigurationOptions.openHelperConfig(
                enableIcu = true,
                enableMultithreading = false,
            )
            exportedFunctions = SqliteExportedFunctions.openHelperExportedFunctions
            setupIcu(project)
            setupAndroidExtensions(project)
        }
    }
}

val wasmResourcesRootDir = layout.buildDirectory.dir("wasmLibraries")
val wasmResourcesJvmDir = wasmResourcesRootDir.map { it.dir("jvm") }
val copyJvmResourcesTask = tasks.register<Copy>("copyWasmLibrariesToJvmResources") {
    from(configurations.named("wasmSqliteReleaseElements").get().artifacts.files)

    // Temporary build with unstripped symbols for debugging. Remove in the future or move to a separate
    // build type
    from(configurations.named("wasmSqliteDebugElements").get().artifacts.files)

    into(wasmResourcesJvmDir.map { it.dir("ru/pixnews/wasm/sqlite/binary") })
    include("*.wasm")
}

kotlin {
    jvm()
    linuxX64()

    sourceSets {
        commonMain.dependencies {
            api(projects.sqliteBinaryApi)
        }
        named("jvmMain") {
            resources.srcDir(files(wasmResourcesJvmDir).builtBy(copyJvmResourcesTask))
        }
    }
}

val resourcesHelper = objects.newInstance<WasmResourcesLinuxX64>(wasmResourcesRootDir)
resourcesHelper.setupResourcesPublication()
