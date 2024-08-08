/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.builder.sqlite.internal

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

public abstract class FilePrefixMapEntry @Inject constructor(
    @get:Input
    public val newPath: String,
) {
    @get:Input
    public abstract val oldPath: Property<String>

    public companion object {
        public fun ObjectFactory.createFilePrefixMapEntry(
            oldPath: Provider<String>,
            newPath: String,
        ): FilePrefixMapEntry = newInstance<FilePrefixMapEntry>(newPath).apply {
            this.oldPath.set(oldPath)
        }

        public fun ObjectFactory.createFilePrefixMapEntry(
            oldPath: String,
            newPath: String,
        ): FilePrefixMapEntry = newInstance<FilePrefixMapEntry>(newPath).apply {
            this.oldPath.set(oldPath)
        }
    }
}
