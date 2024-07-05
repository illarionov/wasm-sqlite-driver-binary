/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary

import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl
import ru.pixnews.wasm.sqlite.binary.base.WasmSqliteConfiguration

public actual object SqliteAndroidWasmEmscriptenIcuMtPthread346 : WasmSqliteConfiguration {
    override val sqliteUrl: WasmSourceUrl = WasmSourceUrl.create(
        requireNotNull(
            SqliteAndroidWasmEmscriptenIcuMtPthread346::class.java.getResource(
                "sqlite3-android-icu-mt-pthread-3460000.wasm",
            ),
        ).toString(),
    )
    override val wasmMinMemorySize: Long = 50_331_648L
    override val requireThreads: Boolean = true
}
