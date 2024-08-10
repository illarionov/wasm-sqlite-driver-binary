/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("SpacingBetweenPackageAndImports")

package ru.pixnews.wasm.sqlite.binary.gradle.buildinfo

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/*
 * A plugin that creates Kotlin classes containing Wasm SQLite build details and the compiler settings used.
 */
private val sqliteConfigurationExtension = extensions.create(
    "sqliteConfigGenerator",
    SqliteConfigGeneratorExtension::class.java,
)

pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        val outputDirectories = SqliteConfigurationOutputDirectories(layout)
        sqliteConfigurationExtension.configurations.configureEach {
            configureGenerationTask(
                multiplatformExtension = this@configure,
                outputDirectories = outputDirectories,
                configuration = this,
            )
        }
    }
}

private fun configureGenerationTask(
    multiplatformExtension: KotlinMultiplatformExtension,
    outputDirectories: SqliteConfigurationOutputDirectories,
    configuration: WasmSqliteBuildInfo,
) {
    @Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
    val generateBuildInfoTask = tasks.register<GenerateBuildInfoTask>(
        "generateSqlite${configuration.name}Configuration",
    ) {
        this.configuration = configuration
        this.dstDirectory = outputDirectories.root
    }

    val codeSourceSets = mutableMapOf(
        "androidMain" to GenerateBuildInfoTask::androidOutputDirectory,
        "commonMain" to GenerateBuildInfoTask::commonOutputDirectory,
        "jsMain" to GenerateBuildInfoTask::jsOutputDirectory,
        "jvmMain" to GenerateBuildInfoTask::jvmOutputDirectory,
        "nativeMain" to GenerateBuildInfoTask::nativeOutputDirectory,
    )

    multiplatformExtension.sourceSets.configureEach {
        val directoryForSourceSet = codeSourceSets.remove(this.name)
        if (directoryForSourceSet != null) {
            kotlin.srcDir(
                generateBuildInfoTask.flatMap(directoryForSourceSet),
            )
        }
    }
}

class SqliteConfigurationOutputDirectories(
    layout: ProjectLayout,
) {
    val root: Provider<Directory> = layout.buildDirectory.dir("generated/sqliteConfig")
}
