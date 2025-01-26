/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.settings.repository

import org.gradle.api.artifacts.dsl.RepositoryHandler
import java.net.URI

public fun RepositoryHandler.sqliteRepository(): Unit = exclusiveContent {
    forRepository {
        ivy {
            url = URI("https://www.sqlite.org/")
            patternLayout {
                artifact("2025/sqlite-amalgamation-[revision].[ext]")
            }
            metadataSources {
                artifact()
            }
        }
    }
    filter {
        includeModule("sqlite", "amalgamation")
    }
}
