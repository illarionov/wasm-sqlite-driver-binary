/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage")

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform

import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.register
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish.CleanupDownloadableReleaseDirectoryTask
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish.DownloadableDistributionPaths
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish.createWasmSqliteVersionsExtension

private val wasmVersions = createWasmSqliteVersionsExtension()

val wasmArchiveAggregation = configurations.dependencyScope("wasmArchiveAggregation")
val wasmArchiveFiles = configurations.resolvable("wasmArchiveAggregationFiles") {
    extendsFrom(wasmArchiveAggregation.get())
    attributes {
        attribute(USAGE_ATTRIBUTE, objects.named("wasm-runtime"))
        attribute(CATEGORY_ATTRIBUTE, objects.named("emscripten-release-archive"))
    }
}

val localMavenPaths = DownloadableDistributionPaths(
    rootProject,
    provider { wasmVersions.rootVersion.get() }, // cache configuration errors if use rootVersion directly
)
val zipTmpDistributionDir = rootProject.layout.buildDirectory.dir("tmp/zip")

@Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
val cleanupDownloadableReleaseRootTask = tasks.register<CleanupDownloadableReleaseDirectoryTask>(
    "cleanupDownloadableRelease",
) {
    inputDirectory.set(localMavenPaths.downloadableReleaseRoot)
}

val prepareDownloadableReleaseTask = tasks.register("publishAllPublicationsToDownloadableReleaseRepository")

@Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
val packDistributionTask: TaskProvider<Zip> = tasks.register<Zip>("packageMavenDistribution") {
    archiveBaseName = "maven-wasm-sqlite-binary"
    destinationDirectory = zipTmpDistributionDir

    from(localMavenPaths.downloadableReleaseRoot)
    into(localMavenPaths.downloadableReleaseDirName)

    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
    dependsOn(prepareDownloadableReleaseTask)
}

tasks.register<Copy>("foldDistribution") {
    from(packDistributionTask.flatMap { it.archiveFile })
    from(wasmArchiveFiles.get().asFileTree)
    into(localMavenPaths.distributionDir)
}
