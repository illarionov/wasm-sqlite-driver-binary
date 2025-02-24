/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.gradle.chicory.aot

import com.dylibso.chicory.experimental.aot.AotCompiler
import com.dylibso.chicory.experimental.aot.CompilerResult
import com.dylibso.chicory.wasm.Parser
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NAME_ONLY
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import javax.inject.Inject
import kotlin.io.path.createDirectories
import kotlin.io.path.writeBytes

@CacheableTask
open class WasmAotGeneratorTask @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout,
    configurations: ConfigurationContainer,
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {
    /**
     * The WASM binary file to be compiled.
     */
    @get:InputFile
    @get:PathSensitive(NAME_ONLY)
    val wasmBinary: RegularFileProperty = objects.fileProperty()

    @get:Internal
    val outputRootDirectory: DirectoryProperty = objects.directoryProperty().convention(
        layout.buildDirectory.dir("generated-chicory-aot"),
    )

    /**
     * The output directory for the generated Java class files.
     */
    @get:OutputDirectory
    val outputClasses: DirectoryProperty = objects.directoryProperty().convention(
        outputRootDirectory.map { it.dir("classes") },
    )

    /**
     *  The output directory for the generated Java source files.
     */
    @get:OutputDirectory
    val outputSources: DirectoryProperty = objects.directoryProperty().convention(
        outputRootDirectory.map { it.dir("sources") },
    )

    /**
     *  The output directory for the generated resources.
     */
    @get:OutputDirectory
    val outputResources: DirectoryProperty = objects.directoryProperty().convention(
        outputRootDirectory.map { it.dir("resources") },
    )

    /**
     * The root package for the generated classes.
     */
    @get:Input
    val rootPackage: Property<String> = objects.property()

    @Internal
    val moduleClassBaseName: Property<String> = objects.property<String>().convention(
        wasmBinary.map { it.asFile.name.substringBefore(".").toUpperCamelCase() },
    )

    /**
     * The name of the Module class.
     */
    @get:Input
    val moduleClassSimpleName: Property<String> = objects.property<String>().convention(
        moduleClassBaseName.map { "${it}Module".toUpperCamelCase() },
    )

    /**
     * The name of the Machine class.
     */
    @get:Input
    val machineClassSimpleName: Property<String> = objects.property<String>().convention(
        moduleClassBaseName.map { "${it}Machine".toUpperCamelCase() },
    )

    /**
     * The name of the generated stripped WASM resource.
     */
    @get:Input
    val wasmMetaResourceName: Property<String> = objects.property<String>().convention(
        moduleClassBaseName.map { "${it.lowercase()}.meta" },
    )

    /**
     * The classpath with Chicory AOT.
     */
    @Suppress("UnstableApiUsage")
    @InputFiles
    @get:Classpath
    val chicoryClasspath: ConfigurableFileCollection = objects.fileCollection().convention(
        configurations.named("chicoryAotRuntimeClasspath"),
    )

    @TaskAction
    fun execute() {
        val workQueue: WorkQueue = workerExecutor.classLoaderIsolation {
            classpath.from(chicoryClasspath)
        }

        val task = this
        workQueue.submit(GenerateWasmClasses::class.java) {
            wasmBinary.set(task.wasmBinary)
            outputClasses.set(task.outputClasses)
            outputSources.set(task.outputSources)
            outputResources.set(task.outputResources)
            rootPackage.set(task.rootPackage)
            moduleClassSimpleName.set(task.moduleClassSimpleName)
            machineClassSimpleName.set(task.machineClassSimpleName)
            wasmMetaResourceName.set(task.wasmMetaResourceName)
        }
    }

    interface WasmAotWorkParameters : WorkParameters {
        val wasmBinary: RegularFileProperty
        val outputClasses: DirectoryProperty
        val outputSources: DirectoryProperty
        val outputResources: DirectoryProperty
        val rootPackage: Property<String>
        val moduleClassSimpleName: Property<String>
        val machineClassSimpleName: Property<String>
        val wasmMetaResourceName: Property<String>
    }

    public abstract class GenerateWasmClasses : WorkAction<WasmAotWorkParameters> {
        override fun execute() {
            val rootPackage = parameters.rootPackage.get()
            parameters.outputSources.asFile.get().toPath().let { outputSources ->
                outputSources.cleanup()
                writeModuleClass(
                    rootPackage = rootPackage,
                    moduleClassSimpleName = parameters.moduleClassSimpleName.get(),
                    machineClassSimpleName = parameters.machineClassSimpleName.get(),
                    wasmMetaSimpleName = parameters.wasmMetaResourceName.get(),
                    destination = outputSources,
                )
            }

            val binaryBytes = parameters.wasmBinary.asFile.get().readBytes()
            val module = Parser.parse(binaryBytes)

            parameters.outputClasses.asFile.get().toPath().let { outputClasses ->
                outputClasses.cleanup()
                val machineClassFqn = "$rootPackage.${parameters.machineClassSimpleName.get()}"
                AotCompiler.compileModule(module, machineClassFqn).writeClasses(outputClasses)
            }

            parameters.outputResources.asFile.get().toPath().let { outputResources ->
                val rewrittenWasm = generateWasmMeta(binaryBytes, module)
                outputResources.resolve(rootPackage.packageNameToPath()).let {
                    val fullPath = it.cleanup()
                    fullPath.resolve(parameters.wasmMetaResourceName.get()).writeBytes(rewrittenWasm)
                }
            }
        }

        private companion object {
            private fun String.packageNameToPath(): String = this.replace(".", "/")

            private fun CompilerResult.writeClasses(destination: Path) = classBytes()
                .mapKeys { it.key.packageNameToPath() + ".class" }
                .forEach { (binaryPath, payload) ->
                    try {
                        destination.resolve(binaryPath).run {
                            parent.createDirectories()
                            writeBytes(payload)
                        }
                    } catch (ioe: IOException) {
                        throw IOException("Failed to write class file `$binaryPath`", ioe)
                    }
                }

            private fun Path.cleanup(): Path {
                val fullPath = createDirectories()
                Files.walkFileTree(
                    fullPath,
                    object : SimpleFileVisitor<Path>() {
                        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                            Files.delete(file)
                            return FileVisitResult.CONTINUE
                        }

                        override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                            if (this@cleanup != dir) {
                                Files.delete(dir)
                                return FileVisitResult.CONTINUE
                            } else {
                                return FileVisitResult.TERMINATE
                            }
                        }
                    },
                )
                return fullPath
            }
        }
    }
}
