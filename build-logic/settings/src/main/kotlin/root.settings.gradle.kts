/*
 * Copyright (c) 2024, the wasm-sqlite-open-helper project authors and contributors.
 * Please see the AUTHORS file for details.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package at.released.wasm.sqlite.binary.gradle.settings

/*
 * Base settings convention plugin for the use in library modules
 */
plugins {
    id("at.released.wasm.sqlite.binary.gradle.settings.common")
    id("at.released.wasm.sqlite.binary.gradle.settings.repositories")
}
