/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.reader

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.tempfolder.sync.TempDirectory
import at.released.tempfolder.sync.createTempDirectory
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl
import kotlin.test.AfterTest
import kotlin.test.Test

class AppleFileManagerSourceReaderTest {
    private val tempFolder: TempDirectory<*> = createTempDirectory { prefix = "wasm-binary-reader-" }
    private val fileSystem: FileSystem = SystemFileSystem

    @AfterTest
    fun cleanup() {
        tempFolder.delete()
    }

    @Test
    fun appleFileManagerSourceReader_shouldRedPath() {
        val url = WasmSourceUrl("resource.txt")
        val path = Path(tempFolder.append("src/macosMain/${url.url}").asString())
        SystemFileSystem.run {
            createDirectories(path.parent!!)
            sink(path).buffered().use {
                it.writeString("Test Resource")
            }
        }

        val reader = AppleFileManagerSourceReader(
            fileSystem = fileSystem,
            basePath = tempFolder.absolutePath().asString(),
        )

        val resourceContent = reader.readBytesOrThrow(url).decodeToString()
        assertThat(resourceContent).isEqualTo("Test Resource")
    }
}
