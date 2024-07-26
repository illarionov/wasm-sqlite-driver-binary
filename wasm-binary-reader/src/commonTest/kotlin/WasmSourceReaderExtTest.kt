/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.reader

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.writeString
import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl
import ru.pixnews.wasm.sqlite.binary.reader.WasmBinarySource.Factory
import kotlin.test.Test
import kotlin.test.assertFailsWith

class WasmSourceReaderExtTest {
    @Test
    fun readOrThrow_should_read_source() {
        val reader = WasmSourceReader { _: WasmSourceUrl ->
            listOf(
                Factory { createFailureWasmBinarySource("/opt/path.txt") },
                Factory { createSuccessWasmBinarySource("/usr/data/path.txt") },
            )
        }

        val data = reader.readBytesOrThrow(WasmSourceUrl("path.txt")).decodeToString()
        assertThat(data).isEqualTo("Success")
    }

    @Test
    fun readOrThrow_should_throw_exception_on_failure() {
        val reader = WasmSourceReader { _: WasmSourceUrl ->
            listOf(
                Factory { createFailureWasmBinarySource("/opt/path.txt") },
                Factory { createFailureWasmBinarySource("/usr/data/path.txt") },
            )
        }

        assertFailsWith<WasmBinaryReaderIoException> {
            reader.readBytesOrThrow(WasmSourceUrl("path.txt"))
        }
    }

    @Test
    fun readOrThrow_should_throw_exception_if_source_list_is_empty() {
        val reader = WasmSourceReader { _: WasmSourceUrl -> emptyList() }

        assertFailsWith<WasmBinaryReaderIoException> {
            reader.readBytesOrThrow(WasmSourceUrl("path.txt"))
        }
    }

    @Test
    fun readOrThrow_should_throw_if_factory_throws_exception() {
        class UnexpectedException : RuntimeException()

        val reader = WasmSourceReader { _: WasmSourceUrl ->
            listOf(
                Factory { throw UnexpectedException() },
                Factory { createSuccessWasmBinarySource("/usr/data/path.txt") },
            )
        }

        assertFailsWith<UnexpectedException> {
            reader.readBytesOrThrow(WasmSourceUrl("path.txt"))
        }
    }

    @Test
    fun readOrThrow_should_not_throw_if_create_source_throws_exception() {
        val reader = WasmSourceReader { _: WasmSourceUrl ->
            listOf(
                Factory {
                    object : WasmBinarySource {
                        override val path: String = "nonexistent"
                        override fun createSource(): RawSource = throw FileNotFoundException("file not found")
                    }
                },
                Factory {
                    createSuccessWasmBinarySource("/usr/data/path.txt")
                },
            )
        }

        val dataBytes = reader.readBytesOrThrow(WasmSourceUrl("path.txt"))
        val dataString = dataBytes.decodeToString()
        assertThat(dataString).isEqualTo("Success")
    }

    @Test
    fun readOrThrow_should_throw_if_transformer_throws_exception() {
        class UnexpectedException : RuntimeException()

        val reader = WasmSourceReader { _: WasmSourceUrl ->
            listOf(
                Factory { createSuccessWasmBinarySource("/usr/data/path.txt") },
            )
        }

        assertFailsWith<UnexpectedException> {
            reader.readOrThrow(WasmSourceUrl("path.txt")) { _: RawSource, _: String ->
                throw UnexpectedException()
            }
        }
    }

    companion object {
        fun createSuccessWasmBinarySource(
            path: String,
            content: Buffer = createSuccessContent(),
        ) = object : WasmBinarySource {
            override val path: String = path
            override fun createSource(): RawSource = content
        }

        private fun createSuccessContent(): Buffer = Buffer().apply {
            writeString("Success")
        }

        fun createFailureWasmBinarySource(
            path: String,
            createSource: () -> Nothing = { throw FileNotFoundException("$path not found") },
        ) = object : WasmBinarySource {
            override val path: String = path
            override fun createSource(): RawSource = createFailureRawSource(createSource)
        }

        private fun createFailureRawSource(
            failureFactory: () -> Nothing = { throw FileNotFoundException("file not found") },
        ): RawSource = object : RawSource {
            override fun close() = Unit
            override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
                failureFactory()
            }
        }
    }
}
