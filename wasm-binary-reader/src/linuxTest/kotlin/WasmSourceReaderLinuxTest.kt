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
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl
import kotlin.test.AfterTest
import kotlin.test.Test

class WasmSourceReaderLinuxTest {
    val tempFolder: TempDirectory<*> = createTempDirectory { prefix = "sqlite3binary" }

    @AfterTest
    fun cleanup() {
        tempFolder.delete()
    }

    @Test
    fun linuxWasmSourceReader_shouldReadPath() {
        val url = WasmSourceUrl("wsohResources/resource.txt")
        val path = Path(tempFolder.append("testApp/${url.url}").asString())
        SystemFileSystem.run {
            createDirectories(path.parent!!)
            sink(path).buffered().use {
                it.writeString("Test Resource")
            }
        }

        val reader = LinuxWasmSourceReader(
            appName = "testApp",
            xdgBaseDirs = listOf(Path(tempFolder.absolutePath().asString())),
        )

        val resourceContent = reader.readBytesOrThrow(url).decodeToString()
        assertThat(resourceContent).isEqualTo("Test Resource")
    }
}
