/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.wasm2class

import at.released.wasm2class.Wasm2ClassTask.GenerateChicoryMachineClasses.Companion.registerWasm2ClassTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

internal fun Project.setupKotlinMultiplatformPluginIntegration() {
    val wasm2ClassExtension: Wasm2ClassExtension = the()
    wasm2ClassExtension.outputDirectory.convention(layout.buildDirectory.dir("generated-chicory-aot/multiplatform"))

    val wasm2classTask = registerWasm2ClassTask()

    extensions.configure<KotlinMultiplatformExtension> {
        targets.withType<KotlinJvmTarget> {
            compilations.named(MAIN_COMPILATION_NAME).configure {
                compileJavaTaskProvider?.configure {
                    source(wasm2classTask.flatMap(Wasm2ClassTask::outputSources))
                }

                val outputClasses = wasm2classTask.flatMap(Wasm2ClassTask::outputClasses)
                defaultSourceSet {
                    resources.srcDir(wasm2classTask.flatMap(Wasm2ClassTask::outputResources))
                    resources.srcDir(outputClasses)
                    dependencies {
                        api(Wasm2Class.Deps.CHICORY_RUNTIME)
                        compileOnly(files(outputClasses))
                    }
                }
            }
        }
    }
}
