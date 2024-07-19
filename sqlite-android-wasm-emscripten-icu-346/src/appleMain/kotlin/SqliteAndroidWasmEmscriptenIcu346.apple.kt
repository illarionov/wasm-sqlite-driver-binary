/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MatchingDeclarationName")

package ru.pixnews.wasm.sqlite.binary

import ru.pixnews.wasm.sqlite.binary.base.WasmSourceUrl

private val sqliteAndroidWasmEmscriptenIcu346Url = WasmSourceUrl(
    "wsohResources/sqlite_android_wasm_emscripten_icu_346/sqlite3-android-icu-3460000.wasm",
)

internal actual fun getSqliteAndroidWasmEmscriptenIcu346Url(): WasmSourceUrl = sqliteAndroidWasmEmscriptenIcu346Url
