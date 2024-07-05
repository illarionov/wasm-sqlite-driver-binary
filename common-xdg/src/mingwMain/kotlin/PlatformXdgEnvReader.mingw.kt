/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.open.helper.common.xdg

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.posix.getenv
import platform.windows.CoTaskMemFree
import platform.windows.FOLDERID_Profile
import platform.windows.PWSTRVar
import platform.windows.SHGetKnownFolderPath

internal actual val platformXdgEnvReader: PlatformXdgEnvReader = WindowsXdgEnvReader

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
internal object WindowsXdgEnvReader : PlatformXdgEnvReader {
    override fun getEnv(name: String): String? = getenv(name)?.toKString()

    override fun getUserHomeDirectory(): String? = memScoped {
        val out: PWSTRVar = this.alloc()
        val userProfile: String? = if (SHGetKnownFolderPath(
                rfid = FOLDERID_Profile.ptr,
                dwFlags = 0u,
                hToken = null,
                ppszPath = out.ptr,
            ) != 0
        ) {
            null
        } else {
            out.value?.toKString()
        }
        CoTaskMemFree(out.value)
        userProfile
    }
}
