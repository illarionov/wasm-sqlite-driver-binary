/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.reader

import kotlinx.io.RawSource
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.Foundation.NSFileManager
import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl

/**
 * Temporary reader directly from the `build/processedResource` until we find out how to embed resources into
 * the application
 */
public class AppleFileManagerSourceReader(
    private val fileSystem: FileSystem = SystemFileSystem,
    private val basePath: String = NSFileManager.defaultManager().currentDirectoryPath,
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
            WasmBinarySource.Factory { AppleWasmBinarySource(path, fileSystem) }
        }
    }

    private class AppleWasmBinarySource(
        override val path: String,
        private val fileSystem: FileSystem = SystemFileSystem,
    ) : WasmBinarySource {
        override fun createSource(): RawSource {
            return fileSystem.source(Path(path))
        }
    }
}
