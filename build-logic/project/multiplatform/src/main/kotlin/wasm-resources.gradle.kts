/*
 * Copyright 2024-2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform

/*
 * Convention plugin that configures the creation and publication of wasm binaries as resources
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("at.released.cassettes.plugin.rewrap")
}

cassettes {
    files.from(configurations.findByName("wasmSqliteReleaseElements")!!.artifacts.files)

    targetPackage = "ru.pixnews.wasm.sqlite.binary"
}
