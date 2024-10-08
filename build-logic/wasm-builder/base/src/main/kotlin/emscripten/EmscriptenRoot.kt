/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.builder.base.emscripten

import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import java.io.File

public fun ProviderFactory.defaultEmscriptenRoot(): Provider<File> = this
    .environmentVariable("EMSDK")
    .orElse(this.gradleProperty("emsdkRoot"))
    .map(::File)
