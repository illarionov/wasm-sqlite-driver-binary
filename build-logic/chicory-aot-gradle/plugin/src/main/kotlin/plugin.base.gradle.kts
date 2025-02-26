/*
 * Copyright 2024-2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.wasm2class

plugins {
    id("base")
}

extensions.create<Wasm2ClassExtension>(Wasm2Class.WASM_2_CLASS_EXTENSION_NAME)

private val chicoryAotDependencies = configurations.create(Wasm2Class.Configurations.CHICORY_AOT_COMPILER) {
    isCanBeResolved = false
    isCanBeConsumed = false
    isVisible = false
    description = "The classpath for the Chicory AOT compiler"
    defaultDependencies {
        listOf(
            Wasm2Class.Deps.CHICORY_AOT,
            Wasm2Class.Deps.CHICORY_WASM,
            Wasm2Class.Deps.CHICORY_RUNTIME,
            Wasm2Class.Deps.JAVAPARSER,
        ).forEach {
            add(project.dependencies.create(it))
        }
    }
}

configurations.create(Wasm2Class.Configurations.CHICORY_AOT_COMPILER_RUNTIME_CLASSPATH) {
    isCanBeResolved = true
    isCanBeConsumed = false
    extendsFrom(chicoryAotDependencies)
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}
