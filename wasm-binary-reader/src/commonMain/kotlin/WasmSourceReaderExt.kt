/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.reader

import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.readByteArray
import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl

public fun <R : Any> WasmSourceReader.readOrThrow(
    url: WasmSourceUrl,
    transform: (RawSource, String) -> Result<R>,
): R {
    val candidates: List<WasmBinarySource.Factory> = getSourcePathCandidates(url)
    val failedPaths: MutableList<Pair<String, Throwable>> = mutableListOf()
    for (sourceFactory in candidates) {
        val wasmBinarySource: WasmBinarySource = sourceFactory()
        val result: Result<R> = tryReadCandidate(wasmBinarySource, transform)
        result.onSuccess {
            return it
        }.onFailure {
            failedPaths.add(wasmBinarySource.path to it)
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

private fun <R : Any> tryReadCandidate(
    candidate: WasmBinarySource,
    transform: (RawSource, String) -> Result<R>,
): Result<R> {
    val source = try {
        candidate.createSource()
    } catch (@Suppress("TooGenericExceptionCaught") ex: Throwable) {
        return Result.failure(WasmBinaryReaderException("Can not create source `${candidate.path}`", ex))
    }

    return source.use {
        transform(it, candidate.path)
    }
}

public fun WasmSourceReader.readBytesOrThrow(url: WasmSourceUrl): ByteArray = readOrThrow(url) { source, _ ->
    runCatching {
        source.buffered().readByteArray()
    }
}

public open class WasmBinaryReaderException(
    message: String?,
    throwable: Throwable? = null,
) : RuntimeException(message, throwable)

public class WasmBinaryReaderIoException(
    message: String?,
    paths: List<Pair<String, Throwable>> = emptyList(),
) : WasmBinaryReaderException(
    message,
    paths.lastOrNull()?.second,
)
