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
    `maven-publish`
}

group = "ru.pixnews.wasm.sqlite.binary.gradle.multiplatform"

dependencies {
    implementation(libs.gradle.maven.publish.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlinx.binary.compatibility.validator.plugin)
    implementation(libs.agp.plugin.api)
    runtimeOnly(libs.agp.plugin)
}

val functionalTestRepository = layout.buildDirectory.dir("functional-tests-plugin-repository")

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
                implementation(libs.mockk)
                runtimeOnly(libs.junit.jupiter.engine)
            }
        }
        withType(JvmTestSuite::class).matching {
            it.name in setOf("functionalTest")
        }.configureEach {
            useJUnitJupiter(libs.versions.junit5)

            dependencies {
                implementation(project())
                implementation(libs.assertk)
            }

            targets {
                all {
                    testTask.configure {
                        configureTestTaskDefaults()
                        dependsOn(tasks.named("publishAllPublicationsToFunctionalTestsRepository"))
                        inputs.dir(functionalTestRepository)
                        shouldRunAfter(test)
                    }
                }
            }
        }
        register<JvmTestSuite>("functionalTest") {
            testType = "functional-test"
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

gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])

publishing {
    repositories {
        maven {
            name = "functionalTests"
            setUrl(functionalTestRepository)
        }
    }
    publications.withType<MavenPublication>().all {
        version = "9999"
    }
}

tasks.named<Task>("check") {
    dependsOn(testing.suites.named("functionalTest"))
}
