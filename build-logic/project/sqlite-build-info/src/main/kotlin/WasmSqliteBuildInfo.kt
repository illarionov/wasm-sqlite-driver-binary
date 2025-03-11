/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.buildinfo

import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.kotlin.dsl.property
import java.io.Serializable
import javax.inject.Inject

public open class WasmSqliteBuildInfo @Inject constructor(
    private val name: String,
    objects: ObjectFactory,
    project: Project,
) : Serializable, Named {
    @get:Input
    val rootPackage: Property<String> = objects.property<String>()
        .convention("ru.pixnews.wasm.sqlite.binary")

    @get:Input
    val wasmSqliteBuildClassName: Property<String> = objects.property<String>()
        .convention("SqliteAndroidWasmEmscriptenIcu346")

    @get:Input
    val wasmFileName: Property<String> = objects.property<String>()
        .convention("sqlite3-android-icu-3460000.wasm")

    @get:Input
    val wasmFileSubdir: Property<String> = objects.property<String>()
        .convention(getDefaultWasmFileSubdir(project.name))

    @get:Input
    val minMemorySize: Property<Long> = objects.property<Long>()
        .convention(50_331_648L)

    @get:Input
    val requireThreads: Property<Boolean> = objects.property<Boolean>()
        .convention(false)

    @get:Nested
    val extendedInfo: Property<WasmSqliteExtendedBuildInfo> = objects.property()

    @Input
    override fun getName(): String = name

    companion object {
        private const val serialVersionUID: Long = -1
        const val WSOH_RESOURCES_SUBDIR = "cassettes"

        fun getDefaultWasmFileSubdir(
            projectName: String,
        ): String = "$WSOH_RESOURCES_SUBDIR/${projectName.minusToUnderscore()}"

        private fun String.minusToUnderscore() = this.replace("-", "_")
    }
}
