/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.wasm2class

import at.released.wasm2class.Wasm2Class.Configurations.CHICORY_AOT_COMPILER_RUNTIME_CLASSPATH
import at.released.wasm2class.Wasm2Class.Deps
import at.released.wasm2class.Wasm2Class.Deps.CHICORY_GROUP
import at.released.wasm2class.Wasm2ClassTask.GenerateChicoryMachineClasses.Companion.registerWasm2ClassTask
import com.android.build.api.AndroidPluginVersion
import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts.Scope.PROJECT
import com.android.build.api.variant.Variant
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register

internal fun Project.setupAndroidPluginIntegration() {
    val componentsExtension = extensions.findByType(AndroidComponentsExtension::class.java)
    checkNotNull(componentsExtension) {
        "Could not find the Android Gradle Plugin (AGP) extension"
    }
    @Suppress("MagicNumber")
    check(componentsExtension.pluginVersion >= AndroidPluginVersion(7, 3)) {
        "Wasm2class Gradle plugin is only compatible with Android Gradle plugin (AGP) " +
                "version 7.3.0 or higher (found ${componentsExtension.pluginVersion})."
    }

    val wasm2ClassExtension = extensions.getByType(Wasm2ClassExtension::class.java)
    wasm2ClassExtension.outputDirectory.convention(layout.buildDirectory.dir("generated-chicory-aot/android"))
    wasm2ClassExtension.targetPackage.convention(extensions.getByType(CommonExtension::class.java).namespace)

    val aotCompileClasspath = configurations.named(CHICORY_AOT_COMPILER_RUNTIME_CLASSPATH).get()
        .incoming
        .artifactView {
            componentFilter {
                it is ModuleComponentIdentifier &&
                        it.group == CHICORY_GROUP &&
                        (it.module == "runtime" || it.module == "wasm")
            }
        }
        .files

    componentsExtension.onVariants { variant: Variant ->
        val wasm2ClassTask: TaskProvider<Wasm2ClassTask> = project.registerWasm2ClassTask(
            name = "${variant.name}PrecompileWasm2class",
            wasm2ClassExtension = wasm2ClassExtension,
            outputSources = wasm2ClassExtension.outputDirectory.map { it.dir("${variant.name}Sources") },
        )

        @Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
        val compileWasmJavaClassesTask = tasks.register<JavaCompile>("${variant.name}CompileAotModuleWithJavac") {
            source(wasm2ClassTask.flatMap(Wasm2ClassTask::outputSources))
            classpath = files(
                aotCompileClasspath,
                wasm2ClassTask.flatMap(Wasm2ClassTask::outputClasses),
            )
            dependsOn(wasm2ClassTask)
        }

        variant.sources.resources?.addGeneratedSourceDirectory(wasm2ClassTask, Wasm2ClassTask::outputResources)
        variant.artifacts.forScope(PROJECT).apply {
            use(wasm2ClassTask).toAppend(ScopedArtifact.CLASSES, Wasm2ClassTask::outputClasses)
            use(compileWasmJavaClassesTask).toAppend(ScopedArtifact.CLASSES, JavaCompile::getDestinationDirectory)
        }
    }

    dependencies {
        add("api", Deps.CHICORY_RUNTIME)
    }
}
