/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.wasm.sqlite.binary.base

public interface WasmSqliteExtendedBuildInfo {
    public val sqliteVersion: String
    public val emscriptenVersion: String
    public val compilerSettings: WasmSqliteCompilerSettings?
}
