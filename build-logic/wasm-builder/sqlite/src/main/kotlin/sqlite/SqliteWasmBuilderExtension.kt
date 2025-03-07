/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.builder.sqlite

import at.released.builder.emscripten.EmscriptenSdk.Companion.defaultEmscriptenRoot
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.property
import ru.pixnews.wasm.builder.base.WasmBuildDsl
import java.io.File
import javax.inject.Inject

@WasmBuildDsl
public abstract class SqliteWasmBuilderExtension @Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory,
    defaultEmscriptenVersion: Provider<String>,
) {
    public val emscriptenVersion: Property<String> = objects.property<String>()
        .convention(defaultEmscriptenVersion)
    public val emscriptenRoot: Property<File> = objects.property(File::class.java).convention(
        providers.defaultEmscriptenRoot(),
    )
    public abstract val builds: NamedDomainObjectContainer<SqliteWasmBuildSpec>
}
