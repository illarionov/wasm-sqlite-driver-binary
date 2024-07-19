/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MatchingDeclarationName")

package ru.pixnews.wasm.sqlite.binary

import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl

internal actual fun getSqliteAndroidWasmEmscriptenIcuMtPthread346Url(): WasmSourceUrl = WasmSourceUrl(
    requireNotNull(
        SqliteAndroidWasmEmscriptenIcuMtPthread346::class.java.getResource(
            "sqlite3-android-icu-mt-pthread-3460000.wasm",
        ),
    ).toString(),
)
