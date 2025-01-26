/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.builder.sqlite.preset.config

public object OpenHelperConfig {
    public fun getBuildFlags(
        enableIcu: Boolean = true,
        enableMultithreading: Boolean = true,
        defaultUnixVfs: DefaultUnixVfs = DefaultUnixVfs.UNIX_EXCL,
    ): List<String> = buildList {
        // Base config
        if (enableIcu) {
            addAll(AndroidGoogleSourceConfig.androidIcu)
        } else {
            addAll(AndroidGoogleSourceConfig.sqliteDefaults)
        }

        // WASM environment adjustments
        remove("-DUSE_PREAD64")
        remove("-Werror")

        // Multithreading adjustments
        if (!enableMultithreading) {
            remove("-DSQLITE_THREADSAFE=2")
            add("-DSQLITE_THREADSAFE=0")
        }

        // Do not create threads from Sqlite native code
        add("-DSQLITE_MAX_WORKER_THREADS=0")

        // Mmap is not implemented in the WASM embedders
        add("-DSQLITE_MAX_MMAP_SIZE=0")

        // Default file system
        add(defaultUnixVfs.sqliteBuildOption)

        // Additional features from sqlite-webassembly config
        addAll(
            listOf(
                "-DSQLITE_ENABLE_EXPLAIN_COMMENTS",
                "-DSQLITE_ENABLE_MATH_FUNCTIONS",
                "-DSQLITE_ENABLE_OFFSET_SQL_FUNC",
                "-DSQLITE_ENABLE_PREUPDATE_HOOK", // required by session extension
                "-DSQLITE_ENABLE_SESSION",
            ),
        )

        // Additional features
        addAll(
            listOf(
                "-DSQLITE_ENABLE_FTS3_PARENTHESIS",
                "-DSQLITE_ENABLE_FTS5",
                "-DSQLITE_ENABLE_JSON1",
                "-DSQLITE_ENABLE_RTREE",
                "-DSQLITE_ENABLE_STMTVTAB",
            ),
        )

        // Some additional features from the `androidx.sqlite:sqlite-bundled` configuration
        addAll(
            listOf(
                "-DSQLITE_DEFAULT_MEMSTATUS=0",
                "-DSQLITE_ENABLE_RBU",
                "-DSQLITE_ENABLE_STAT4",
                // The following parameters are not added since they are not used:
                // -DSQLITE_ENABLE_COLUMN_METADATA,
                // -DSQLITE_ENABLE_LOAD_EXTENSION,
                // -DSQLITE_ENABLE_NORMALIZE,
                // -DSQLITE_OMIT_PROGRESS_CALLBACK is not added since `sqlite3_progress_handler` is used in open-helper.
            ),
        )

        // Other
        addAll(
            listOf(
                "-DSQLITE_ENABLE_DBPAGE_VTAB",
                "-DSQLITE_OMIT_DEPRECATED",
                "-DSQLITE_OMIT_SHARED_CACHE",
                "-DSQLITE_WASM_ENABLE_C_TESTS",
            ),
        )
    }
}
