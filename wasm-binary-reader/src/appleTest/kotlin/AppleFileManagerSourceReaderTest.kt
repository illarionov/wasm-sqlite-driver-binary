/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.reader

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl
import ru.pixnews.wasm.sqlite.open.helper.common.tempfolder.TempFolder
import kotlin.test.AfterTest
import kotlin.test.Test

class AppleFileManagerSourceReaderTest {
    private val tempFolder: TempFolder = TempFolder.create()
    private val fileSystem: FileSystem = SystemFileSystem

    @AfterTest
    fun cleanup() {
        tempFolder.delete()
    }

    @Test
    fun appleFileManagerSourceReader_shouldRedPath() {
        val url = WasmSourceUrl("resource.txt")
        val path = Path(tempFolder.resolve("src/macosMain/${url.url}"))
        SystemFileSystem.run {
            createDirectories(path.parent!!)
            sink(path).buffered().use {
                it.writeString("Test Resource")
            }
        }

        val reader = AppleFileManagerSourceReader(
            fileSystem = fileSystem,
            basePath = tempFolder.path,
        )

        val resourceContent = reader.readBytesOrThrow(url).decodeToString()
        assertThat(resourceContent).isEqualTo("Test Resource")
    }
}
