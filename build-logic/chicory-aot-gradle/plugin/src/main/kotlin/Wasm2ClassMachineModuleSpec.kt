/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.wasm2class

import org.gradle.api.Named
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NAME_ONLY
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Wasm2ClassGeneratorDsl
public open class Wasm2ClassMachineModuleSpec @Inject constructor(
    private val name: String,
    objects: ObjectFactory,
    baseTargetPackage: Provider<String>,
) : Named {
    /**
     * The WASM binary file to be compiled.
     */
    @get:InputFile
    @get:PathSensitive(NAME_ONLY)
    val wasm: RegularFileProperty = objects.fileProperty()

    /**
     * The root package for the generated classes.
     */
    @get:Input
    val targetPackage: Property<String> = objects.property<String>().convention(baseTargetPackage)

    /**
     * The name of the Module class.
     */
    @get:Input
    val moduleClassSimpleName: Property<String> = objects.property<String>().convention(
        "${name}Module".toUpperCamelCase(),
    )

    /**
     * The name of the Machine class.
     */
    @get:Input
    val machineClassSimpleName: Property<String> = objects.property<String>().convention(
        "${name}Machine".toUpperCamelCase(),
    )

    /**
     * The name of the generated stripped WASM resource.
     */
    @get:Input
    val wasmMetaResourceName: Property<String> = objects.property<String>().convention(
        "${name.toUpperCamelCase()}.meta",
    )

    /**
     * Base name for the Machine and Module classes
     */
    @Input
    override fun getName(): String = name
}
