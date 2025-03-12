/*
 * Copyright 2024-2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.wasm.sqlite.binary.base

import at.released.cassettes.base.AssetUrl
import kotlin.jvm.JvmField

public interface WasmSqliteConfiguration {
    public val sqliteUrl: AssetUrl
    public val wasmMinMemorySize: Long
    public val requireThreads: Boolean
    public val buildInfo: WasmSqliteExtendedBuildInfo?

    public companion object {
        @JvmField
        @Suppress("NULLABLE_PROPERTY_TYPE")
        public val UNSET: WasmSqliteConfiguration = object : WasmSqliteConfiguration {
            override val sqliteUrl: AssetUrl = AssetUrl("")
            override val wasmMinMemorySize: Long = 0
            override val requireThreads: Boolean = false
            override val buildInfo: WasmSqliteExtendedBuildInfo? = null
        }
    }
}
