/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.wasm2class

import at.released.wasm2class.Wasm2Class.Configurations.CHICORY_AOT_COMPILER_RUNTIME_CLASSPATH
import at.released.wasm2class.Wasm2ClassTask.GenerateChicoryMachineClasses.WasmAotWorkParameters
import at.released.wasm2class.generator.WasmModuleClassGenerator
import at.released.wasm2class.generator.generateWasmMeta
import com.dylibso.chicory.experimental.aot.AotCompiler
import com.dylibso.chicory.experimental.aot.CompilerResult
import com.dylibso.chicory.wasm.Parser
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
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
abstract class Wasm2ClassTask @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {
    /**
     * The WASM binaries to be compiled.
     */
    @get:Nested
    abstract val modules: ListProperty<Wasm2ClassMachineModuleSpec>

    /**
     * The output directory for the generated Java .class files.
     */
    @get:OutputDirectory
    abstract val outputClasses: DirectoryProperty

    /**
     *  The output directory for the generated Java source files.
     */
    @get:OutputDirectory
    abstract val outputSources: DirectoryProperty

    /**
     *  The output directory for the generated resources.
     */
    @get:OutputDirectory
    abstract val outputResources: DirectoryProperty

    /**
     * The classpath with Chicory AOT.
     */
    @get:InputFiles
    @get:Classpath
    abstract val chicoryClasspath: ConfigurableFileCollection

    @TaskAction
    fun execute() {
        val workQueue: WorkQueue = workerExecutor.classLoaderIsolation {
            classpath.from(chicoryClasspath)
        }
        modules.get().forEach { binarySpec: Wasm2ClassMachineModuleSpec ->
            workQueue.submit(GenerateChicoryMachineClasses::class.java) { setFrom(binarySpec) }
        }
    }

    private fun WasmAotWorkParameters.setFrom(spec: Wasm2ClassMachineModuleSpec) {
        wasm.set(spec.wasm)
        outputClasses.set(this@Wasm2ClassTask.outputClasses)
        outputSources.set(this@Wasm2ClassTask.outputSources)
        outputResources.set(this@Wasm2ClassTask.outputResources)
        targetPackage.set(spec.targetPackage)
        moduleClassSimpleName.set(spec.moduleClassSimpleName)
        machineClassSimpleName.set(spec.machineClassSimpleName)
        wasmMetaResourceName.set(spec.wasmMetaResourceName)
    }

    abstract class GenerateChicoryMachineClasses : WorkAction<WasmAotWorkParameters> {
        override fun execute() {
            val targetPackage = parameters.targetPackage.get()
            parameters.outputSources.asFile.get().toPath().let { outputSources ->
                outputSources.createEmpty()
                WasmModuleClassGenerator(
                    targetPackage = targetPackage,
                    moduleClassSimpleName = parameters.moduleClassSimpleName.get(),
                    machineClassSimpleName = parameters.machineClassSimpleName.get(),
                    wasmMetaSimpleName = parameters.wasmMetaResourceName.get(),
                ).writeTo(outputSources)
            }

            val wasmBytes = parameters.wasm.asFile.get().readBytes()
            val module = Parser.parse(wasmBytes)

            parameters.outputClasses.asFile.get().toPath().let { outputClasses ->
                outputClasses.createEmpty()
                val machineClassFqn = "$targetPackage.${parameters.machineClassSimpleName.get()}"
                AotCompiler.compileModule(module, machineClassFqn).writeClasses(outputClasses)
            }

            parameters.outputResources.asFile.get().toPath().let { outputResources ->
                val rewrittenWasm = generateWasmMeta(wasmBytes, module)
                outputResources.resolve(targetPackage.packageNameToPath()).let {
                    val fullPath = it.createEmpty()
                    fullPath.resolve(parameters.wasmMetaResourceName.get()).writeBytes(rewrittenWasm)
                }
            }
        }

        interface WasmAotWorkParameters : WorkParameters {
            val wasm: RegularFileProperty
            val outputClasses: DirectoryProperty
            val outputSources: DirectoryProperty
            val outputResources: DirectoryProperty
            val targetPackage: Property<String>
            val moduleClassSimpleName: Property<String>
            val machineClassSimpleName: Property<String>
            val wasmMetaResourceName: Property<String>
        }

        companion object {
            internal fun Project.registerWasm2ClassTask(
                name: String = "precompileWasm2class",
                wasm2ClassExtension: Wasm2ClassExtension = the<Wasm2ClassExtension>(),
                outputSources: Provider<Directory> = wasm2ClassExtension.outputSources,
            ): TaskProvider<Wasm2ClassTask> = tasks.register<Wasm2ClassTask>(name) {
                this.modules.set(wasm2ClassExtension.modules)
                this.outputClasses.set(wasm2ClassExtension.outputClasses)
                this.outputSources.set(outputSources)
                this.outputResources.set(wasm2ClassExtension.outputResources)
                this.chicoryClasspath.from(configurations.named(CHICORY_AOT_COMPILER_RUNTIME_CLASSPATH))
            }

            private fun CompilerResult.writeClasses(destination: Path) {
                classBytes()
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
            }

            private fun String.packageNameToPath(): String = this.replace(".", "/")

            private fun Path.createEmpty(): Path {
                val fullPath = createDirectories()
                Files.walkFileTree(
                    fullPath,
                    object : SimpleFileVisitor<Path>() {
                        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                            Files.delete(file)
                            return FileVisitResult.CONTINUE
                        }

                        override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                            if (this@createEmpty != dir) {
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
