/*
 * Copyright 2024-2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage")

package at.released.gradle.chicory.aot

plugins {
    id("base")
}

private val libs = versionCatalogs.named("libs")

configurations {
    val chicoryAotDependencies = dependencyScope("chicoryAot") {
        description = "The classpath for the Chicory AOT compiler"
        isVisible = false
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        }
        defaultDependencies {
            listOf("chicory.aot", "chicory.wasm", "chicory.runtime", "javaparser").forEach {
                add(libs.findLibrary(it).get().get())
            }
        }
    }
    resolvable("chicoryAotRuntimeClasspath") {
        extendsFrom(chicoryAotDependencies.get())
    }
}
