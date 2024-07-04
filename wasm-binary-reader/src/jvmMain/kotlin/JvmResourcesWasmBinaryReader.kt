/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.reader

import okio.source
import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl
import java.io.File
import java.net.URI

public class JvmResourcesWasmBinaryReader : WasmSourceReader {
    override fun getSourcePathCandidates(url: WasmSourceUrl): List<WasmSourceFactory> {
        val urlAsFile = File(URI(url.url))
        return listOf(WasmSourceFactory(urlAsFile::source))
    }
}
