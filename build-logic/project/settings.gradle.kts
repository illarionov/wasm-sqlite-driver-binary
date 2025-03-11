
/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

pluginManagement {
    includeBuild("../settings")
    includeBuild("../wasm-builder") { name = "sohb-gradle-wasm-builder-plugins" }
}

plugins {
    id("ru.pixnews.wasm.sqlite.binary.gradle.settings.root")
}

// Workaround for https://github.com/gradle/gradle/issues/26020
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$embeddedKotlinVersion")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
}

include("lint")
include("multiplatform")
include("sqlite-build-info")
include("sqlite-build-info-ext")

rootProject.name = "sohb-gradle-project-plugins"
