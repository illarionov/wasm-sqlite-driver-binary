/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.buildinfo

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ACTUAL
import com.squareup.kotlinpoet.KModifier.EXPECT
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File

class WasmSqliteBuildInfoGenerator(
    private val configuration: WasmSqliteBuildInfo,
) {
    private val outputObjectClassName = ClassName(
        configuration.rootPackage.get(),
        configuration.wasmSqliteBuildClassName.get(),
    )

    fun generateCommonCode(
        commonOutputDirectory: File,
    ) {
        val objectSpec: TypeSpec = TypeSpec.objectBuilder(outputObjectClassName)
            .addModifiers(PUBLIC, EXPECT)
            .addSuperinterface(WASM_SQLITE_CONFIGURATION_CLASS_NAME)
            .build()
        val fileContent = FileSpec
            .builder(outputObjectClassName)
            .addType(objectSpec)
            .build()
        fileContent.writeTo(commonOutputDirectory)
    }

    fun generateJvmActualCode(
        jsMainOutputDirectory: File,
    ) {
        val sqliteUrlPropertyBuilder: PropertySpec.Builder.() -> Unit = {
            this.getter(
                FunSpec.getterBuilder()
                    .addStatement(
                        "return %L(requireNotNull(%L::class.java.getResource(%S)).toString())",
                        WASM_SOURCE_URL_CLASS_NAME.simpleName,
                        outputObjectClassName,
                        configuration.wasmFileName.get(),
                    )
                    .build(),
            )
        }
        val fileContent = generateActualObject(sqliteUrlPropertyBuilder)
        fileContent.writeTo(jsMainOutputDirectory)
    }

    public fun generateAndroidActualCode(
        androidMainOutputDirectory: File,
    ) {
        val wasmSubdir = configuration.wasmFileSubdir.get().dropLeadingSlash().dropTrailingSlash()
        val path = buildString {
            append("file:///android_asset/")
            append(wasmSubdir)
            append('/')
            append(configuration.wasmFileName.get().dropLeadingSlash())
        }
        generateActualCodeWithStaticPath(androidMainOutputDirectory, path)
    }

    public fun generateNativeOrJsActualCode(
        outputDirectory: File,
    ) {
        val path = buildString {
            append(configuration.wasmFileSubdir.get().dropTrailingSlash())
            append('/')
            append(configuration.wasmFileName.get())
        }
        generateActualCodeWithStaticPath(outputDirectory, path)
    }

    private fun generateActualCodeWithStaticPath(
        outputDirectory: File,
        wasmUrlStaticPath: String,
    ) {
        val sqliteUrlPropertyBuilder: PropertySpec.Builder.() -> Unit = {
            this.initializer("%L(%S)", WASM_SOURCE_URL_CLASS_NAME.simpleName, wasmUrlStaticPath)
        }
        val fileContent = generateActualObject(sqliteUrlPropertyBuilder)
        fileContent.writeTo(outputDirectory)
    }

    private fun generateActualObject(
        sqliteUrlPropertyBuilder: PropertySpec.Builder.() -> Unit,
    ): FileSpec {
        val objectSpec: TypeSpec = TypeSpec.objectBuilder(outputObjectClassName)
            .addModifiers(PUBLIC, ACTUAL)
            .addSuperinterface(WASM_SQLITE_CONFIGURATION_CLASS_NAME)
            .addProperty(
                PropertySpec.builder("sqliteUrl", WASM_SOURCE_URL_CLASS_NAME, OVERRIDE)
                    .apply(sqliteUrlPropertyBuilder)
                    .build(),
            )
            .addProperty(
                PropertySpec.builder("wasmMinMemorySize", LONG, OVERRIDE)
                    .initializer("%L", configuration.minMemorySize.get())
                    .build(),
            )
            .addProperty(
                PropertySpec.builder("requireThreads", BOOLEAN, OVERRIDE)
                    .initializer("%L", configuration.requireThreads.get())
                    .build(),
            )
            .build()

        return FileSpec
            .builder(outputObjectClassName)
            .addType(objectSpec)
            .build()
    }

    private companion object {
        const val SQLITE_BINARY_API_ROOT_PACKAGE = "ru.pixnews.wasm.sqlite.binary.base"
        val WASM_SQLITE_CONFIGURATION_CLASS_NAME = ClassName(SQLITE_BINARY_API_ROOT_PACKAGE, "WasmSqliteConfiguration")
        val WASM_SOURCE_URL_CLASS_NAME = ClassName(SQLITE_BINARY_API_ROOT_PACKAGE, "WasmSourceUrl")

        private fun String.dropTrailingSlash() = if (this.endsWith("/")) {
            this.dropLast(1)
        } else {
            this
        }

        private fun String.dropLeadingSlash() = if (this.startsWith("/")) {
            this.drop(1)
        } else {
            this
        }
    }
}
