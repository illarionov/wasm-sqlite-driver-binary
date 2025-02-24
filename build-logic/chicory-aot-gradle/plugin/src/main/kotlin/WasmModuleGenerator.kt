/*
 * Copyright 2025, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.gradle.chicory.aot

import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.runtime.Machine
import com.dylibso.chicory.wasm.Parser
import com.dylibso.chicory.wasm.WasmModule
import com.github.javaparser.StaticJavaParser.parseClassOrInterfaceType
import com.github.javaparser.StaticJavaParser.parseType
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Modifier.Keyword.FINAL
import com.github.javaparser.ast.Modifier.Keyword.PRIVATE
import com.github.javaparser.ast.Modifier.Keyword.PUBLIC
import com.github.javaparser.ast.Modifier.Keyword.STATIC
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.NodeList.nodeList
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.Parameter
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.ClassExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.expr.StringLiteralExpr
import com.github.javaparser.ast.expr.VariableDeclarationExpr
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.CatchClause
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.ast.stmt.ThrowStmt
import com.github.javaparser.ast.stmt.TryStmt
import com.github.javaparser.utils.SourceRoot
import java.io.IOException
import java.io.InputStream
import java.io.UncheckedIOException
import java.nio.file.Path

fun writeModuleClass(
    rootPackage: String,
    moduleClassSimpleName: String,
    machineClassSimpleName: String,
    wasmMetaSimpleName: String,
    destination: Path,
) {
    val compilationUnit = generateModuleClass(
        rootPackage,
        moduleClassSimpleName,
        machineClassSimpleName,
        wasmMetaSimpleName,
    )
    val dest = SourceRoot(destination)
    dest.add(rootPackage, "$moduleClassSimpleName.java", compilationUnit)
    dest.saveAll()
}

fun generateModuleClass(
    dstPackage: String,
    moduleClassSimpleName: String,
    machineClassSimpleName: String,
    wasmMetaSimpleSimpleName: String,
): CompilationUnit {
    return CompilationUnit(dstPackage).apply {
        addClass(moduleClassSimpleName, PUBLIC, FINAL).also { type: ClassOrInterfaceDeclaration ->
            type.addConstructor(PRIVATE).createBody()
            generateCreateMethod(type, machineClassSimpleName)
            generateLoadMethod(type, moduleClassSimpleName, wasmMetaSimpleSimpleName)
        }
    }
}

private fun CompilationUnit.generateCreateMethod(
    type: ClassOrInterfaceDeclaration,
    machineClassSimpleName: String,
) {
    addImport(Instance::class.java)
    addImport(Machine::class.java)

    val method = type.addMethod("create", PUBLIC, STATIC)
        .addParameter(parseType("Instance"), "instance")
        .setType(Machine::class.java)
        .createBody()

    val constructorInvocation = ObjectCreationExpr(
        null,
        parseClassOrInterfaceType(machineClassSimpleName),
        nodeList(NameExpr("instance")),
    )
    method.addStatement(ReturnStmt(constructorInvocation))
}

private fun CompilationUnit.generateLoadMethod(
    type: ClassOrInterfaceDeclaration,
    modulePackage: String,
    wasmName: String,
) {
    addImport(IOException::class.java)
    addImport(UncheckedIOException::class.java)
    addImport(Parser::class.java)
    addImport(WasmModule::class.java)
    addImport(InputStream::class.java)

    val getResource = MethodCallExpr(
        ClassExpr(parseType(modulePackage)),
        "getResourceAsStream",
        NodeList(StringLiteralExpr(wasmName)),
    )
    val resourceVar = VariableDeclarationExpr(
        VariableDeclarator(parseType("InputStream"), "in", getResource),
    )

    val returnStmt = ReturnStmt(
        MethodCallExpr().setScope(NameExpr("Parser")).setName("parse").addArgument(NameExpr("in")),
    )

    val newException: ObjectCreationExpr =
        ObjectCreationExpr()
            .setType(parseClassOrInterfaceType("UncheckedIOException"))
            .addArgument(StringLiteralExpr("Failed to load AOT WASM module"))
            .addArgument(NameExpr("e"))
    val catchIoException = CatchClause()
        .setParameter(Parameter(parseClassOrInterfaceType("IOException"), "e"))
        .setBody(BlockStmt(NodeList(ThrowStmt(newException))))

    val loadMethod = type.addMethod("load", PUBLIC, STATIC).setType(WasmModule::class.java)
    loadMethod.createBody().apply {
        addStatement(
            TryStmt()
                .setResources(NodeList(resourceVar))
                .setTryBlock(BlockStmt(NodeList(returnStmt)))
                .setCatchClauses(NodeList(catchIoException)),
        )
    }
}
