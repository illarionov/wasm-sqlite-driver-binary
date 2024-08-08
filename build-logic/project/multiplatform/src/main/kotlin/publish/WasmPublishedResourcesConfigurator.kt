/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage")

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish

import com.android.build.api.variant.Variant
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ConsumableConfiguration
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.register
import org.gradle.language.cpp.CppBinary
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import javax.inject.Inject

internal open class WasmPublishedResourcesConfigurator @Inject constructor(
    layout: ProjectLayout,
    private val tasks: TaskContainer,
    private val objects: ObjectFactory,
    private val providers: ProviderFactory,
    private val configurations: ConfigurationContainer,
    private val softwareComponentFactory: SoftwareComponentFactory,
) {
    private val wasmPaths = WasmLibraryResourcesPaths(layout)

    fun setupAndroidAssets(
        wasmFiles: FileCollection,
        androidVariant: Variant,
        projectName: String,
        resourcePackage: String = getResourcePackage(projectName),
    ) {
        val assetsSubdirectory = "wsohResources/$resourcePackage/"
        val variantName = androidVariant.name.capitalizeAscii()

        @Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
        val copyAssetsTask = tasks.register<CopyAssetsTask>("copyWasmLibrariesTo${variantName}Assets") {
            this.wasmFiles.from(wasmFiles)
            this.subdirectoryInAssets.set(assetsSubdirectory)
        }

        androidVariant.sources.assets?.addGeneratedSourceDirectory(
            copyAssetsTask,
            CopyAssetsTask::outputDirectory,
        )
    }

    fun setupJvmResources(
        kotlinJvmSourceSet: KotlinSourceSet,
        wasmFiles: FileCollection,
        jvmResourcesPackage: String,
    ) {
        val wasmResourcesJvmDir = wasmPaths.jvmRoot
        val jvmPackageSubdir = jvmResourcesPackage.replace(".", "/")

        @Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
        val copyJvmResourcesTask = tasks.register<Sync>("copyWasmLibrariesToJvmResources") {
            from(wasmFiles)
            into(wasmResourcesJvmDir.map { it.dir(jvmPackageSubdir) })
            include("*.wasm")
        }

        val resourcesDir = objects.fileCollection().apply {
            from(wasmResourcesJvmDir)
            builtBy(copyJvmResourcesTask)
        }

        kotlinJvmSourceSet.resources.srcDir(resourcesDir)
    }

    fun setupCommonResources(
        targetComponent: AdhocComponentWithVariants,
        wasmFiles: FileCollection,
        projectName: String,
        projectVersion: Provider<String>,
        archiveBaseName: Provider<String> = providers.provider { projectName },
    ) {
        val packZipForPublicationTask = setupPackageResourcesTask(
            targetName = "common",
            wasmFiles = wasmFiles,
            projectName = projectName,
            projectVersion = projectVersion,
            archiveBaseName = archiveBaseName,
        )
        val publishedConfiguration = createConfigurationWithArchiveArtifact(
            null,
            packZipForPublicationTask.flatMap { it.archiveFile },
        )
        targetComponent.addVariantsFromConfiguration(publishedConfiguration) {
            mapToMavenScope("runtime")
        }
    }

    fun setupNativeOrJsResources(
        target: KotlinTarget,
        wasmFiles: FileCollection,
        projectName: String,
        projectVersion: Provider<String>,
        archiveBaseName: Provider<String> = providers.provider { projectName },
    ) {
        val packZipForPublicationTask = setupPackageResourcesTask(
            targetName = target.targetName,
            wasmFiles = wasmFiles,
            projectName = projectName,
            projectVersion = projectVersion,
            archiveBaseName = archiveBaseName,
        )
        val publishedConfiguration = createConfigurationWithArchiveArtifact(
            target,
            packZipForPublicationTask.flatMap { it.archiveFile },
        )
        target.addVariantsFromConfigurationsToPublication(publishedConfiguration) {
            mapToMavenScope("runtime")
        }
    }

    fun setupDebugSymbolsPublishedComponent(
        debugWasmFiles: FileCollection,
        projectName: String,
        projectVersion: Provider<String>,
        archiveBaseName: Provider<String> = providers.provider { projectName },
        componentName: String = "debugsymbols",
    ): AdhocComponentWithVariants {
        val packZipForPublicationTask = setupPackageResourcesTask(
            targetName = "commonDebug",
            wasmFiles = debugWasmFiles,
            projectName = projectName,
            projectVersion = projectVersion,
            archiveBaseName = archiveBaseName,
        )

        val debugSymbolsComponent: AdhocComponentWithVariants = softwareComponentFactory.adhoc(componentName)
        val releaseWasmFilesConfiguration = createConfigurationWithArchiveArtifact(
            configurationName = "wasmSqliteCommonDebugElements",
            description = "Wasm binaries with debug symbols",
            archiveForPublication = packZipForPublicationTask.flatMap { it.archiveFile },
            isDebuggable = true,
        ).get()
        debugSymbolsComponent.addVariantsFromConfiguration(releaseWasmFilesConfiguration) {
            mapToMavenScope("runtime")
        }
        return debugSymbolsComponent
    }

    @Suppress("LongParameterList")
    private fun setupPackageResourcesTask(
        targetName: String,
        wasmFiles: FileCollection,
        projectName: String,
        projectVersion: Provider<String>,
        archiveBaseName: Provider<String>,
        resourcePackage: String = getResourcePackage(projectName),
    ): Provider<Zip> {
        val zipForPublicationDir = wasmPaths.rootForTarget(targetName).map { it.dir("zip-for-publication") }
        val subdirInsideZip = "wsohResources/$resourcePackage/"

        return tasks.register<Zip>("package${targetName.capitalizeAscii()}Resources") {
            this.archiveBaseName.set(archiveBaseName)
            archiveVersion.set(projectVersion)
            archiveClassifier.set("kotlin_resources")
            archiveExtension.set("zip")

            this.destinationDirectory.set(zipForPublicationDir)

            from(wasmFiles) {
                include("*.wasm")
            }
            into(subdirInsideZip)

            isReproducibleFileOrder = true
            isPreserveFileTimestamps = false
        }
    }

    private fun getResourcePackage(
        projectName: String,
    ): String {
        return projectName.lowercase().replace("-", "_")
    }

    private fun createConfigurationWithArchiveArtifact(
        target: KotlinTarget?,
        archiveForPublication: Provider<RegularFile>,
        isDebuggable: Boolean = false,
    ): ConsumableConfiguration {
        val targetName = target?.targetName?.capitalizeAscii() ?: "Common"
        return createConfigurationWithArchiveArtifact(
            configurationName = "wasmSqliteReleasePacked${targetName}Elements",
            description = "Wasm binaries published as Kotlin Multiplatform Resources for $targetName",
            archiveForPublication = archiveForPublication,
            isDebuggable = isDebuggable,
        ).get()
    }

    private fun createConfigurationWithArchiveArtifact(
        configurationName: String,
        description: String,
        archiveForPublication: Provider<RegularFile>,
        isDebuggable: Boolean = false,
    ): Provider<ConsumableConfiguration> = configurations.consumable(configurationName) {
        this.description = description
        attributes {
            addMultiplatformNativeResourcesAttributes(objects, null)
            attribute(CppBinary.DEBUGGABLE_ATTRIBUTE, isDebuggable)
        }
        outgoing {
            artifact(archiveForPublication) {
                extension = "zip"
                classifier = "kotlin_resources"
            }
        }
    }

    class WasmLibraryResourcesPaths(
        layout: ProjectLayout,
    ) {
        val root: Provider<Directory> = layout.buildDirectory.dir("wasmLibraries")
        val jvmRoot: Provider<Directory> = root.map { it.dir("jvm") }

        fun rootForTarget(targetName: String): Provider<Directory> = root.map { it.dir(targetName) }
    }

    abstract class CopyAssetsTask @Inject constructor(
        private val fileOperations: FileOperations,
    ) : DefaultTask() {
        @get:InputFiles
        abstract val wasmFiles: ConfigurableFileCollection

        @get:Input
        abstract val subdirectoryInAssets: Property<String>

        @get:OutputDirectory
        abstract val outputDirectory: DirectoryProperty

        @TaskAction
        fun copy() {
            val dir = outputDirectory.get().dir(subdirectoryInAssets.get())
            fileOperations.sync {
                from(wasmFiles)
                into(dir)
                include("*.wasm")
            }
        }
    }
}
