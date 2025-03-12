/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.wasm.sqlite.binary.gradle.settings

import at.released.wasm.sqlite.binary.gradle.settings.repository.googleFiltered
import at.released.wasm.sqlite.binary.gradle.settings.repository.icuRepository
import at.released.wasm.sqlite.binary.gradle.settings.repository.pixnewsRepository
import at.released.wasm.sqlite.binary.gradle.settings.repository.sqliteRepository

/*
 * Settings convention plugin that configures repositories used in the application
 */
pluginManagement {
    repositories {
        googleFiltered()
        mavenCentral()
        gradlePluginPortal()
        pixnewsRepository()
    }
}

dependencyResolutionManagement {
    repositories {
        googleFiltered()
        mavenCentral()
        sqliteRepository()
        icuRepository()
        pixnewsRepository()
    }
}
