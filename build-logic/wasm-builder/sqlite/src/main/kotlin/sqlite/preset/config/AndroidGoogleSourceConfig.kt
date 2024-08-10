/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.builder.sqlite.preset.config

/**
 * Build configurations from Android port of SQLite (The Android Open Source Project)
 * https://android.googlesource.com/platform/external/sqlite
 * 3cfcc6cc2e54ab58ed8114194d54da5ac1ab16b2 (2024-03-25)
 */
public object AndroidGoogleSourceConfig {
    public val sqliteMinimalDefaults: List<String> = listOf(
        "-DBIONIC_IOCTL_NO_SIGNEDNESS_OVERLOAD",
        "-DHAVE_USLEEP=1",
        "-DNDEBUG=1",
        "-DSQLITE_ALLOW_ROWID_IN_VIEW",
        "-DSQLITE_DEFAULT_AUTOVACUUM=1",
        "-DSQLITE_DEFAULT_FILE_FORMAT=4",
        "-DSQLITE_DEFAULT_FILE_PERMISSIONS=0600",
        "-DSQLITE_DEFAULT_JOURNAL_SIZE_LIMIT=1048576",
        "-DSQLITE_DEFAULT_LEGACY_ALTER_TABLE",
        "-DSQLITE_ENABLE_BATCH_ATOMIC_WRITE",
        "-DSQLITE_ENABLE_BYTECODE_VTAB",
        "-DSQLITE_ENABLE_FTS3",
        "-DSQLITE_ENABLE_FTS3_BACKWARDS",
        "-DSQLITE_ENABLE_FTS4",
        "-DSQLITE_ENABLE_MEMORY_MANAGEMENT=1",
        "-DSQLITE_HAVE_ISNAN",
        "-DSQLITE_OMIT_BUILTIN_TEST",
        "-DSQLITE_OMIT_COMPILEOPTION_DIAGS",
        "-DSQLITE_OMIT_LOAD_EXTENSION",
        "-DSQLITE_POWERSAFE_OVERWRITE=1",
        "-DSQLITE_SECURE_DELETE",
        "-DSQLITE_TEMP_STORE=3",
        "-DSQLITE_THREADSAFE=2",
        "-Werror",
        "-Wno-unused-parameter",

        // Default value causes sqlite3_open_v2 to return error if DB is missing.
        "-ftrivial-auto-var-init=pattern",
    )
    public val sqliteDefaults: List<String> = sqliteMinimalDefaults + listOf(
        "-DUSE_PREAD64",
        "-Dfdatasync=fdatasync",
        "-DHAVE_MALLOC_H=1",
        "-DHAVE_MALLOC_USABLE_SIZE",
        "-DSQLITE_ENABLE_DBSTAT_VTAB",
    )
    public val androidIcu: List<String> = sqliteDefaults + "-DSQLITE_ENABLE_ICU"
}
