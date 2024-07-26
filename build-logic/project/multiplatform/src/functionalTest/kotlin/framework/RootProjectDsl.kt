/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.framework

import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.framework.fixtures.RootProjectFixtures
import java.io.File

public class RootProjectDsl private constructor(
    public val rootDir: File,
) {
    public fun writeFiles(
        vararg files: FileContent,
    ) {
        files.forEach {
            val dst = rootDir.resolve(it.dstPath)
            dst.parentFile.mkdirs()
            dst.writeText(it.content)
        }
    }

    internal companion object {
        public fun setupRoot(
            rootDir: File,
            vararg submodules: String,
        ) = RootProjectDsl(rootDir).apply {
            writeFiles(
                files = arrayOf(
                    RootProjectFixtures.getGradleProperties(),
                    RootProjectFixtures.getSettingsGradleKts(includeSubprojects = submodules),
                ),
            )
        }
    }
}
