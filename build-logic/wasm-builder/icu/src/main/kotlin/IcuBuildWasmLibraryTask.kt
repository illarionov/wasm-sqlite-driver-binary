/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.builder.icu

import at.released.builder.emscripten.EmscriptenSdk
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecOperations
import org.gradle.process.internal.ExecException
import ru.pixnews.wasm.builder.base.icu.ICU_DATA_PACKAGING_STATIC
import ru.pixnews.wasm.builder.icu.IcuBuildDefaults.ICU_PTHREADS_CFLAGS
import ru.pixnews.wasm.builder.icu.IcuBuildDefaults.ICU_PTHREADS_CXXFLAGS
import ru.pixnews.wasm.builder.icu.IcuBuildDefaults.ICU_PTHREAD_FORCE_LIBS
import java.io.File
import javax.inject.Inject

/**
 * Builds the ICU for WebAssembly using Emscripten
 */
@CacheableTask
public abstract class IcuBuildWasmLibraryTask @Inject constructor(
    private val execOperations: ExecOperations,
    objects: ObjectFactory,
) : DefaultTask() {
    @get:InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public val icuSource: DirectoryProperty = objects.directoryProperty()

    @get:Nested
    public val emscriptenSdk: EmscriptenSdk = objects.newInstance()

    @get:InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public val icuBuildToolchainDirectory: DirectoryProperty = objects.directoryProperty()

    @get:OutputDirectory
    public val outputDirectory: DirectoryProperty = objects.directoryProperty()

    @get:Internal
    public val buildDirectory: DirectoryProperty = objects.directoryProperty()

    @get:Input
    @Optional
    public val target: Property<String> = objects.property<String>()
        .convention(IcuBuildDefaults.ICU_DEFAULT_TARGET)

    @get:Input
    @Optional
    public val dataPackaging: Property<String> = objects.property(String::class.java)
        .convention(ICU_DATA_PACKAGING_STATIC)

    @get:Input
    @Optional
    public val buildFeatures: SetProperty<IcuBuildFeature> = objects.setProperty(IcuBuildFeature::class.java)
        .convention(IcuBuildFeature.DEFAULT)

    @get:Input
    @Optional
    public val icuAdditionalCflags: ListProperty<String> = objects.listProperty(String::class.java)
        .convention(IcuBuildDefaults.ICU_CFLAGS)

    @get:Input
    @Optional
    public val icuAdditionalCxxflags: ListProperty<String> = objects.listProperty(String::class.java)
        .convention(IcuBuildDefaults.ICU_CXXFLAGS)

    @get:Input
    @Optional
    public val icuForceLibs: ListProperty<String> = objects.listProperty(String::class.java)
        .convention(IcuBuildDefaults.ICU_FORCE_LIBS)

    @get:Input
    @Optional
    public val icuUsePthreads: Property<Boolean> = objects.property(Boolean::class.java)
        .convention(IcuBuildDefaults.ICU_USE_PTHREADS)

    @get:Input
    @Optional
    public val icuDataDir: Property<String> = objects.property(String::class.java)
        .convention(IcuBuildDefaults.ICU_DATA_DIR)

    @get:Internal
    public val maxJobs: Property<Int> = objects.property<Int>().convention(
        Runtime.getRuntime().availableProcessors(),
    )

    private val icuConfigurePath: File get() = icuSource.file("source/configure").get().asFile

    @TaskAction
    public fun build() {
        emscriptenSdk.checkEmsdkVersion()
        buildWasmIcu()
    }

    private fun buildWasmIcu() {
        val buildDir = buildDirectory.get().asFile
        val dstDir = outputDirectory.get().asFile
        val toolchainDir = icuBuildToolchainDirectory.get().asFile

        val hostEnv = getWasmBuildInstallEnvironment()
        val emConfigure = emscriptenSdk.buildEmconfigureCommandLine {
            add(icuConfigurePath.absolutePath)
            addAll(buildFeatures.get().toCommandLineArgs())
            add("--prefix=${dstDir.absolutePath}")
            add("--target=${target.get()}")
            add("--with-cross-build=${toolchainDir.absolutePath}")
            add("--with-data-packaging=${dataPackaging.get()}")
        }
        val emBuild = emscriptenSdk.buildEmMakeCommandLine {
            add("gmake")
            add("-j${maxJobs.get()}")
        }
        val emInstall = emscriptenSdk.buildEmMakeCommandLine {
            add("gmake")
            add("install")
            add("-j${maxJobs.get()}")
        }

        buildDir.deleteRecursively()
        buildDir.mkdirs()

        listOf(
            emConfigure,
            emBuild,
            emInstall,
        ).forEach { cmdLine ->
            try {
                execOperations.exec {
                    commandLine = cmdLine
                    workingDir = buildDir
                    environment = hostEnv
                }.rethrowFailure().assertNormalExitValue()
            } catch (execException: ExecException) {
                throw ExecException("Failed to execute `$cmdLine`", execException)
            }
        }
    }

    private fun getWasmBuildInstallEnvironment(): Map<String, String> {
        val cflags = icuAdditionalCflags.getWithPthreadDefaults(ICU_PTHREADS_CFLAGS) + getCflagsDataDirDefaults()
        val cxxflags = icuAdditionalCxxflags.getWithPthreadDefaults(ICU_PTHREADS_CXXFLAGS) + getCflagsDataDirDefaults()

        val env: Map<String, String> = buildMap {
            putAll(System.getenv().filterKeys { it == "PATH" })
            put("CFLAGS", cflags.joinToString(" "))
            put("CXXFLAGS", cxxflags.joinToString(" "))
            put("FORCE_LIBS", icuForceLibs.getWithPthreadDefaults(ICU_PTHREAD_FORCE_LIBS).joinToString(" "))
            put("PKGDATA_OPTS", IcuBuildDefaults.ICU_PKGDATA_OPTS.joinToString(" "))
            put("PATH", System.getenv()["PATH"] ?: "")
            emscriptenSdk.emscriptenCacheDir.let {
                if (it.isPresent) {
                    put("EM_CACHE", it.get().asFile.canonicalPath)
                }
            }
        }
        return env
    }

    private fun Provider<List<String>>.getWithPthreadDefaults(
        pthreadDefaults: List<String>,
    ) = this.get() + if (icuUsePthreads.get()) {
        pthreadDefaults
    } else {
        emptyList()
    }

    private fun getCflagsDataDirDefaults(): List<String> = if (icuDataDir.isPresent) {
        listOf("""-DICU_DATA_DIR='"${icuDataDir.get()}"'""")
    } else {
        listOf()
    }

    private companion object {
        private fun Set<IcuBuildFeature>.toCommandLineArgs(): List<String> = IcuBuildFeature.values().map { feature ->
            if (feature in this) {
                "--enable-${feature.id}"
            } else {
                "--disable-${feature.id}"
            }
        }
    }
}
