/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish.PublishResourcesExtension
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish.WasmPublishedResourcesConfigurator

/*
 * Convention plugin that configures the creation and publication of wasm binaries as resources
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

private val publishResourcesExtension = extensions.create("publishedResources", PublishResourcesExtension::class.java)

pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
    val wasmFiles = publishResourcesExtension.files
    val resourcesConfigurator: WasmPublishedResourcesConfigurator = objects.newInstance()
    extensions.configure<KotlinMultiplatformExtension> {
        val targetsWithResources = setOf(
            "iosArm64",
            "iosSimulatorArm64",
            "iosX64",
            "linuxX64",
            "macosArm64",
            "macosX64",
            // XX: js and mingwX64 are disabled until needed
        )
        targets.matching { it.name in targetsWithResources }.configureEach {
            resourcesConfigurator.setupNativeJsResources(
                linuxTarget = this,
                wasmFiles = wasmFiles,
                projectName = project.name,
                projectVersion = provider { project.version.toString() },
                archiveBaseName = project.extensions.getByType<BasePluginExtension>().archivesName,
            )
        }

        sourceSets
            .matching { it.name == "jvmMain" }
            .configureEach {
                resourcesConfigurator.setupJvmResources(
                    kotlinJvmSourceSet = this,
                    wasmFiles = wasmFiles,
                    jvmResourcesPackage = "ru.pixnews.wasm.sqlite.binary",
                )
            }
    }
}
