/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.builder.sqlite.preset.config

public object RequeryAndroidConfig {
    /**
     * Build configuration from https://github.com/requery/sqlite-android.git
     */
    public val sqliteAndroid: List<String> = AndroidGoogleSourceConfig.sqliteMinimalDefaults - setOf(
        "-DBIONIC_IOCTL_NO_SIGNEDNESS_OVERLOAD",
        "-DSQLITE_ALLOW_ROWID_IN_VIEW",
        "-DSQLITE_DEFAULT_LEGACY_ALTER_TABLE",
        "-DSQLITE_ENABLE_BYTECODE_VTAB",
        "-DSQLITE_ENABLE_FTS3_BACKWARDS",
        "-DSQLITE_OMIT_BUILTIN_TEST",
        "-DSQLITE_OMIT_LOAD_EXTENSION",
        "-DSQLITE_SECURE_DELETE",
        "-ftrivial-auto-var-init=pattern",
        "-Werror",
        "-Wno-unused-parameter",
    ) + setOf(
        "-DSQLITE_DEFAULT_MEMSTATUS=0",
        "-DSQLITE_ENABLE_FTS3_PARENTHESIS",
        "-DSQLITE_ENABLE_FTS4_PARENTHESIS",
        "-DSQLITE_ENABLE_FTS5",
        "-DSQLITE_ENABLE_FTS5_PARENTHESIS",
        "-DSQLITE_ENABLE_JSON1",
        "-DSQLITE_ENABLE_RTREE=1",
        "-DSQLITE_MAX_EXPR_DEPTH=0",
        "-DSQLITE_UNTESTABLE",
        "-DSQLITE_USE_ALLOCA",
        "-O3",
    )
}
