/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.wasm.sqlite.binary.gradle.settings.repository

import org.gradle.api.artifacts.dsl.RepositoryHandler
import java.net.URI

public fun RepositoryHandler.icuRepository(): Unit = exclusiveContent {
    forRepository {
        ivy {
            url = URI("https://github.com/unicode-org/icu/releases/download")
            patternLayout {
                artifact("release-76-1/icu4c-76_1-src.[ext]")
            }
            metadataSources {
                artifact()
            }
        }
    }
    filter {
        includeModule("icu", "sources")
    }
}
