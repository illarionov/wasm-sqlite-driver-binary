/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.reader

import okio.Source
import okio.buffer
import okio.use
import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl

public fun <R : Any> WasmSourceReader.readOrThrow(
    url: WasmSourceUrl,
    transform: (Source, String) -> Result<R>,
): R {
    val candidates = getSourcePathCandidates(url)
    val failedPaths: MutableList<Pair<String, Throwable>> = mutableListOf()
    for (sourceFactory in candidates) {
        val (wasmSource: Source, path: String) = sourceFactory()
        wasmSource.use { source ->
            val result: Result<R> = transform(source, path)
            result.onSuccess {
                return it
            }.onFailure {
                failedPaths.add(path to it)
            }
        }
    }
    if (failedPaths.isEmpty()) {
        throw WasmBinaryReaderIoException("Could not determine the full path to `$url`")
    } else {
        val (firstPath, firstError) = failedPaths.first()
        throw WasmBinaryReaderIoException(
            "Could not determine the full path to `$url`. " +
                    "Error when reading a file `$firstPath`: $firstError",
            failedPaths,
        )
    }
}

public fun WasmSourceReader.readBytesOrThrow(url: WasmSourceUrl): ByteArray = readOrThrow(url) { source, _ ->
    runCatching {
        source.buffer().readByteArray()
    }
}

public class WasmBinaryReaderIoException(
    message: String?,
    paths: List<Pair<String, Throwable>> = emptyList(),
) : RuntimeException(
    message,
    paths.lastOrNull()?.second,
)
