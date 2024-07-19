/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.reader

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.files.FileNotFoundException
import platform.Foundation.NSFileManager
import platform.posix.memcpy
import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl

/**
 * Temporary reader directly from the `build/processedResource` until we find out how to embed resources into
 * the application
 */
@OptIn(ExperimentalForeignApi::class)
public class AppleFileManagerSourceReader(
    private val fileManager: NSFileManager = NSFileManager.defaultManager(),
    private val basePath: String = fileManager.currentDirectoryPath,
) : WasmSourceReader {
    override fun getSourcePathCandidates(url: WasmSourceUrl): List<WasmBinarySource.Factory> {
        val subpath = url.url
        return listOf(
            "$basePath/build/processedResources/macosX64/main/$subpath",
            "$basePath/src/macosMain/$subpath",
            "$basePath/src/macosTest/$subpath",
            "$basePath/src/commonMain/$subpath",
            "$basePath/src/commonTest/$subpath",
        ).map { path ->
            WasmBinarySource.Factory { AppleWasmBinarySource(fileManager, path) }
        }
    }

    private class AppleWasmBinarySource(
        private val fileManager: NSFileManager,
        override val path: String,
    ) : WasmBinarySource {
        override fun createSource(): RawSource {
            val data = fileManager.contentsAtPath(path) ?: throw FileNotFoundException("File not found: $path")
            // TODO: FileHandle source?
            val bytes = ByteArray(data.length.toInt()).apply {
                usePinned { pinnedByteArray ->
                    memcpy(pinnedByteArray.addressOf(0), data.bytes, data.length)
                }
            }
            return Buffer().apply {
                write(bytes)
            }
        }
    }
}
