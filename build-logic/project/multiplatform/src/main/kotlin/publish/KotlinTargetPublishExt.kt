/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish

import org.gradle.api.artifacts.Configuration
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.component.ConfigurationVariantDetails
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget

/**
 * Adds variants from [configuration] to [KotlinTarget] published component.
 *
 * Workaround for https://youtrack.jetbrains.com/issue/KT-58830
 */
public fun KotlinTarget.addVariantsFromConfigurationsToPublication(
    configuration: Configuration,
    details: ConfigurationVariantDetails.() -> Unit,
) {
    val component = this.components.first()
    check(KOTLIN_SOFTWARE_COMPONENT_CLASS.isAssignableFrom(component::class.java))
    val getAdHocComponentField = KOTLIN_SOFTWARE_COMPONENT_CLASS.getDeclaredField("adhocComponent").apply {
        setAccessible(true)
    }
    val adHocComponent = getAdHocComponentField.get(component) as AdhocComponentWithVariants
    adHocComponent.addVariantsFromConfiguration(configuration, details)
}

private val KOTLIN_SOFTWARE_COMPONENT_CLASS = Class.forName(
    "org.jetbrains.kotlin.gradle.plugin.mpp.KotlinTargetSoftwareComponentImpl",
)
