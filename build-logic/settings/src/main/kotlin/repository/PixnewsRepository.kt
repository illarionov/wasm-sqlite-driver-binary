/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.wasm.sqlite.binary.gradle.settings.repository

import org.gradle.api.artifacts.dsl.RepositoryHandler
import java.net.URI

fun RepositoryHandler.pixnewsRepository() {
    maven {
        url = URI("https://maven.pixnews.ru")
        mavenContent {
            includeGroupAndSubgroups("at.released.builder.emscripten")
            includeGroupAndSubgroups("at.released.cassettes")
        }
    }
}
