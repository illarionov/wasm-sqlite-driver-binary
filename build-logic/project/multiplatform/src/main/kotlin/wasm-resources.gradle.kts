/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.publish.maven.internal.publication.MavenPublicationInternal
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish.CompositeComponent
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish.PublishResourcesExtension
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish.PublishResourcesExtension.PublishMethod.COMMON_MODULE
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish.PublishResourcesExtension.PublishMethod.TARGETS
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish.WasmPublishedResourcesConfigurator

/*
 * Convention plugin that configures the creation and publication of wasm binaries as resources
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

private val publishResourcesExtension = extensions.create("publishedResources", PublishResourcesExtension::class.java)

pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
    val resourcesConfigurator: WasmPublishedResourcesConfigurator = objects.newInstance()

    extensions.configure<KotlinMultiplatformExtension> {
        @Suppress("SENSELESS_NULL_IN_WHEN")
        when (publishResourcesExtension.publishMethod.get()) {
            COMMON_MODULE, null -> setupCommonResources(this, resourcesConfigurator, publishResourcesExtension.files)
            TARGETS -> setupNativeOrJsTargetsResources(this, resourcesConfigurator, publishResourcesExtension.files)
        }
        setupAndroidAssets(resourcesConfigurator, publishResourcesExtension.files)
        setupJvmResources(this, resourcesConfigurator, publishResourcesExtension.files)
    }
}

private fun setupCommonResources(
    multiplatformExtension: KotlinMultiplatformExtension,
    resourcesConfigurator: WasmPublishedResourcesConfigurator,
    wasmFiles: FileCollection,
) {
    @Suppress("INVISIBLE_MEMBER")
    val rootKotlinSoftwareComponent = multiplatformExtension.rootSoftwareComponent

    val compositeComponent: CompositeComponent = objects.newInstance(rootKotlinSoftwareComponent)
    resourcesConfigurator.setupCommonResources(
        targetComponent = compositeComponent.adHocComponent,
        wasmFiles = wasmFiles,
        projectName = project.name,
        projectVersion = provider { project.version.toString() },
        archiveBaseName = project.extensions.getByType<BasePluginExtension>().archivesName,
    )

    plugins.withType<PublishingPlugin> {
        // Replace "KotlinMultiplatform" maven publication with own copy with added resources
        extensions.getByType<PublishingExtension>().publications.create<MavenPublication>("kotlinMultiplatform2") {
            from(compositeComponent)
            (this as MavenPublicationInternal).publishWithOriginalFileName()
        }

        tasks.matching {
            it.name.startsWith("publishKotlinMultiplatformPublication")
        }.configureEach {
            enabled = false
        }
    }
}

// Old version with publishing resources in target publications.
private fun setupNativeOrJsTargetsResources(
    multiplatformExtension: KotlinMultiplatformExtension,
    resourcesConfigurator: WasmPublishedResourcesConfigurator,
    wasmFiles: FileCollection = publishResourcesExtension.files,
) {
    val targetsWithResources = setOf(
        "iosArm64",
        "iosSimulatorArm64",
        "iosX64",
        "linuxX64",
        "macosArm64",
        "macosX64",
        // XX: js and mingwX64 are disabled until needed
    )

    multiplatformExtension.targets.matching { it.name in targetsWithResources }.configureEach {
        resourcesConfigurator.setupNativeOrJsResources(
            target = this,
            wasmFiles = wasmFiles,
            projectName = project.name,
            projectVersion = provider { project.version.toString() },
            archiveBaseName = project.extensions.getByType<BasePluginExtension>().archivesName,
        )
    }
}

private fun setupAndroidAssets(
    resourcesConfigurator: WasmPublishedResourcesConfigurator,
    wasmFiles: FileCollection = publishResourcesExtension.files,
) {
    plugins.withId("com.android.library") {
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants {
            resourcesConfigurator.setupAndroidAssets(
                wasmFiles = wasmFiles,
                androidVariant = it,
                projectName = project.name,
            )
        }
    }
}

private fun setupJvmResources(
    multiplatformExtension: KotlinMultiplatformExtension,
    resourcesConfigurator: WasmPublishedResourcesConfigurator,
    wasmFiles: FileCollection = publishResourcesExtension.files,
) {
    multiplatformExtension.sourceSets
        .matching { it.name == "jvmMain" }
        .configureEach {
            resourcesConfigurator.setupJvmResources(
                kotlinJvmSourceSet = this,
                wasmFiles = wasmFiles,
                jvmResourcesPackage = "ru.pixnews.wasm.sqlite.binary",
            )
        }
}
