/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.reader

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test
import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl

class JvmResourcesWasmBinaryReaderTest {
    @Test
    fun jvmWasmSourceReader_shouldReadResourcePath() {
        val url = requireNotNull(
            JvmResourcesWasmBinaryReaderTest::class.java.getResource("resource.txt"),
        ).toString()
        val wasmUrl = WasmSourceUrl(url)

        val reader = JvmResourcesWasmBinaryReader()
        val resourceContent = reader.readBytesOrThrow(wasmUrl).decodeToString().trim()
        assertThat(resourceContent).isEqualTo("Test Resource")
    }
}
