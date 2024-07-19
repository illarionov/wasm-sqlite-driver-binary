/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary

import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl
import ru.pixnews.wasm.sqlite.binary.base.WasmSqliteConfiguration

internal expect fun getSqliteAndroidWasmEmscriptenIcu346Url(): WasmSourceUrl

public object SqliteAndroidWasmEmscriptenIcu346 : WasmSqliteConfiguration {
    override val sqliteUrl: WasmSourceUrl get() = getSqliteAndroidWasmEmscriptenIcu346Url()
    override val wasmMinMemorySize: Long = 50_331_648L
    override val requireThreads: Boolean = false
}
