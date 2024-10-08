/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("BLANK_LINE_BETWEEN_PROPERTIES", "GENERIC_VARIABLE_WRONG_DECLARATION")

package ru.pixnews.wasm.builder.sqlite

import org.gradle.api.Named
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.register
import ru.pixnews.wasm.builder.base.WasmBuildDsl
import ru.pixnews.wasm.builder.base.ext.toUpperCamelCase
import ru.pixnews.wasm.builder.emscripten.EmscriptenBuildTask
import ru.pixnews.wasm.builder.emscripten.WasmStripTask
import ru.pixnews.wasm.builder.sqlite.FilePrefixMapEntry.Companion.createFilePrefixMapEntry
import ru.pixnews.wasm.builder.sqlite.preset.SqliteCodeGenerationFlags
import ru.pixnews.wasm.builder.sqlite.preset.config.OpenHelperConfig
import java.io.Serializable
import javax.inject.Inject

@WasmBuildDsl
public open class SqliteWasmBuildSpec @Inject internal constructor(
    objects: ObjectFactory,
    providers: ProviderFactory,
    projectLayout: ProjectLayout,
    tasks: TaskContainer,
    private val name: String,
) : Named, Serializable {
    private val sqliteWasmFilesSrdDir = projectLayout.projectDirectory.dir("../sqlite-android-common/sqlite")

    public val sqliteVersion: Property<String> = objects.property(String::class.java)
        .convention("3450100")

    public val sqlite3Source: ConfigurableFileCollection = objects.fileCollection()

    public val additionalSourceFiles: ConfigurableFileCollection = objects.fileCollection().apply {
        this.from(sqliteWasmFilesSrdDir.file("wasm/api/sqlite3-wasm.c"))
    }

    public val additionalIncludes: ConfigurableFileCollection = objects.fileCollection().apply {
        this.from(sqliteWasmFilesSrdDir.dir("wasm/api"))
    }

    public val additionalLibs: ConfigurableFileCollection = objects.fileCollection()

    public val wasmBaseFileName: Property<String> = objects.property(String::class.java)
        .convention("sqlite")

    public val wasmDebugFileName: Property<String> = objects.property(String::class.java)
        .convention(
            providers.provider {
                "${wasmBaseFileName.get()}-$name-${sqliteVersion.get()}-debug.wasm"
            },
        )

    public val wasmFileName: Property<String> = objects.property(String::class.java)
        .convention(
            providers.provider {
                "${wasmBaseFileName.get()}-$name-${sqliteVersion.get()}.wasm"
            },
        )

    public val codeGenerationFlags: ListProperty<String> = objects.listProperty(String::class.java)
        .convention(SqliteCodeGenerationFlags.codeGenerationFlagsMultithread)

    public val codeOptimizationFlags: ListProperty<String> = objects.listProperty(String::class.java)
        .convention(SqliteCodeGenerationFlags.codeOptimizationFlagsO2)

    public val emscriptenFlags: ListProperty<String> = objects.listProperty(String::class.java)
        .convention(SqliteCodeGenerationFlags.emscriptenFlagsMultithread)

    public val exportedFunctions: ListProperty<String> = objects.listProperty(String::class.java)
        .convention(SqliteExportedFunctions.openHelperExportedFunctions)

    public val sqliteFlags: ListProperty<String> = objects.listProperty(String::class.java)
        .convention(OpenHelperConfig.getBuildFlags())

    @Nested
    public val filePrefixMap: ListProperty<FilePrefixMapEntry> = objects.listProperty<FilePrefixMapEntry>().apply {
        add(
            objects.createFilePrefixMapEntry(
                newPath = "/sqlite-android-common/sqlite",
                oldPath = sqliteWasmFilesSrdDir.asFile.canonicalPath,
            ),
        )
    }

    public val buildTaskName: String = "compileSqlite${name.toUpperCamelCase()}"

    public val buildTask: TaskProvider<EmscriptenBuildTask> = tasks.register<EmscriptenBuildTask>(buildTaskName)

    public val stripTaskName: String = "stripSqlite${name.toUpperCamelCase()}"

    public val stripTask: TaskProvider<WasmStripTask> = tasks.register<WasmStripTask>(stripTaskName)

    public val debugWasmOutput: Provider<RegularFile> = buildTask.flatMap {
        it.outputDirectory.file(wasmDebugFileName.get())
    }

    public val strippedWasmOutput: Provider<RegularFile> = stripTask.flatMap(WasmStripTask::destination)

    public val packEmscriptenOutputTaskName: String = "packEmscriptenOutput${name.toUpperCamelCase()}"

    public val packEmscriptenOutputTask: TaskProvider<Zip> = tasks.register<Zip>(packEmscriptenOutputTaskName)

    public val emscriptenPackedOutput: Provider<RegularFile> = packEmscriptenOutputTask.flatMap { it.archiveFile }

    override fun getName(): String = name

    public companion object {
        private const val serialVersionUID: Long = -5
    }
}
