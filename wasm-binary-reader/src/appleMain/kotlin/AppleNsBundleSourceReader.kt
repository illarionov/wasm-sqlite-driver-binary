/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.reader

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import okio.Buffer
import okio.FileNotFoundException
import okio.Source
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.posix.memcpy
import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl

/**
 * Reader from the main bundle.
 *
 * Requires placing resources into the test binary's output directory on the consumer side so that they can be accessed
 * using `NSBundle.mainBundle`.
 */
public class AppleNsBundleSourceReader(
    private val bundle: NSBundle = NSBundle.mainBundle,
    private val fileManager: NSFileManager = NSFileManager.defaultManager,
    private val wshohResourcesRoot: String = "wsoh-resources",
) : WasmSourceReader {
    override fun getSourcePathCandidates(url: WasmSourceUrl): List<WasmBinarySource.Factory> = listOf(
        WasmBinarySource.Factory { NsBundleBinarySource(url) },
    )

    @OptIn(ExperimentalForeignApi::class)
    private inner class NsBundleBinarySource(
        private val url: WasmSourceUrl,
    ) : WasmBinarySource {
        override val path: String = url.url

        override fun createSource(): Source {
            val fullPath = url.url

            val absolutePath = bundle.pathForResource(
                name = fullPath.substringBeforeLast("."),
                ofType = fullPath.substringAfterLast("."),
                inDirectory = wshohResourcesRoot,
            ) ?: throw FileNotFoundException("File not found in bundle: $fullPath. Bundle base: ${bundle.bundlePath}")
            val data = fileManager.contentsAtPath(absolutePath)
                ?: throw FileNotFoundException("Can not read $absolutePath")

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
