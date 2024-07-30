/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.ext

import ru.pixnews.wasm.builder.sqlite.SqliteWasmBuildSpec
import ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.WasmSqliteBuildInfo

internal const val DEFAULT_EMSCRIPTEN_MEMORY_SIZE: Long = 16_777_216L

fun WasmSqliteBuildInfo.fromSqliteBuild(
    sqliteWasmBuild: SqliteWasmBuildSpec,
) {
    this.wasmSqliteBuildClassName.set(
        sqliteBuildNameToClassName(sqliteWasmBuild.name),
    )
    this.wasmFileName.set(
        sqliteWasmBuild.wasmFileName,
    )
    this.minMemorySize.set(
        sqliteWasmBuild.emscriptenConfigurationOptions.map(::readMinMemorySize),
    )
    this.requireThreads.set(
        sqliteWasmBuild.codeGenerationOptions.map(::readRequireThreads),
    )
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
