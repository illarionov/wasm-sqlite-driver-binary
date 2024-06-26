/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish

import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.attributes.Bundling.BUNDLING_ATTRIBUTE
import org.gradle.api.attributes.Bundling.EXTERNAL
import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Category.LIBRARY
import org.gradle.api.attributes.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.attributes.java.TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.Companion.konanTargetAttribute
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class WasmResourcesLinuxX64 @Inject constructor(
    private val tasks: TaskContainer,
    private val configurations: ConfigurationContainer,
    private val objects: ObjectFactory,
    private val project: Project,
    wasmResourcesRootDir: Provider<Directory>,
) {
    val root: Provider<Directory> = wasmResourcesRootDir.map { it.dir("linuxX64") }
    val zipForPublicationDir = root.map { it.dir("zip-for-publication") }

    fun setupResourcesPublication() {
        val packZipForPublicationTask = setupPackResourcesTask()
        setupPublication(
            archiveForPublication = packZipForPublicationTask.flatMap { it.archiveFile },
        )
    }

    private fun setupPackResourcesTask(): Provider<Zip> {
        val unpackedSubdir = "wsohResources/sqlite_android_wasm_emscripten_icu_346/"
        val wasmFiles = configurations.named("wasmSqliteReleaseElements").get().artifacts.files
        return tasks.register<Zip>("packageLinuxX64Resources") {
            // TODO: remove seconds kotlin_resources
            archiveFileName.set(
                "sqlite-android-wasm-emscripten-icu-346-0.1-alpha06-SNAPSHOT.kotlin_resources.kotlin_resources.zip",
            )
            destinationDirectory.set(zipForPublicationDir)

            from(wasmFiles) {
                include("*.wasm")
            }
            into(unpackedSubdir)

            isReproducibleFileOrder = true
            isPreserveFileTimestamps = false
        }
    }

    private fun setupPublication(
        archiveForPublication: Provider<RegularFile>,
    ) {
        val publishedConfiguration = configurations.consumable("wasmSqliteReleasePackedLinuxX64Elements") {
            description = "Wasm binaries published as Kotlin Multiplatform Resources"
            attributes {
                attribute(CATEGORY_ATTRIBUTE, objects.named(LIBRARY))
                attribute(BUNDLING_ATTRIBUTE, objects.named(EXTERNAL))
                attribute(TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named("non-jvm"))
                attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named("kotlin-multiplatformresources"))
                attribute(USAGE_ATTRIBUTE, objects.named("kotlin-multiplatformresources"))

                attribute(konanTargetAttribute, "linux_x64")
                attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
            }
            outgoing {
                artifact(archiveForPublication)
            }
        }.get()

        project.extensions.getByName<KotlinMultiplatformExtension>("kotlin").linuxX64 {
            addVariantsFromConfigurationsToPublication(publishedConfiguration) {
                mapToMavenScope("runtime")
            }
        }
    }
}
