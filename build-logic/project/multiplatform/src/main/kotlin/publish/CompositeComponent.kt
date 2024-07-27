/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish

import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.component.ComponentWithCoordinates
import org.gradle.api.component.ComponentWithVariants
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.api.internal.component.SoftwareComponentInternal
import org.gradle.api.internal.component.UsageContext
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinSoftwareComponentWithCoordinatesAndPublication
import javax.inject.Inject

public open class CompositeComponent @Inject constructor(
    private val softwareComponentFactory: SoftwareComponentFactory,
    private val parent: KotlinSoftwareComponentWithCoordinatesAndPublication,
) : SoftwareComponentInternal by parent, ComponentWithVariants by parent, ComponentWithCoordinates by parent {
    val adHocComponent: AdhocComponentWithVariants = softwareComponentFactory.adhoc("compositeAdHoc")

    override fun getName(): String = parent.name

    override fun getUsages(): Set<UsageContext> {
        return parent.usages + (adHocComponent as SoftwareComponentInternal).usages
    }
}
