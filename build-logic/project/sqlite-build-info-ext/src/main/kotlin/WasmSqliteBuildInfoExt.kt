/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.ext

import org.gradle.api.file.FileSystemLocation
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.newInstance
import ru.pixnews.wasm.builder.sqlite.SqliteWasmBuildSpec
import ru.pixnews.wasm.builder.sqlite.SqliteWasmBuilderExtension
import ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.WasmSqliteBuildInfo
import ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.WasmSqliteExtendedBuildInfo
import ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.WasmSqliteExtendedBuildInfo.WasmSqliteCompilerSettings

internal const val DEFAULT_EMSCRIPTEN_MEMORY_SIZE: Long = 16_777_216L
internal const val GENERATE_EXTENDED_INFO: Boolean = true

fun WasmSqliteBuildInfo.fromSqliteBuild(
    objects: ObjectFactory,
    builderExtension: SqliteWasmBuilderExtension,
    name: String = this.name,
) = fromSqliteBuild(
    objects = objects,
    spec = builderExtension.builds.getByName(name),
    emscriptenVersion = builderExtension.emscriptenVersion,
)

fun WasmSqliteBuildInfo.fromSqliteBuild(
    objects: ObjectFactory,
    spec: SqliteWasmBuildSpec,
    emscriptenVersion: Provider<String>,
) {
    this.wasmSqliteBuildClassName.set(sqliteBuildNameToClassName(spec.name))
    this.wasmFileName.set(spec.wasmFileName)
    this.minMemorySize.set(spec.emscriptenFlags.map(::readMinMemorySize))
    this.requireThreads.set(spec.codeGenerationFlags.map(::readRequireThreads))

    if (GENERATE_EXTENDED_INFO) {
        val extendedBuildInfo = objects.newInstance<WasmSqliteExtendedBuildInfo>().apply {
            fromBuildSpec(objects, spec, emscriptenVersion)
        }

        this.extendedInfo.set(extendedBuildInfo)
    }
}

private fun WasmSqliteExtendedBuildInfo.fromBuildSpec(
    objects: ObjectFactory,
    spec: SqliteWasmBuildSpec,
    emscriptenVersion: Provider<String>,
) {
    this.sqliteVersion.set(spec.sqliteVersion)
    this.emscriptenVersion.set(emscriptenVersion)

    val additionalSourceFileNames: Provider<List<String>> =
        spec.additionalSourceFiles.elements.map { files: Set<FileSystemLocation> ->
            files.map { it.asFile.name }
        }
    val additionalIncludeNames: Provider<List<String>> =
        spec.additionalIncludes.elements.map { files: Set<FileSystemLocation> ->
            files.map { it.asFile.name }
        }
    val additionalLibNames: Provider<List<String>> =
        spec.additionalLibs.elements.map { files: Set<FileSystemLocation> ->
            files.map { it.asFile.name }
        }

    val compilerSettings = objects.newInstance<WasmSqliteCompilerSettings>().apply {
        additionalSourceFiles.set(additionalSourceFileNames)
        additionalIncludes.set(additionalIncludeNames)
        additionalLibs.set(additionalLibNames)
        codeGenerationFlags.set(spec.codeGenerationFlags)
        codeOptimizationFlags.set(spec.codeOptimizationFlags)
        emscriptenFlags.set(spec.emscriptenFlags)
        exportedFunctions.set(spec.exportedFunctions.get())
        sqliteFlags.set(spec.sqliteFlags)
    }
    this.compilerSettings.set(compilerSettings)
}

private fun sqliteBuildNameToClassName(
    buildName: String,
): String = "Sqlite${buildName.toUpperCamelCase()}"

internal fun readMinMemorySize(
    emscriptenConfigurationOptions: List<String>,
): Long {
    val size = emscriptenConfigurationOptions.firstNotNullOfOrNull {
        readEmscriptenConfigurationOption("INITIAL_MEMORY", it)
    }
    return size?.toLongOrNull() ?: DEFAULT_EMSCRIPTEN_MEMORY_SIZE
}

internal fun readEmscriptenConfigurationOption(
    parameterName: String,
    option: String,
): String? = when {
    option.startsWith("-s$parameterName=") -> option.substringAfter("=")
    option == "-s$parameterName" -> parameterName
    else -> null
}

internal fun readRequireThreads(
    codeGenerationOptions: List<String>,
): Boolean = codeGenerationOptions.contains("-pthread")
