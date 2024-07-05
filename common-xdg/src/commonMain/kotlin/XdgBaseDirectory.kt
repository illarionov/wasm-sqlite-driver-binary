/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.open.helper.common.xdg

import okio.Path
import okio.Path.Companion.toPath

/**
 * Simple helper for getting base directories according to the [XDG Base Directory Specification](https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html)
 */
public interface XdgBaseDirectory {
    public fun getBaseDataDirectories(): List<Path>

    public companion object : XdgBaseDirectory by DefaultXdgBaseDirectory()
}

private class DefaultXdgBaseDirectory(
    private val envReader: PlatformXdgEnvReader = platformXdgEnvReader,
) : XdgBaseDirectory {
    override fun getBaseDataDirectories(): List<Path> = buildSet {
        getXdgDataHome()?.let { add(it) }
        addAll(getXdgDataDirs())
    }.toList()

    fun getXdgDataHome(): Path? {
        val dataHome = envReader.getEnv("XDG_DATA_HOME").toAbsolutePathOrNull()
        return dataHome ?: getUserHome()?.resolve(".local/share")
    }

    fun getUserHome(): Path? {
        val envUserHome = envReader.getEnv("HOME").toAbsolutePathOrNull()
        if (envUserHome != null) {
            return envUserHome
        }
        return envReader.getUserHomeDirectory().toAbsolutePathOrNull()
    }

    fun getXdgDataDirs(): List<Path> {
        val xdgDataDirs = envReader.getEnv("XDG_DATA_DIRS")
        if (xdgDataDirs.isNullOrBlank()) {
            return DEFAULT_XDG_DATA_DIRS
        }
        return xdgDataDirs.splitToSequence(':')
            .mapNotNull { it.toAbsolutePathOrNull() }
            .toList()
    }

    private companion object {
        private val DEFAULT_XDG_DATA_DIRS = listOf(
            "/usr/local/share/".toPath(),
            "/usr/share/".toPath(),
        )

        private fun String?.toAbsolutePathOrNull() = this
            ?.ifBlank { null }
            ?.toPath()
            ?.takeIf(Path::isAbsolute)
    }
}
