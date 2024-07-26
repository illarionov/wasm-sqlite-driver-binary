/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.framework.fixtures

import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.framework.FileContent

object RootProjectFixtures {
    public fun getGradleProperties(): FileContent = FileContent(
        "gradle.properties",
        """
            |org.gradle.jvmargs=-Xmx2G -XX:MaxMetaspaceSize=768M -XX:SoftRefLRUPolicyMSPerMB=0 -XX:+UseParallelGC -XX:+HeapDumpOnOutOfMemoryError
            |org.gradle.workers.max=2
            |org.gradle.vfs.watch=false
            |org.gradle.parallel=false
            |org.gradle.caching=true
            |org.gradle.configuration-cache=true
        """.trimMargin(),
    )

    public fun getSettingsGradleKts(
        vararg includeSubprojects: String,
    ): FileContent {
        val includes = includeSubprojects.joinToString("\n") { """include("$it")""" }
        val functionalTestsMaven = Paths.functionTestPluginRepository
        val kotlinVersion = "2.0.0"

        val content = """
            |buildscript {
            |    repositories {
            |        exclusiveContent {
            |            forRepository {
            |                maven { url = uri("file://$functionalTestsMaven") }
            |            }
            |            filter {
            |                includeGroupAndSubgroups("ru.pixnews.wasm")
            |            }
            |        }
            |        mavenCentral()
            |        google()
            |    }
            |    dependencies {
            |        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
            |        classpath("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform:multiplatform:9999")
            |    }
            |}
            |
            |dependencyResolutionManagement {
            |    repositories {
            |        google()
            |        mavenCentral()
            |    }
            |}
            |$includes
        """.trimMargin()
        return FileContent("settings.gradle.kts", content)
    }
}
