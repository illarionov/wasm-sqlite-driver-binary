/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.builder.base.emscripten

import assertk.assertThat
import assertk.assertions.containsExactly
import org.junit.jupiter.api.Test
import ru.pixnews.wasm.builder.base.emscripten.ValidateDwarfTask.Companion.findStringsStartsWithPath

class ValidateDwarfTaskTest {
    @Test
    fun `getPatternStringStartsWithAnyOf() should return correct substrings`() {
        val testContent = listOf(
            "/home",
            "/home/work/test1",
            "/sqlite",
            "/emsdk/path1",
            "/home/work/path3",
            "/emsdk/path2",
            "/home/work/path4",
            "/home/user",
            "/emsdk/home/user",
            "/home/user/subpath",
        ).joinToString("\n")

        val shouldNotContainPaths = listOf(
            "/home/work",
            "/home/user",
        )
        val incorrectPaths = findStringsStartsWithPath(testContent, shouldNotContainPaths)

        assertThat(incorrectPaths.toList())
            .containsExactly(
                "/home/user",
                "/home/user/subpath",
                "/home/work/path3",
                "/home/work/path4",
                "/home/work/test1",
            )
    }
}
