/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isFile
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.framework.MultiplatformProjectExtension
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.framework.MultiplatformProjectExtension.Companion.build
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.framework.RootProjectDsl
import java.io.File
import java.util.zip.ZipFile

class PublishTest {
    @JvmField
    @RegisterExtension
    var projectBuilder = MultiplatformProjectExtension()

    @Test
    fun `can publish project with resources`() {
        val testProject: RootProjectDsl = projectBuilder.setupTestProject("lib-simple-producer")
        val result = projectBuilder.build("publishAllPublicationsToTestRepository")

        val commonResultFile = File(
            testProject.rootDir,
            "lib-simple-producer/build/repo/com/example/" +
                    "lib-simple-producer/9999/lib-simple-producer-9999-kotlin_resources.zip",
        )

        assertThat(result.output).contains("BUILD SUCCESSFUL")
        assertResourcesText(commonResultFile)
    }

    private fun assertResourcesText(
        resourcesFile: File,
    ) {
        assertThat(resourcesFile).isFile()
        val linuxX64ResourceText = ZipFile(resourcesFile).use { zipFile ->
            val resourceEntry = zipFile.getEntry("wsohResources/lib_simple_producer/resource.txt.wasm")
            zipFile.getInputStream(resourceEntry).bufferedReader().use {
                it.readText()
            }
        }

        assertThat(linuxX64ResourceText).isEqualTo("test resource\n")
    }
}
