/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.reader

import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl
import ru.pixnews.wasm.sqlite.open.helper.common.xdg.XdgBaseDirectory

internal actual fun getDefaultWasmSourceReader(): WasmSourceReader = LinuxWasmSourceReader()

public class LinuxWasmSourceReader(
    private val appName: String = "wasm-sqlite-open-helper",
    private val xdgBaseDirs: List<Path> = XdgBaseDirectory.getBaseDataDirectories(),
    private val fileSystem: FileSystem = SystemFileSystem,
) : WasmSourceReader {
    override fun getSourcePathCandidates(url: WasmSourceUrl): List<WasmBinarySource.Factory> {
        return xdgBaseDirs.map { xdgBaseDir ->
            val candidate = Path(xdgBaseDir, appName, url.url)
            WasmBinarySource.Factory {
                object : WasmBinarySource {
                    override val path: String = candidate.toString()
                    override fun createSource(): Source = fileSystem.source(candidate).buffered()
                }
            }
        }
    }
}
