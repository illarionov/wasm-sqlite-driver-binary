/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "ru.pixnews.wasm.builder.base"

kotlin {
    explicitApi = ExplicitApiMode.Warning
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

java {
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.assertk)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
