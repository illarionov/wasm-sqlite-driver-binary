/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage", "GENERIC_VARIABLE_WRONG_DECLARATION")

package ru.pixnews.wasm.builder.sqlite

import ru.pixnews.wasm.builder.base.ext.capitalizeAscii
import ru.pixnews.wasm.builder.emscripten.EmscriptenBuildTask
import ru.pixnews.wasm.builder.emscripten.WasmStripTask
import ru.pixnews.wasm.builder.sqlite.internal.BuildDirPath
import ru.pixnews.wasm.builder.sqlite.internal.BuildDirPath.STRIPPED_RESULT_DIR
import ru.pixnews.wasm.builder.sqlite.internal.SqliteAdditionalArgumentProvider
import ru.pixnews.wasm.builder.sqlite.internal.createSqliteSourceConfiguration
import ru.pixnews.wasm.builder.sqlite.internal.setupUnpackingSqliteAttributes

// Convention Plugin for building Sqlite WebAssembly using Emscripten
plugins {
    base
}

setupUnpackingSqliteAttributes(
    androidSqlitePatchFile = project.layout.projectDirectory.file(
        provider { "../sqlite-android-common/android/Android.patch" },
    ),
)

internal val wasmConfigurations = SqliteWasmConfigurations.Factory(objects, configurations).build()

private val sqliteExtension = extensions.create(
    "sqlite3Build",
    SqliteWasmBuilderExtension::class.java,
    provider {
        versionCatalogs.named("libs").findVersion("emscripten").get().toString()
    },
)

afterEvaluate {
    sqliteExtension.builds.configureEach {
        setupTasksForBuild(this, sqliteExtension.emscriptenVersion)
        setupPackTask(this)
        setupOutgoingArtifacts(this)
    }
}

private fun setupTasksForBuild(
    buildSpec: SqliteWasmBuildSpec,
    emscriptenVersion: Provider<String>,
) {
    val sqlite3c: FileCollection = if (buildSpec.sqlite3Source.isEmpty) {
        createSqliteSourceConfiguration(buildSpec.sqliteVersion)
    } else {
        buildSpec.sqlite3Source
    }
    val unstrippedWasmFileName = buildSpec.wasmUnstrippedFileName
    val unstrippedJsFileName = unstrippedWasmFileName.map { it.substringBeforeLast(".wasm") + ".mjs" }
    val strippedWasmFileName = buildSpec.wasmFileName
    val buildName = buildSpec.name.capitalizeAscii()

    val buildSqliteTask = buildSpec.buildTask
    buildSqliteTask.configure {
        val sqlite3cFile = sqlite3c.elements.map { it.first().asFile }

        group = "Build"
        description = "Compiles SQLite `$buildName` for Wasm"
        sourceFiles.from(buildSpec.additionalSourceFiles)
        outputFileName = unstrippedJsFileName
        outputDirectory = layout.buildDirectory.dir(BuildDirPath.compileUnstrippedResultDir(buildSpec.name))
        emscriptenSdk.emccVersion = emscriptenVersion
        includes.setFrom(
            sqlite3cFile.map { it.parentFile },
            buildSpec.additionalIncludes,
        )
        libs.setFrom(buildSpec.additionalLibs)

        val additionalArgsProvider = SqliteAdditionalArgumentProvider(
            sqlite3cFile,
            codeGenerationOptions = buildSpec.codeGenerationFlags,
            codeOptimizationOptions = buildSpec.codeOptimizationFlags,
            emscriptenConfigurationOptions = buildSpec.emscriptenFlags,
            exportedFunctions = buildSpec.exportedFunctions,
            sqliteConfigOptions = buildSpec.sqliteFlags,
        )
        additionalArgumentProviders.add(additionalArgsProvider)
    }

    val stripSqliteTask = buildSpec.stripTask
    stripSqliteTask.configure {
        group = "Build"
        description = "Strip compiled SQLite `$buildName` Wasm binary"
        source = buildSqliteTask.flatMap { it.outputDirectory.file(unstrippedWasmFileName.get()) }
        val dstDir = layout.buildDirectory.dir(STRIPPED_RESULT_DIR)
        destination = dstDir.zip(strippedWasmFileName) { dir, name -> dir.file(name) }
        doFirst {
            dstDir.get().asFile.let { dir ->
                dir.walkBottomUp()
                    .filter { it != dir }
                    .forEach(File::delete)
            }
        }
    }

    tasks.named("assemble").configure {
        dependsOn(stripSqliteTask)
    }
}

private fun setupPackTask(
    buildSpec: SqliteWasmBuildSpec,
) {
    val buildName = buildSpec.name
    val dstDirectory = layout.buildDirectory.dir(BuildDirPath.PACKED_OUTPUT_DIR)
    val archiveFileName = buildSpec.wasmUnstrippedFileName.map { it.substringBefore(".wasm") + ".zip" }
    buildSpec.packEmscriptenOutputTask.configure {
        description = "Pack Emscripten output for `$buildName`"
        destinationDirectory = dstDirectory
        this.archiveFileName = archiveFileName

        from(buildSpec.buildTask.flatMap(EmscriptenBuildTask::outputDirectory))
        from(buildSpec.stripTask.flatMap(WasmStripTask::destination))
    }
    tasks.named("assemble").configure {
        dependsOn(buildSpec.packEmscriptenOutputTask)
    }
}

private fun setupOutgoingArtifacts(
    buildSpec: SqliteWasmBuildSpec,
) {
    wasmConfigurations.wasmDebugElements.outgoing {
        artifact(buildSpec.unstrippedWasmOutput)
    }
    wasmConfigurations.wasmReleaseElements.outgoing {
        artifact(buildSpec.strippedWasmOutput)
    }
    wasmConfigurations.wasmSqliteEmscriptenArchiveElements.outgoing {
        artifact(buildSpec.emscriptenPackedOutput)
    }
}
