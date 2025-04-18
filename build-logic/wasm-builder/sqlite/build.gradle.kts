/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage")

import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "ru.pixnews.wasm.builder.sqlite"

kotlin {
    explicitApi = ExplicitApiMode.Warning
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        optIn = listOf("at.released.builder.emscripten.InternalEmscriptenApi")
    }
}

java {
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    api(projects.base)
    api(libs.emscripten.gradle.plugin)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnit()
            targets.all {
                testTask.configure {
                    configureTestTaskDefaults()
                }
            }
            dependencies {
                implementation(libs.assertk)
            }
        }
        register<JvmTestSuite>("functionalTest") {
            useJUnit()

            dependencies {
                implementation(gradleApi())
                implementation(gradleTestKit())
                implementation(libs.assertk)
                implementation(project())
            }

            targets.all {
                testTask.configure {
                    configureTestTaskDefaults()
                    shouldRunAfter(test)
                }
            }
        }
    }
}

gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])

private fun Test.configureTestTaskDefaults() {
    maxHeapSize = "1512M"
    jvmArgs = listOf("-XX:MaxMetaspaceSize=768M")
    testLogging {
        events = if (providers.gradleProperty("verboseTest").map(String::toBoolean).getOrElse(false)) {
            setOf(
                TestLogEvent.FAILED,
                TestLogEvent.STANDARD_ERROR,
                TestLogEvent.STANDARD_OUT,
            )
        } else {
            setOf(TestLogEvent.FAILED)
        }
    }
}

tasks.named<Task>("check") {
    dependsOn(testing.suites.named("functionalTest"))
}
