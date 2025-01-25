/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.builder.base.emscripten

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecOperations
import org.gradle.process.internal.ExecException
import java.io.File
import javax.inject.Inject

/**
 * Compiles system libraries and prepares the Emscripten cache for future use with embuilder.
 * It helps achieve more reproducible builds, as this compilation uses the debug-prefix-map parameter to get
 * deterministic paths in the DWARF debug info.
 */
@CacheableTask
public open class EmscriptenPrepareCacheTask @Inject constructor(
    private val execOperations: ExecOperations,
    projectLayout: ProjectLayout,
    objects: ObjectFactory,
) : DefaultTask() {
    @get:Nested
    public val emscriptenSdk: EmscriptenSdk = objects.newInstance()

    @get:OutputDirectory
    public val cacheDirectory: DirectoryProperty = objects.directoryProperty().apply {
        val projectName = project.name
        val cacheDir: Provider<File> = emscriptenSdk.emscriptenRoot.map {
            File(it, "upstream/emscripten/gradlecache-$projectName").canonicalFile
        }
        convention(projectLayout.dir(cacheDir))
    }

    @get:Input
    public val targets: ListProperty<String> = objects.listProperty<String>()
        .convention(
            listOf(
                "crtbegin",
                "libGL-getprocaddr",
                "libGL-mt-getprocaddr",
                "libal",
                "libc",
                "libc++-mt-noexcept",
                "libc++-noexcept",
                "libc++abi-mt-noexcept",
                "libc++abi-noexcept",
                "libc-mt",
                "libcompiler_rt",
                "libcompiler_rt-mt",
                "libdlmalloc",
                "libdlmalloc-mt",
                "libhtml5",
                "libnoexit",
                "libsockets",
                "libsockets-mt",
                "libstubs",
            ),
        )

    @get:Input
    public val lto: Property<LinkTimeOptimizer> = objects.property<LinkTimeOptimizer>()
        .convention(LinkTimeOptimizer.NONE)

    @get:Input
    public val pic: Property<Boolean> = objects.property<Boolean>()
        .convention(false)

    @get:Input
    public val wasm64: Property<Boolean> = objects.property<Boolean>()
        .convention(false)

    @TaskAction
    public fun build() {
        emscriptenSdk.checkEmsdkVersion()

        validateCacheDirectory()

        val cmdLine = buildCommandLine()
        val execEnv = getEmbuilderEnvironment()
        try {
            execOperations.exec {
                this.commandLine = cmdLine
                this.workingDir = workingDir
                this.environment = execEnv
            }.rethrowFailure().assertNormalExitValue()
        } catch (execException: ExecException) {
            throw ExecException("Failed to execute `$cmdLine`", execException)
        }
    }

    private fun validateCacheDirectory() {
        val emscriptenSourceDir: File = File(emscriptenSdk.emscriptenRoot.get(), "upstream/emscripten").canonicalFile
        val cacheDir: File = cacheDirectory.get().asFile.canonicalFile
        if (!cacheDir.startsWith(emscriptenSourceDir)) {
            error("cacheDirectory must be inside the Emscripten SDK root directory to work properly")
        }
    }

    private fun buildCommandLine(): List<String> = emscriptenSdk.buildEmBuilderCommandLine {
        @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
        when (lto.get()) {
            LinkTimeOptimizer.NONE -> Unit
            LinkTimeOptimizer.FULL -> add("--lto")
            LinkTimeOptimizer.THIN -> add("--lto=thin")
        }
        if (pic.get() == true) {
            add("--pic")
        }
        if (wasm64.get() == true) {
            add("--wasm64")
        }
        add("build")
        addAll(targets.get())
    }

    private fun getEmbuilderEnvironment(): Map<String, String> {
        val cacheDirectory = cacheDirectory.get().asFile.canonicalPath
        return emscriptenSdk.getEmsdkEnvironment() + mapOf(
            "EM_CACHE" to cacheDirectory,
        )
    }

    public enum class LinkTimeOptimizer {
        NONE,
        FULL,
        THIN,
    }
}
