/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.open.helper.common.xdg

import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import platform.posix.getenv
import platform.posix.getpwuid
import platform.posix.getuid

internal actual val platformXdgEnvReader: PlatformXdgEnvReader = LinuxXdgEnvReader

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
internal object LinuxXdgEnvReader : PlatformXdgEnvReader {
    override fun getEnv(name: String): String? = getenv(name)?.toKString()

    override fun getUserHomeDirectory(): String? {
        val uid = getuid()
        // TODO: getpwuid is thread-unsafe
        return getpwuid(uid)?.let {
            it.pointed.pw_dir!!.toKString()
        }
    }
}
