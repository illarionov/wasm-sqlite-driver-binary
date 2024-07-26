/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.framework

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestWatcher
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.framework.fixtures.Paths
import java.io.File
import java.nio.file.Files
import java.util.Optional

@Suppress("TooManyFunctions")
public class MultiplatformProjectExtension : BeforeEachCallback, TestWatcher {
    private lateinit var rootDir: File

    override fun beforeEach(context: ExtensionContext?) {
        rootDir = Files.createTempDirectory("test").toFile()
    }

    override fun testSuccessful(context: ExtensionContext?) {
        cleanup()
    }

    override fun testAborted(context: ExtensionContext?, cause: Throwable?) {
        cleanup()
    }

    override fun testFailed(context: ExtensionContext?, cause: Throwable?) {
        // do not clean up, leave a temporary rootDir directory for future inspection
    }

    override fun testDisabled(context: ExtensionContext?, reason: Optional<String>?): Unit = Unit

    public fun setupTestProject(
        submoduleProjectName: String,
    ): RootProjectDsl = RootProjectDsl.setupRoot(
        rootDir,
        submoduleProjectName,
    ).apply {
        val testProjectDir = Paths.testProjectsRoot.resolve(submoduleProjectName)
        testProjectDir.copyRecursively(
            target = rootDir.resolve(submoduleProjectName),
            overwrite = true,
        )
    }

    public fun buildWithGradleVersion(
        gradleVersion: String = "8.9",
        expectFail: Boolean,
        vararg args: String,
    ): BuildResult {
        val runner = GradleRunner.create().apply {
            forwardOutput()
            withArguments(
                "--stacktrace",
                *args,
            )
            withProjectDir(rootDir)
            withGradleVersion(gradleVersion)
        }
        return if (!expectFail) {
            runner.build()
        } else {
            runner.buildAndFail()
        }
    }

    private fun cleanup() {
        rootDir.deleteRecursively()
    }

    public companion object {
        public fun MultiplatformProjectExtension.build(
            vararg args: String,
        ): BuildResult = buildWithGradleVersion(
            expectFail = false,
            args = args,
        )

        public fun MultiplatformProjectExtension.buildAndFail(
            vararg args: String,
        ): BuildResult = buildWithGradleVersion(
            expectFail = true,
            args = args,
        )
    }
}
