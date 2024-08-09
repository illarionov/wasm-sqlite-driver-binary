/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.builder.base.ext

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class StringExtTest {
    @ParameterizedTest
    @CsvSource(
        "android-wasm-emscripten-icu-346,AndroidWasmEmscriptenIcu346",
        "android_wasm_emscripten-icu-346,AndroidWasmEmscriptenIcu346",
        "-android__wasm_-emscripten--icu-346-,AndroidWasmEmscriptenIcu346",
        "--_,''",
    )
    fun toUpperCamelCase(
        lowercase: String,
        expectedResult: String,
    ) {
        assertThat(lowercase.toUpperCamelCase()).isEqualTo(expectedResult)
    }
}
