/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.builder.sqlite.preset.config

import ru.pixnews.wasm.builder.sqlite.preset.config.DefaultUnixVfs.UNIX_NONE

public object SqliteWasmConfig {
    /**
     * Build configuration from sqlite3-wasm
     * https://sqlite.org/src/file?name=ext/wasm/GNUmakefile&ci=trunk
     */
    public fun wasmConfig(
        defaultUnixVfs: DefaultUnixVfs = UNIX_NONE,
    ): List<String> = listOf(
        defaultUnixVfs.sqliteBuildOption,
        "-DSQLITE_ENABLE_BYTECODE_VTAB",
        "-DSQLITE_ENABLE_DBPAGE_VTAB",
        "-DSQLITE_ENABLE_DBSTAT_VTAB",
        "-DSQLITE_ENABLE_EXPLAIN_COMMENTS",
        "-DSQLITE_ENABLE_FTS5",
        "-DSQLITE_ENABLE_OFFSET_SQL_FUNC",
        "-DSQLITE_ENABLE_RTREE",
        "-DSQLITE_ENABLE_STMTVTAB",
        "-DSQLITE_ENABLE_UNKNOWN_SQL_FUNCTION",
        "-DSQLITE_OMIT_DEPRECATED",
        "-DSQLITE_OMIT_LOAD_EXTENSION",
        "-DSQLITE_OMIT_SHARED_CACHE",
        "-DSQLITE_OMIT_UTF16",
        "-DSQLITE_OS_KV_OPTIONAL=1",
        "-DSQLITE_TEMP_STORE=2",
        "-DSQLITE_THREADSAFE=0",
        "-DSQLITE_USE_URI=1",
        "-DSQLITE_WASM_ENABLE_C_TESTS",
    )
}
