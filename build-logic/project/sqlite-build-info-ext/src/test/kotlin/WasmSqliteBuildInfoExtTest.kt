/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.wasm.sqlite.binary.gradle.buildinfo.ext

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import at.released.wasm.sqlite.binary.gradle.buildinfo.WasmSqliteBuildInfo
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import ru.pixnews.wasm.builder.sqlite.SqliteWasmBuildSpec
import java.io.File

class WasmSqliteBuildInfoExtTest {
    private val project: Project = ProjectBuilder.builder().build()
    private val objects: ObjectFactory = project.objects

    @Test
    fun fromSqliteBuild_success_case() {
        val testSourceFile = File("/test/source.c")
        val testIncludeFile = File("/test/include.h")
        val testLibFile = File("/test/lib.so")

        val testBuildSpec = objects.newInstance(SqliteWasmBuildSpec::class.java, "testSpec").apply {
            sqliteVersion.set("123")
            wasmBaseFileName.set("testSqlite")
            emscriptenFlags.set(listOf("-sINITIAL_MEMORY=100"))
            codeGenerationFlags.set(listOf("-pthread"))
            codeOptimizationFlags.set(listOf("-optimFlag"))
            additionalSourceFiles.setFrom(testSourceFile)
            additionalIncludes.setFrom(testIncludeFile)
            additionalLibs.setFrom(testLibFile)
            exportedFunctions.set(listOf("exportFunc"))
            sqliteFlags.set(listOf("sqliteFlag"))
        }

        val buildInfo = objects.newInstance(WasmSqliteBuildInfo::class.java, "testSpec").apply {
            fromSqliteBuild(objects, testBuildSpec, project.provider { "emscripten5.0" })
        }

        assertThat(buildInfo.wasmSqliteBuildClassName.get()).isEqualTo("SqliteTestSpec")
        assertThat(buildInfo.wasmFileName.get()).isEqualTo("testSqlite-testSpec-123.wasm")
        assertThat(buildInfo.minMemorySize.get()).isEqualTo(100L)
        assertThat(buildInfo.requireThreads.get()).isEqualTo(true)

        buildInfo.extendedInfo.get().let { extendedInfo ->
            assertThat(extendedInfo.sqliteVersion.get()).isEqualTo("123")
            assertThat(extendedInfo.emscriptenVersion.get()).isEqualTo("emscripten5.0")
        }

        buildInfo.extendedInfo.get().compilerSettings.get().let { compilerSettings ->
            assertThat(compilerSettings.additionalSourceFiles.get()).containsExactly("source.c")
            // Disabled until https://github.com/gradle/gradle/issues/27443 is fixed
            // assertThat(compilerSettings.additionalIncludes.get()).containsExactly("include.h")
            // assertThat(compilerSettings.additionalLibs.get()).containsExactly("lib.so")
            assertThat(compilerSettings.codeGenerationFlags.get()).containsExactly("-pthread")
            assertThat(compilerSettings.codeOptimizationFlags.get()).containsExactly("-optimFlag")
            assertThat(compilerSettings.exportedFunctions.get()).containsExactly("exportFunc")
            assertThat(compilerSettings.sqliteFlags.get()).containsExactly("sqliteFlag")
        }
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
