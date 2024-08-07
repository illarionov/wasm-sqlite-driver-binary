/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.builder.sqlite.internal

internal object BuildDirPath {
    internal const val COMPILE_WORK_DIR = "emscripten/work"
    internal const val STRIPPED_RESULT_DIR = "emscripten/out"
    internal const val PACKED_OUTPUT_DIR = "emscripten/pack"
    internal const val EMSCRIPTEN_WORK_CACHE = "emscripten/cache"

    internal fun compileDebugResultDir(buildName: String): String = "emscripten/debug-$buildName"
}
