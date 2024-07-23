/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.reader

import kotlinx.io.RawSource
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.Foundation.NSBundle
import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl

/**
 * Reader from the main bundle.
 *
 * Requires placing resources into the test binary's output directory on the consumer side so that they can be accessed
 * using `NSBundle.mainBundle`.
 */
public class AppleNsBundleSourceReader(
    private val bundle: NSBundle = NSBundle.mainBundle,
    private val fileSystem: FileSystem = SystemFileSystem,
    private val wshohResourcesRoot: String = "wsoh-resources",
) : WasmSourceReader {
    override fun getSourcePathCandidates(url: WasmSourceUrl): List<WasmBinarySource.Factory> = listOf(
        WasmBinarySource.Factory { NsBundleBinarySource(url) },
    )

    private inner class NsBundleBinarySource(
        private val url: WasmSourceUrl,
    ) : WasmBinarySource {
        override val path: String = url.url

        override fun createSource(): RawSource {
            val fullPath = url.url
            val absolutePath = bundle.pathForResource(
                name = fullPath.substringBeforeLast("."),
                ofType = fullPath.substringAfterLast("."),
                inDirectory = wshohResourcesRoot,
            ) ?: throw FileNotFoundException("File not found in bundle: $fullPath. Bundle base: ${bundle.bundlePath}")
            return fileSystem.source(Path(absolutePath))
        }
    }
}
