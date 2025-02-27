/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.wasm2class

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

@Wasm2ClassGeneratorDsl
public abstract class Wasm2ClassExtension @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout,
    project: Project,
) {
    val modules: NamedDomainObjectContainer<Wasm2ClassMachineModuleSpec> = objects.domainObjectContainer(
        Wasm2ClassMachineModuleSpec::class.java,
    ) { name -> objects.newInstance(Wasm2ClassMachineModuleSpec::class.java, name, targetPackage) }

    /**
     * The root package for the generated classes.
     */
    val targetPackage: Property<String> = objects.property<String>()
        .convention(project.provider { project.group.toString() })

    /**
     * The root directory for the generated classes.
     */
    val outputDirectory: DirectoryProperty = objects.directoryProperty().convention(
        layout.buildDirectory.dir("generated-chicory-aot"),
    )

    /**
     * The output directory for the generated Java .class files.
     */
    val outputClasses: DirectoryProperty = objects.directoryProperty().convention(
        outputDirectory.map { it.dir("classes") },
    )

    /**
     *  The output directory for the generated Java source files.
     */
    val outputSources: DirectoryProperty = objects.directoryProperty().convention(
        outputDirectory.map { it.dir("sources") },
    )

    /**
     *  The output directory for the generated resources.
     */
    val outputResources: DirectoryProperty = objects.directoryProperty().convention(
        outputDirectory.map { it.dir("resources") },
    )
}
