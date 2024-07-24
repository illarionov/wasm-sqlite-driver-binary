/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish

import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.Bundling.BUNDLING_ATTRIBUTE
import org.gradle.api.attributes.Bundling.EXTERNAL
import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Category.LIBRARY
import org.gradle.api.attributes.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.attributes.java.TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.Companion.konanTargetAttribute
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsCompilerAttribute
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget

internal fun AttributeContainer.addMultiplatformNativeResourcesAttributes(
    objects: ObjectFactory,
    target: KotlinTarget,
) {
    attribute(CATEGORY_ATTRIBUTE, objects.named(LIBRARY))
    attribute(BUNDLING_ATTRIBUTE, objects.named(EXTERNAL))
    attribute(TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named("non-jvm"))
    attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(target.multiplatformResourcesUsageAttribute))
    attribute(USAGE_ATTRIBUTE, objects.named(target.multiplatformResourcesUsageAttribute))

    if (target is KotlinNativeTarget) {
        attribute(konanTargetAttribute, target.konanTarget.name)
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.native)
    }

    if (target is KotlinJsIrTarget) {
        attribute(KotlinJsCompilerAttribute.jsCompilerAttribute, KotlinJsCompilerAttribute.ir)
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.js)
    }
}

private val KotlinTarget.multiplatformResourcesUsageAttribute: String
    get() = when {
        this is KotlinJsIrTarget -> "kotlin-multiplatformresourcesjs"
        else -> "kotlin-multiplatformresources"
    }
