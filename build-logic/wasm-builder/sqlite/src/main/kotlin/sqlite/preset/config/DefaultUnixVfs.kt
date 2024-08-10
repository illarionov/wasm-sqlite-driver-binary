/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.builder.sqlite.preset.config

/**
 * Sqlite Standard UNIX Filesystems
 *
 * https://www.sqlite.org/vfs.html
 * https://www.sqlite.org/src/doc/trunk/src/os_unix.c
 */
public enum class DefaultUnixVfs(public val id: String) {
    UNIX("unix"),
    UNIX_DOTFILE("unix-dotfile"),
    UNIX_EXCL("unix-excl"),
    UNIX_NONE("unix-none"),
    UNIX_NAMEDSEM("unix-namedsem"),
    ;

    public val sqliteBuildOption: String get() = """-DSQLITE_DEFAULT_UNIX_VFS="${this.id}""""
}
