/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.wasm.sqlite.binary.gradle.multiplatform.localsnapshot

import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ConsumableConfiguration
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.named

@Suppress("UnstableApiUsage")
class DistributionAggregationConfigurations(
    objects: ObjectFactory,
    configurations: ConfigurationContainer,
) {
    val wasmArchiveAggregation = configurations.dependencyScope("wasmArchiveAggregation")
    val wasmArchiveFiles = configurations.resolvable("wasmArchiveAggregationFiles") {
        extendsFrom(wasmArchiveAggregation.get())
        attributes {
            attribute(USAGE_ATTRIBUTE, objects.named("wasm-runtime"))
            attribute(CATEGORY_ATTRIBUTE, objects.named("emscripten-release-archive"))
        }
    }
    val mavenSnapshotAggregation = configurations.dependencyScope("mavenSnapshotAggregation")
    val mavenSnapshotAggregationFiles = configurations.resolvable("mavenSnapshotAggregationFiles") {
        extendsFrom(mavenSnapshotAggregation.get())
        attributes {
            setupMavenSnapshotAggregationAttributes(objects)
        }
    }

    companion object {
        fun Project.createMavenSnapshotReleaseElements(): NamedDomainObjectProvider<ConsumableConfiguration> =
            configurations.consumable("mavenSnapshotReleaseElements") {
                attributes {
                    setupMavenSnapshotAggregationAttributes(objects)
                }
            }

        fun AttributeContainer.setupMavenSnapshotAggregationAttributes(objects: ObjectFactory) {
            attribute(USAGE_ATTRIBUTE, objects.named("wasm-runtime"))
            attribute(CATEGORY_ATTRIBUTE, objects.named("local-maven-snapshot"))
        }
    }
}
