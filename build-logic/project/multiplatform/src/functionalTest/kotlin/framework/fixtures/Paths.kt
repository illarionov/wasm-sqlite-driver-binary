/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.framework.fixtures

import java.io.File

object Paths {
    internal val userDir: String
        get() = System.getProperty("user.dir")
    public val testProjectsRoot: File
        get() = File(userDir, "src/testProjects")
    public val functionTestPluginRepository: File
        get() = File(userDir, "build/functional-tests-plugin-repository")
}
