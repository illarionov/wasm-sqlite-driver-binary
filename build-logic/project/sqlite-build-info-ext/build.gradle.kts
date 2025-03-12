/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "at.released.wasm.sqlite.binary.gradle"

dependencies {
    implementation("ru.pixnews.wasm.builder.sqlite:sqlite")
    implementation(projects.sqliteBuildInfo)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

java {
    targetCompatibility = JavaVersion.VERSION_21
}

testing {
    suites {
        @Suppress("UnusedPrivateProperty")
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter(libs.versions.junit5)
            targets {
                all {
                    testTask.configure {
                        configureTestTaskDefaults()
                    }
                }
            }
            dependencies {
                implementation(platform(libs.junit.bom))
                implementation(libs.assertk)
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.params)
                runtimeOnly(libs.junit.jupiter.engine)
            }
        }
    }
}

private fun Test.configureTestTaskDefaults() {
    maxHeapSize = "1512M"
    jvmArgs = listOf("-XX:MaxMetaspaceSize=768M")
    testLogging {
        if (providers.gradleProperty("verboseTest").map(String::toBoolean).getOrElse(false)) {
            events = setOf(
                TestLogEvent.FAILED,
                TestLogEvent.STANDARD_ERROR,
                TestLogEvent.STANDARD_OUT,
            )
        } else {
            events = setOf(TestLogEvent.FAILED)
        }
    }
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = providers.environmentVariable("TEST_JDK_VERSION")
            .map { JavaLanguageVersion.of(it.toInt()) }
            .orElse(JavaLanguageVersion.of(21))
    }
}
