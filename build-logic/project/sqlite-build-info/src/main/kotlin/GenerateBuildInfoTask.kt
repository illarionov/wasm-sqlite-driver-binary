/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.buildinfo

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class GenerateBuildInfoTask @Inject constructor(
    objects: ObjectFactory,
) : DefaultTask() {
    @get:Nested
    val configuration: Property<WasmSqliteBuildInfo> = objects.property()

    @get:Internal
    val dstDirectory: DirectoryProperty = objects.directoryProperty()

    @get:OutputDirectory
    val androidOutputDirectory: DirectoryProperty = objects.directoryProperty()
        .convention(dstDirectory.map { it.dir("androidMain") })

    @get:OutputDirectory
    val nativeOutputDirectory: DirectoryProperty = objects.directoryProperty()
        .convention(dstDirectory.map { it.dir("nativeMain") })

    @get:OutputDirectory
    val commonOutputDirectory: DirectoryProperty = objects.directoryProperty()
        .convention(dstDirectory.map { it.dir("commonMain") })

    @get:OutputDirectory
    val jsOutputDirectory: DirectoryProperty = objects.directoryProperty()
        .convention(dstDirectory.map { it.dir("jsMain") })

    @get:OutputDirectory
    val jvmOutputDirectory: DirectoryProperty = objects.directoryProperty()
        .convention(dstDirectory.map { it.dir("jvmMain") })

    @TaskAction
    fun generate() {
        listOf(
            commonOutputDirectory,
            androidOutputDirectory,
            jvmOutputDirectory,
            nativeOutputDirectory,
            jsOutputDirectory,
        )
            .map { it.get().asFile }
            .forEach { outputDirectory ->
                outputDirectory
                    .walkBottomUp()
                    .filter { it != outputDirectory }
                    .forEach {
                        it.deleteRecursively()
                    }
            }

        val generator = WasmSqliteBuildInfoGenerator(configuration.get())
        generator.run {
            generateCommonCode(commonOutputDirectory.get().asFile)
            generateAndroidActualCode(androidOutputDirectory.get().asFile)
            generateJvmActualCode(jvmOutputDirectory.get().asFile)
            generateNativeOrJsActualCode(nativeOutputDirectory.get().asFile)
            generateNativeOrJsActualCode(jsOutputDirectory.get().asFile)
        }
    }
}
