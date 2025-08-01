/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

pluginManagement {
    includeBuild("build-logic/settings")
    includeBuild("build-logic/project") { name = "sohb-gradle-project-plugins" }
    includeBuild("build-logic/wasm-builder") { name = "sohb-gradle-wasm-builder-plugins" }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
    id("at.released.wasm.sqlite.binary.gradle.settings.root")
}

// Workaround for https://github.com/gradle/gradle/issues/26020
buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.3")
        classpath("com.diffplug.spotless:spotless-plugin-gradle:7.2.1")
        classpath("com.saveourtool.diktat:diktat-gradle-plugin:2.0.0")
        classpath("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.8")
        classpath("at.released.cassettes:cassettes-plugin:0.1-alpha01")
        classpath("at.released.wasm2class:plugin:0.3")
        classpath(
            group = "org.jetbrains.kotlinx.binary-compatibility-validator",
            name = "org.jetbrains.kotlinx.binary-compatibility-validator.gradle.plugin",
            version = "0.17.0",
        )
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.20")
    }
}

rootProject.name = "wasm-sqlite-open-helper-binary"

include("sqlite-binary-api")
include("icu-wasm")
include("sqlite-android-wasm-emscripten-icu-mt-pthread-349")
include("sqlite-android-wasm-emscripten-icu-349")
include("sqlite-android-wasm-emscripten-icu-aot-349")
include("sqlite-wasm-emscripten-349")
include("sqlite-wasm-emscripten-aot-349")
include("sqlite-wasm-emscripten-mt-pthread-349")
