/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage")

package at.released.wasm.sqlite.binary.gradle.multiplatform

import at.released.wasm.sqlite.binary.gradle.multiplatform.localsnapshot.DistributionAggregationConfigurations

private val wasmVersions = createWasmSqliteVersionsExtension()
private val downloadableReleaseDirName: Provider<String> = wasmVersions.rootVersion.map {
    "maven-wasm-sqlite-binary-$it"
}
private val distributionDir: Provider<Directory> = layout.buildDirectory.dir("distribution")
private val aggregateConfigurations = DistributionAggregationConfigurations(objects, configurations)

@Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
val packDistributionTask: TaskProvider<Zip> = tasks.register<Zip>("packageMavenDistribution") {
    archiveBaseName = "maven-wasm-sqlite-binary"
    archiveVersion = wasmVersions.rootVersion
    destinationDirectory = layout.buildDirectory.dir("tmp/zip")

    from(aggregateConfigurations.mavenSnapshotAggregationFiles.get().asFileTree)
    into(downloadableReleaseDirName)

    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}

tasks.register<Copy>("foldDistribution") {
    from(packDistributionTask.flatMap { it.archiveFile })
    from(aggregateConfigurations.wasmArchiveFiles.get().asFileTree)
    into(distributionDir)
}
