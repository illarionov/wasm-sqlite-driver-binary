/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.base

import kotlin.jvm.JvmStatic

public interface WasmSourceUrl {
    public val url: String

    private class DefaultWasmSourceUrl(
        override val url: String,
    ) : WasmSourceUrl {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other == null || this::class != other::class) {
                return false
            }

            other as WasmSourceUrl

            return url == other.url
        }

        override fun hashCode(): Int = url.hashCode()

        override fun toString(): String = "DefaultWasmSourceUrl('$url')"
    }

    public companion object {
        @JvmStatic
        public fun create(url: String): WasmSourceUrl = DefaultWasmSourceUrl(url)
    }
}
