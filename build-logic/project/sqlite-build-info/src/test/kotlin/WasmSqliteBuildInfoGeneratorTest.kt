/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalCompilerApi::class)

package ru.pixnews.wasm.sqlite.binary.gradle.buildinfo

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode.OK
import com.tschuchort.compiletesting.SourceFile
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.CleanupMode.ON_SUCCESS
import org.junit.jupiter.api.io.TempDir
import ru.pixnews.wasm.sqlite.binary.gradle.buildinfo.WasmSqliteExtendedBuildInfo.WasmSqliteCompilerSettings
import java.io.File
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class WasmSqliteBuildInfoGeneratorTest {
    @TempDir(cleanup = ON_SUCCESS)
    var codeGenDir: File? = null
    private val project: Project = ProjectBuilder.builder().build()
    private val objects: ObjectFactory = project.objects

    @TestFactory
    fun generated_config_should_contain_correct_url(): List<DynamicTest> = listOf(
        ShouldContainTextTestCase(
            name = "Jvm",
            textFactory = WasmSqliteBuildInfoGenerator::generateJvmActualCode,
            expectContainsText = arrayOf(
                "SqliteAndroidWasmEmscriptenIcu346::class.java.getResource",
                "sqlite3-android-icu-3460000.wasm",
            ),
        ),
        ShouldContainTextTestCase(
            name = "Android",
            textFactory = WasmSqliteBuildInfoGenerator::generateAndroidActualCode,
            expectContainsText = arrayOf(
                "cassettes/test/sqlite3-android-icu-3460000.wasm",
            ),
        ),
        ShouldContainTextTestCase(
            name = "Native or JS",
            textFactory = WasmSqliteBuildInfoGenerator::generateNativeOrJsActualCode,
            expectContainsText = arrayOf(
                "cassettes/test/sqlite3-android-icu-3460000.wasm",
            ),
        ),
    ).map { testParams ->
        DynamicTest.dynamicTest("Generated config for ${testParams.name} should contain correct url") {
            val buildInfo: WasmSqliteBuildInfo = objects.newInstance("testSqlite")

            val generator = WasmSqliteBuildInfoGenerator(buildInfo)
            testParams.textFactory(generator, codeGenDir!!)

            val generatedFileContent = codeGenDir!!.resolve(
                "ru/pixnews/wasm/sqlite/binary/SqliteAndroidWasmEmscriptenIcu346.kt",
            ).readText()

            assertThat(generatedFileContent).contains(expected = testParams.expectContainsText)
        }
    }

    @Test
    fun generated_config_should_contain_correct_extended_info() {
        val testCompilerSettingsMap = mapOf(
            "additionalSourceFiles" to listOf("source1", "source2", "source3"),
            "additionalIncludes" to listOf("include1", "include2", "include3"),
            "additionalLibs" to listOf("lib1", "lib2", "lib3"),
            "codeGenerationFlags" to listOf("-codegenFlag1", "-codegenFlag2", "-codegenFlag3"),
            "codeOptimizationFlags" to listOf("-codeoptimFlag1", "-codeoptimFlag2", "-codeoptimFlag3"),
            "emscriptenFlags" to listOf("-emFlag1", "-emFlag2", "-emFlag3"),
            "exportedFunctions" to listOf("func1", "func2", "func3"),
            "sqliteFlags" to listOf("sqliteFlag1", "sqliteFlag2", "sqliteFlag3"),
        )

        val testCompilerSettings = objects.newInstance<WasmSqliteCompilerSettings>().apply {
            additionalSourceFiles.set(testCompilerSettingsMap["additionalSourceFiles"])
            additionalIncludes.set(testCompilerSettingsMap["additionalIncludes"])
            additionalLibs.set(testCompilerSettingsMap["additionalLibs"])
            codeGenerationFlags.set(testCompilerSettingsMap["codeGenerationFlags"])
            codeOptimizationFlags.set(testCompilerSettingsMap["codeOptimizationFlags"])
            emscriptenFlags.set(testCompilerSettingsMap["emscriptenFlags"])
            exportedFunctions.set(testCompilerSettingsMap["exportedFunctions"])
            sqliteFlags.set(testCompilerSettingsMap["sqliteFlags"])
        }
        val testExtendedBuildInfo = objects.newInstance<WasmSqliteExtendedBuildInfo>().apply {
            sqliteVersion.set("testSqliteVersion")
            emscriptenVersion.set("testEmscriptenVersion")
            compilerSettings.set(testCompilerSettings)
        }
        val buildInfo: WasmSqliteBuildInfo = objects.newInstance<WasmSqliteBuildInfo>("testSqlite").apply {
            extendedInfo.set(testExtendedBuildInfo)
        }
        val generator = WasmSqliteBuildInfoGenerator(buildInfo)

        generator.generateCommonCode(codeGenDir!!)
        val generatedExtendedInfo = SourceFile.fromPath(
            codeGenDir!!.resolve(
                "ru/pixnews/wasm/sqlite/binary/SqliteAndroidWasmEmscriptenIcu346ExtendedBuildInfo.kt",
            ),
        )

        val compilation: JvmCompilationResult = KotlinCompilation().apply {
            sources = listOf(generatedExtendedInfo, wasmSqliteCompilerSettingsStub, wasmSqliteExtendedBuildInfoStub)
            inheritClassPath = false
            messageOutputStream = System.out
        }.compile()

        assertThat(compilation.exitCode).isEqualTo(OK)

        val resultBuildInfo = compilation.classLoader.loadClass(
            "ru.pixnews.wasm.sqlite.binary.SqliteAndroidWasmEmscriptenIcu346ExtendedBuildInfo",
        ).kotlin.objectInstance!!
        assertThat(resultBuildInfo.readProp<String>("sqliteVersion"))
            .isEqualTo("testSqliteVersion")
        assertThat(resultBuildInfo.readProp<String>("emscriptenVersion"))
            .isEqualTo("testEmscriptenVersion")

        val resultCompilerSettings: Any = resultBuildInfo.readProp("compilerSettings")
        testCompilerSettingsMap.forEach { (propertyName, expectedValue) ->
            assertThat(resultCompilerSettings.readProp<List<String>?>(propertyName))
                .isNotNull()
                .containsExactly(elements = expectedValue.toTypedArray())
        }
    }

    private inline fun <reified R : Any?> Any.readProp(
        propertyName: String,
    ): R {
        @Suppress("UNCHECKED_CAST")
        val property = this::class.memberProperties.first { it.name == propertyName } as KProperty1<Any, R>
        return property.get(this)
    }

    private class ShouldContainTextTestCase(
        val name: String,
        val textFactory: WasmSqliteBuildInfoGenerator.(codegenDir: File) -> Unit,
        vararg val expectContainsText: String,
    )

    private companion object {
        val wasmSqliteCompilerSettingsStub = SourceFile.kotlin(
            "WasmSqliteCompilerSettings.kt",
            """
                |package ru.pixnews.wasm.sqlite.binary.base
                |interface WasmSqliteCompilerSettings {
                |    val additionalSourceFiles: List<String>?
                |    val additionalIncludes: List<String>?
                |    val additionalLibs: List<String>
                |    val codeGenerationFlags: List<String>
                |    val codeOptimizationFlags: List<String>
                |    val emscriptenFlags: List<String>
                |    val exportedFunctions: List<String>
                |    val sqliteFlags: List<String>
                |}
            """.trimMargin(),
        )
        val wasmSqliteExtendedBuildInfoStub = SourceFile.kotlin(
            "WasmSqliteExtendedBuildInfo.kt",
            """
                |package ru.pixnews.wasm.sqlite.binary.base
                |
                |interface WasmSqliteExtendedBuildInfo {
                |    val sqliteVersion: String
                |    val emscriptenVersion: String
                |    val compilerSettings: WasmSqliteCompilerSettings?
                |}
            """.trimMargin(),
        )
    }
}
