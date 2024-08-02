/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.buildinfo

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

public interface WasmSqliteExtendedBuildInfo {
    @get:Input
    public val sqliteVersion: Property<String>

    @get:Input
    public val emscriptenVersion: Property<String>

    @get:Input
    @get:Optional
    public val compilerSettings: Property<WasmSqliteCompilerSettings>

    public interface WasmSqliteCompilerSettings {
        @get:Input
        @get:Optional
        public val additionalSourceFiles: ListProperty<String>

        @get:Input
        @get:Optional
        public val additionalIncludes: ListProperty<String>

        @get:Input
        public val additionalLibs: ListProperty<String>

        @get:Input
        public val codeGenerationFlags: ListProperty<String>

        @get:Input
        public val codeOptimizationFlags: ListProperty<String>

        @get:Input
        public val emscriptenFlags: ListProperty<String>

        @get:Input
        public val exportedFunctions: ListProperty<String>

        @get:Input
        public val sqliteFlags: ListProperty<String>
    }
}
