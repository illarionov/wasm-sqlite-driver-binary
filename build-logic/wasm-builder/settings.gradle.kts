/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

pluginManagement {
    includeBuild("../settings")
}

plugins {
    id("at.released.wasm.sqlite.binary.gradle.settings.root")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "sohb-gradle-wasm-builder-plugins"

include("base")
include("icu")
include("sqlite")
