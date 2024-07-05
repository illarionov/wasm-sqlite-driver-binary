/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.reader

import okio.Source

public interface WasmBinarySource {
    public val source: Source
    public val path: String

    public operator fun component1(): Source = source
    public operator fun component2(): String = path

    public fun interface Factory {
        public operator fun invoke(): WasmBinarySource
    }
}
