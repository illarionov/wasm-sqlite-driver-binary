/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.reader

import android.content.res.AssetManager
import kotlinx.io.RawSource
import kotlinx.io.asSource
import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl

public class AndroidAssetsWasmSourceReader(
    private val assertManager: AssetManager,
) : WasmSourceReader {
    private val jvmSourceReader = JvmResourcesWasmSourceReader()

    override fun getSourcePathCandidates(url: WasmSourceUrl): List<WasmBinarySource.Factory> {
        return if (!url.url.startsWith(ANDROID_ASSET_URL_PREFIX)) {
            jvmSourceReader.getSourcePathCandidates(url)
        } else {
            listOf(
                WasmBinarySource.Factory {
                    AndroidAssetsBinarySource(url, assertManager)
                },
            )
        }
    }

    private class AndroidAssetsBinarySource(
        private val url: WasmSourceUrl,
        private val assertManager: AssetManager,
    ) : WasmBinarySource {
        override val path: String = url.url
        override fun createSource(): RawSource {
            val fileName = url.url.substringAfter(ANDROID_ASSET_URL_PREFIX)
            return assertManager.open(fileName).asSource()
        }
    }

    private companion object {
        private const val ANDROID_ASSET_URL_PREFIX = "file:///android_asset/"
    }
}
