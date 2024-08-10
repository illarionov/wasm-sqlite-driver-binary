/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.builder.sqlite.preset.config

public object AndroidSqliteDriverBundledConfig {
    /**
     * Build configurations from androidx.sqlite:sqlite-bundled for reference
     *
     * https://github.com/androidx/androidx/blob/07076aa00829f374a559c3cd9dd07e1aeb8cabd0/sqlite/sqlite-bundled/build.gradle
     *
     * ce79d6f71d1e706f11e34c6ea7ceb424813ada1b (20224-05-15)
     */
    public val sqlite: List<String> = listOf(
        "-DHAVE_USLEEP=1",
        "-DSQLITE_DEFAULT_MEMSTATUS=0",
        "-DSQLITE_ENABLE_COLUMN_METADATA=1",
        "-DSQLITE_ENABLE_FTS3=1",
        "-DSQLITE_ENABLE_FTS3_PARENTHESIS=1",
        "-DSQLITE_ENABLE_FTS4=1",
        "-DSQLITE_ENABLE_FTS5=1",
        "-DSQLITE_ENABLE_JSON1=1",
        "-DSQLITE_ENABLE_LOAD_EXTENSION=1",
        "-DSQLITE_ENABLE_NORMALIZE=1",
        "-DSQLITE_ENABLE_RBU=1",
        "-DSQLITE_ENABLE_RTREE=1",
        "-DSQLITE_ENABLE_STAT4=1",
        "-DSQLITE_OMIT_PROGRESS_CALLBACK=0",
        "-DSQLITE_THREADSAFE=2",
    )
}
