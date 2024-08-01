/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.ext

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import ru.pixnews.wasm.builder.sqlite.SqliteWasmBuildSpec
import ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.WasmSqliteBuildInfo

class WasmSqliteBuildInfoExtTest {
    private val project: Project = ProjectBuilder.builder().build()
    private val objects: ObjectFactory = project.objects

    @Test
    fun fromSqliteBuild_success_case() {
        val testBuildSpec = objects.newInstance(SqliteWasmBuildSpec::class.java, "testSpec").apply {
            sqliteVersion.set("123")
            wasmBaseFileName.set("testSqlite")
            emscriptenConfigurationOptions.set(
                listOf("-sINITIAL_MEMORY=100"),
            )
            codeGenerationOptions.set(
                listOf(
                    "-pthread",
                ),
            )
        }

        val buildInfo = objects.newInstance(WasmSqliteBuildInfo::class.java, "testSpec").apply {
            fromSqliteBuild(testBuildSpec)
        }

        assertThat(buildInfo.wasmSqliteBuildClassName.get()).isEqualTo("SqliteTestSpec")
        assertThat(buildInfo.wasmFileName.get()).isEqualTo("testSqlite-testSpec-123.wasm")
        assertThat(buildInfo.minMemorySize.get()).isEqualTo(100L)
        assertThat(buildInfo.requireThreads.get()).isEqualTo(true)
    }

    @TestFactory
    fun test_readMinMemorySize(): List<DynamicTest> {
        val testConfigurationOptionsFull = listOf(
            "-sALLOW_MEMORY_GROWTH",
            "-sALLOW_TABLE_GROWTH",
            "-sINITIAL_MEMORY=100",
            "-sENVIRONMENT=worker",
        )
        return listOf(
            testConfigurationOptionsFull to 100L,
            listOf(
                "-sALLOW_MEMORY_GROWTH",
                "-sALLOW_TABLE_GROWTH",
            ) to DEFAULT_EMSCRIPTEN_MEMORY_SIZE,
            listOf("-sINITIAL_MEMORY") to DEFAULT_EMSCRIPTEN_MEMORY_SIZE,
        ).mapIndexed { index, (testConfiguration, expectedResult) ->
            DynamicTest.dynamicTest("Initial memory test $index") {
                assertThat(readMinMemorySize(testConfiguration))
                    .isEqualTo(expectedResult)
            }
        }
    }

    @ParameterizedTest
    @CsvSource(
        "-sINITIAL_MEMORY=16777216,16777216",
        "-sINITIAL_MEMORY,INITIAL_MEMORY",
        "INITIAL_MEMORY,",
        "-sALLOW_MEMORY_GROWTH,",
    )
    fun test_readEmscriptenConfigurationOption(
        testString: String,
        expectedValue: String?,
    ) {
        assertThat(readEmscriptenConfigurationOption("INITIAL_MEMORY", testString))
            .isEqualTo(expectedValue)
    }
}
