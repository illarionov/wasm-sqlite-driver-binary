/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.wasm2class

import at.released.wasm2class.Wasm2Class.Deps
import at.released.wasm2class.Wasm2ClassTask.GenerateChicoryMachineClasses.Companion.registerWasm2ClassTask
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the

internal fun Project.setupJavaPluginIntegration() {
    val wasm2ClassExtension: Wasm2ClassExtension = the()
    wasm2ClassExtension.outputDirectory.convention(layout.buildDirectory.dir("generated-chicory-aot/java"))

    val wasm2classTask = registerWasm2ClassTask()

    extensions.configure<JavaPluginExtension>("java") {
        sourceSets.named("jvmMain") {
            java.srcDir(wasm2classTask.flatMap(Wasm2ClassTask::outputSources))
            resources.srcDir(wasm2classTask.flatMap(Wasm2ClassTask::outputResources))
            resources.srcDir(wasm2classTask.flatMap(Wasm2ClassTask::outputClasses))
        }
    }

    val outputClasses = wasm2classTask.flatMap(Wasm2ClassTask::outputClasses)
    dependencies {
        add("compileOnly", files(outputClasses))
        add("api", Deps.CHICORY_RUNTIME)
    }
}
