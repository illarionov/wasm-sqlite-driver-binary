/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.gradle.chicory.aot

import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasm.WasmModule
import com.dylibso.chicory.wasm.WasmWriter
import com.dylibso.chicory.wasm.WasmWriter.writeVarUInt32
import com.dylibso.chicory.wasm.types.OpCode
import com.dylibso.chicory.wasm.types.RawSection
import com.dylibso.chicory.wasm.types.Section
import com.dylibso.chicory.wasm.types.SectionId
import java.io.ByteArrayOutputStream

internal fun generateWasmMeta(wasmBytes: ByteArray, module: WasmModule): ByteArray {
    val writer = WasmWriter()
    Parser.parseWithoutDecoding(wasmBytes) { section: Section ->
        when (section.sectionId()) {
            SectionId.CODE -> {
                val newCode = ByteArrayOutputStream().use { out ->
                    val count = module.codeSection().functionBodyCount()
                    writeVarUInt32(out, count)
                    repeat(count) {
                        writeVarUInt32(out, 3) // function size in bytes
                        writeVarUInt32(out, 0) // locals count
                        out.write(OpCode.UNREACHABLE.opcode())
                        out.write(OpCode.END.opcode())
                    }
                    out.toByteArray()
                }
                writer.writeSection(SectionId.CODE, newCode)
            }

            SectionId.CUSTOM -> Unit
            else -> writer.writeSection(section as RawSection)
        }
    }
    return writer.bytes()
}
