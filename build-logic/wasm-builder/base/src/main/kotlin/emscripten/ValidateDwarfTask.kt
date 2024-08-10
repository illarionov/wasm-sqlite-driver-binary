/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.builder.base.emscripten

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.process.ExecOperations
import org.gradle.process.internal.ExecException
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.text.RegexOption.IGNORE_CASE
import kotlin.text.RegexOption.MULTILINE

/**
 * Validates that the DWARF debug information of the WebAssembly binary does not contain absolute paths.
 *
 * Requires `llvm-dwarfdump` to be available.
 */
public open class ValidateDwarfTask @Inject constructor(
    private val execOperations: ExecOperations,
    objects: ObjectFactory,
) : DefaultTask() {
    @get:InputFile
    public val wasmBinary: RegularFileProperty = objects.fileProperty()

    @get:Input
    public val paths: ListProperty<String> = objects.listProperty()

    @TaskAction
    public fun validate() {
        if (paths.get().isEmpty()) {
            return
        }
        val binary = wasmBinary.get().asFile

        val outputStream = ByteArrayOutputStream()

        try {
            execOperations.exec {
                this.commandLine = listOf("llvm-dwarfdump", "--show-sources", binary.canonicalPath)
                this.standardOutput = outputStream
            }.rethrowFailure().assertNormalExitValue()
        } catch (execException: ExecException) {
            logger.error("Failed to execute `llvm-dwarfdump`", execException)
            return
        }

        val sources = outputStream.toString()
        val paths = findStringsStartsWithPath(sources, paths.get())
        if (paths.isNotEmpty()) {
            logger.error("Wasm binary `$binary` contains not mapped paths: ${paths.joinToString(", ")}")
        }
    }

    internal companion object {
        internal fun findStringsStartsWithPath(
            content: String,
            paths: List<String>,
        ): Set<String> {
            val pattern = getPatternStringStartsWithAnyOf(paths)
            val matches = pattern.findAll(content)
                .map { it.value }
                .distinct()
                .toSortedSet()
            return matches
        }

        internal fun getPatternStringStartsWithAnyOf(paths: List<String>): Regex {
            if (paths.isEmpty()) {
                return Regex("""^$""")
            }
            return paths
                .joinToString(
                    separator = "|",
                    prefix = """^(?:""",
                    postfix = """).*$""",
                    transform = Regex::escape,
                )
                .toRegex(setOf(MULTILINE, IGNORE_CASE))
        }
    }
}
