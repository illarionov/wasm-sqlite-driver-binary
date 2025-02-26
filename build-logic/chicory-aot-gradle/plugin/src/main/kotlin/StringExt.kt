/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.wasm2class

import java.util.Locale

internal fun String.capitalizeAscii(): String = replaceFirstChar {
    if (it.isLowerCase()) {
        it.titlecase(Locale.ROOT)
    } else {
        it.toString()
    }
}

internal fun String.toUpperCamelCase(): String = this
    .split("-", "_")
    .filter(String::isNotEmpty)
    .joinToString("", transform = String::capitalizeAscii)
