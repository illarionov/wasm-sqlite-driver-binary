/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish

import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import javax.inject.Inject

internal open class WasmPublishedResourcesConfigurator @Inject constructor(
    layout: ProjectLayout,
    private val tasks: TaskContainer,
    private val objects: ObjectFactory,
    private val configurations: ConfigurationContainer,
) {
    private val wasmPaths = WasmLibraryResourcesPaths(layout)

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

    fun setupLinuxX64Resources(
        linuxTarget: KotlinTarget,
        wasmFiles: FileCollection,
        projectName: String,
        projectVersion: Provider<String>,
    ) {
        val zipForPublicationDir = wasmPaths.linuxX64Root.map { it.dir("zip-for-publication") }
        val packZipForPublicationTask = setupPackageLinuxResourcesTask(
            zipForPublicationDir = zipForPublicationDir,
            wasmFiles = wasmFiles,
            projectName = projectName,
            projectVersion = projectVersion,
        )
        setupLinuxX64Publication(
            linuxTarget = linuxTarget,
            archiveForPublication = packZipForPublicationTask.flatMap { it.archiveFile },
        )
    }

    private fun setupPackageLinuxResourcesTask(
        zipForPublicationDir: Provider<Directory>,
        wasmFiles: FileCollection,
        projectName: String,
        projectVersion: Provider<String>,
    ): Provider<Zip> {
        val unpackedSubdir = "wsohResources/${projectName.replace("-", "_")}/"
        return tasks.register<Zip>("packageLinuxX64Resources") {
            archiveBaseName.set(projectName)
            archiveVersion.set(projectVersion)
            archiveClassifier.set("kotlin_resources")
            archiveExtension.set("zip")

            this.destinationDirectory.set(zipForPublicationDir)

            from(wasmFiles) {
                include("*.wasm")
            }
            into(unpackedSubdir)

            isReproducibleFileOrder = true
            isPreserveFileTimestamps = false
        }
    }

    @Suppress("UnstableApiUsage")
    private fun setupLinuxX64Publication(
        linuxTarget: KotlinTarget,
        archiveForPublication: Provider<RegularFile>,
    ) {
        val publishedConfiguration = configurations.consumable("wasmSqliteReleasePackedLinuxX64Elements") {
            description = "Wasm binaries published as Kotlin Multiplatform Resources"
            attributes {
                addLinuxX64MultiplatformResourcesAttributes(objects)
            }
            outgoing {
                artifact(archiveForPublication)
            }
        }.get()

        linuxTarget.addVariantsFromConfigurationsToPublication(publishedConfiguration) {
            mapToMavenScope("runtime")
        }
    }

    class WasmLibraryResourcesPaths(
        layout: ProjectLayout,
    ) {
        val root: Provider<Directory> = layout.buildDirectory.dir("wasmLibraries")
        val jvmRoot: Provider<Directory> = root.map { it.dir("jvm") }
        val linuxX64Root: Provider<Directory> = root.map { it.dir("linuxX64") }
    }
}
