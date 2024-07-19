/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.reader

import kotlinx.io.RawSource
import kotlinx.io.asSource
import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl
import java.net.URI

public class JvmResourcesWasmBinaryReader : WasmSourceReader {
    override fun getSourcePathCandidates(url: WasmSourceUrl): List<WasmBinarySource.Factory> {
        return listOf(
            WasmBinarySource.Factory {
                object : WasmBinarySource {
                    override val path: String = url.toString()
                    override fun createSource(): RawSource {
                        return URI(url.url).toURL().openStream().asSource()
                    }
                }
            },
        )
    }
}
