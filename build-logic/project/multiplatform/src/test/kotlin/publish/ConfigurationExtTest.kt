/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import org.gradle.api.Project
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.KonanTarget.LINUX_X64
import org.jetbrains.kotlin.konan.target.KonanTarget.MACOS_X64
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

@Suppress("UnstableApiUsage")
class ConfigurationExtTest {
    val project: Project = ProjectBuilder.builder().build()
    val objects: ObjectFactory = project.objects

    @TestFactory
    fun CheckAttributes(): List<DynamicTest> {
        val macosx64Target = createKotlinNativeTarget(MACOS_X64)
        val linuxX64Target = createKotlinNativeTarget(LINUX_X64)
        val jsTarget: KotlinJsIrTarget = mockk()

        return listOf(
            macosx64Target to MACOS_X64_PUBLISHED_RESOURCE_ATTRIBUTES,
            linuxX64Target to LINUX_X64_PUBLISHED_RESOURCE_ATTRIBUTES,
            jsTarget to JS_PUBLISHED_RESOURCE_ATTRIBUTES,
        ).map { (target, expectedAttributes) ->
            DynamicTest.dynamicTest("$target target") {
                val configuration = project.configurations.consumable("testElements$target") {
                    attributes.addMultiplatformNativeResourcesAttributes(objects, target)
                }.get()

                val attributes: AttributeContainer = configuration.attributes
                val allNames = attributes.keySet().map { it.name }

                assertThat(allNames).containsExactlyInAnyOrder(elements = expectedAttributes.keys.toTypedArray())
                attributes.keySet().forEach { attribute ->
                    assertThat(attributes.getAttribute(attribute).toString())
                        .isEqualTo(expectedAttributes[attribute.name])
                }
            }
        }
    }

    private fun createKotlinNativeTarget(konanTarget: KonanTarget = LINUX_X64): KotlinNativeTarget {
        val linuxTarget: KotlinNativeTarget = mockk()
        every { linuxTarget.konanTarget } returns konanTarget
        return linuxTarget
    }

    private companion object {
        val COMMON_PUBLISHED_RESOURCE_ATTRIBUTES: Map<String, String> = mapOf(
            "org.gradle.category" to "library",
            "org.gradle.dependency.bundling" to "external",
            "org.gradle.jvm.environment" to "non-jvm",
        )
        val WASM_JS_PUBLISHED_RESOURCE_ATTRIBUTES: Map<String, String> = COMMON_PUBLISHED_RESOURCE_ATTRIBUTES + mapOf(
            "org.gradle.libraryelements" to "kotlin-multiplatformresourcesjs",
            "org.gradle.usage" to "kotlin-multiplatformresourcesjs",
            "org.jetbrains.kotlin.platform.type" to "wasm",
            "org.jetbrains.kotlin.wasm.target" to "js",
        )
        val JS_PUBLISHED_RESOURCE_ATTRIBUTES: Map<String, String> = COMMON_PUBLISHED_RESOURCE_ATTRIBUTES + mapOf(
            "org.gradle.libraryelements" to "kotlin-multiplatformresourcesjs",
            "org.gradle.usage" to "kotlin-multiplatformresourcesjs",
            "org.jetbrains.kotlin.js.compiler" to "ir",
            "org.jetbrains.kotlin.platform.type" to "js",
        )
        val MACOS_X64_PUBLISHED_RESOURCE_ATTRIBUTES: Map<String, String> = COMMON_PUBLISHED_RESOURCE_ATTRIBUTES + mapOf(
            "org.gradle.libraryelements" to "kotlin-multiplatformresources",
            "org.gradle.usage" to "kotlin-multiplatformresources",
            "org.jetbrains.kotlin.native.target" to "macos_x64",
            "org.jetbrains.kotlin.platform.type" to "native",
        )
        val LINUX_X64_PUBLISHED_RESOURCE_ATTRIBUTES: Map<String, String> = COMMON_PUBLISHED_RESOURCE_ATTRIBUTES + mapOf(
            "org.gradle.libraryelements" to "kotlin-multiplatformresources",
            "org.gradle.usage" to "kotlin-multiplatformresources",
            "org.jetbrains.kotlin.native.target" to "linux_x64",
            "org.jetbrains.kotlin.platform.type" to "native",
        )
    }
}
