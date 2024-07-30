/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.buildinfo

import assertk.assertThat
import assertk.assertions.contains
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.io.TempDir
import java.io.File

class WasmSqliteBuildInfoGeneratorTest {
    @TempDir
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
                "wsohResources/test/sqlite3-android-icu-3460000.wasm",
            ),
        ),
        ShouldContainTextTestCase(
            name = "Native or JS",
            textFactory = WasmSqliteBuildInfoGenerator::generateNativeOrJsActualCode,
            expectContainsText = arrayOf(
                "wsohResources/test/sqlite3-android-icu-3460000.wasm",
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

    private class ShouldContainTextTestCase(
        val name: String,
        val textFactory: WasmSqliteBuildInfoGenerator.(codegenDir: File) -> Unit,
        vararg val expectContainsText: String,
    )
}
