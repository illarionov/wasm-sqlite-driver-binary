/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.wasm.sqlite.binary.gradle.buildinfo

import at.released.wasm.sqlite.binary.gradle.buildinfo.WasmSqliteExtendedBuildInfo.WasmSqliteCompilerSettings
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ACTUAL
import com.squareup.kotlinpoet.KModifier.EXPECT
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.provider.Provider
import java.io.File

@Suppress("TooManyFunctions")
class WasmSqliteBuildInfoGenerator(
    private val configuration: WasmSqliteBuildInfo,
) {
    private val outputObjectClassName = ClassName(
        configuration.rootPackage.get(),
        configuration.wasmSqliteBuildClassName.get(),
    )
    private val outputExtendedInfoClassName = ClassName(
        configuration.rootPackage.get(),
        configuration.wasmSqliteBuildClassName.get() + "ExtendedBuildInfo",
    )

    fun generateCommonCode(
        commonOutputDirectory: File,
    ) {
        val objectSpec: TypeSpec = TypeSpec.objectBuilder(outputObjectClassName)
            .addModifiers(PUBLIC, EXPECT)
            .addSuperinterface(WASM_SQLITE_CONFIGURATION_CLASS_NAME)
            .build()
        val configurationContent = FileSpec
            .builder(outputObjectClassName)
            .addType(objectSpec)
            .build()
        configurationContent.writeTo(commonOutputDirectory)
        generateExtendedBuildInfo(commonOutputDirectory)
    }

    private fun generateExtendedBuildInfo(
        commonOutputDirectory: File,
    ) {
        val extendedInfo: WasmSqliteExtendedBuildInfo = configuration.extendedInfo.orNull ?: return
        val objectSpec: TypeSpec = TypeSpec.objectBuilder(outputExtendedInfoClassName)
            .addModifiers(INTERNAL)
            .addSuperinterface(WASM_SQLITE_EXTENDED_BUILD_INFO_CLASS_NAME)
            .addProperty(
                PropertySpec.builder("sqliteVersion", STRING, OVERRIDE)
                    .initializer("%S", extendedInfo.sqliteVersion.get())
                    .build(),
            )
            .addProperty(
                PropertySpec.builder("emscriptenVersion", STRING, OVERRIDE)
                    .initializer("%S", extendedInfo.emscriptenVersion.get())
                    .build(),
            )
            .apply {
                val compilerSettings = extendedInfo.compilerSettings.orNull
                val compilerSettingsType = if (compilerSettings != null) {
                    WASM_SQLITE_COMPILER_SETTINGS_CLASS_NAME
                } else {
                    WASM_SQLITE_COMPILER_SETTINGS_CLASS_NAME.copy(nullable = true)
                }
                val compilerSettingsPropertyBuilder = PropertySpec.builder(
                    "compilerSettings",
                    compilerSettingsType,
                    OVERRIDE,
                )
                if (compilerSettings != null) {
                    val extendedCompilerSettings: TypeSpec = generateExtendedCompilerSettings(compilerSettings)
                    compilerSettingsPropertyBuilder.initializer("%L", extendedCompilerSettings)
                } else {
                    compilerSettingsPropertyBuilder.initializer("null")
                }
                addProperty(compilerSettingsPropertyBuilder.build())
            }
            .build()
        val extendedInfoContent = FileSpec
            .builder(outputExtendedInfoClassName)
            .addType(objectSpec)
            .build()
        extendedInfoContent.writeTo(commonOutputDirectory)
    }

    private fun generateExtendedCompilerSettings(
        compilerSettings: WasmSqliteCompilerSettings,
    ): TypeSpec {
        val extendedCompilerSettings: TypeSpec = TypeSpec.anonymousClassBuilder()
            .addSuperinterface(WASM_SQLITE_COMPILER_SETTINGS_CLASS_NAME)
            .addProperty(compilerSettings.additionalSourceFiles.toPropertySpec("additionalSourceFiles"))
            .addProperty(compilerSettings.additionalIncludes.toPropertySpec("additionalIncludes"))
            .addProperty(compilerSettings.additionalLibs.toPropertySpec("additionalLibs"))
            .addProperty(compilerSettings.codeGenerationFlags.toPropertySpec("codeGenerationFlags"))
            .addProperty(compilerSettings.codeOptimizationFlags.toPropertySpec("codeOptimizationFlags"))
            .addProperty(compilerSettings.emscriptenFlags.toPropertySpec("emscriptenFlags"))
            .addProperty(compilerSettings.exportedFunctions.toPropertySpec("exportedFunctions"))
            .addProperty(compilerSettings.sqliteFlags.toPropertySpec("sqliteFlags"))
            .build()
        return extendedCompilerSettings
    }

    private fun Provider<List<String>>.toPropertySpec(
        name: String,
    ): PropertySpec {
        val value = this.orNull
        return PropertySpec.builder(name, value.typeName(), OVERRIDE)
            .initializer(value.asListOfStringsCodeBlock())
            .build()
    }

    private fun List<String>?.typeName(): TypeName {
        val listOfStringsType = LIST.parameterizedBy(STRING)
        return if (this != null) {
            listOfStringsType
        } else {
            listOfStringsType.copy(nullable = true)
        }
    }

    private fun List<String>?.asListOfStringsCodeBlock(): CodeBlock {
        if (this == null) {
            return CodeBlock.of("null")
        }

        val argsTemplate = List(this.size) { "%S,\n" }.joinToString(prefix = "\n", separator = "")
        return CodeBlock.builder()
            .addStatement("listOf($argsTemplate)", args = this.toTypedArray())
            .build()
    }

    fun generateJvmActualCode(
        jsMainOutputDirectory: File,
    ) {
        val sqliteUrlPropertyBuilder: PropertySpec.Builder.() -> Unit = {
            this.getter(
                FunSpec.getterBuilder()
                    .addStatement(
                        "return %L(requireNotNull(%T::class.java.getResource(%S)).toString())",
                        SOURCE_URL_CLASS_NAME,
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
            this.initializer("%L(%S)", SOURCE_URL_CLASS_NAME.simpleName, wasmUrlStaticPath)
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
                PropertySpec.builder("sqliteUrl", SOURCE_URL_CLASS_NAME, OVERRIDE)
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
            .addProperty(
                PropertySpec.builder("buildInfo", WASM_SQLITE_EXTENDED_BUILD_INFO_CLASS_NAME, OVERRIDE)
                    .initializer("%T", outputExtendedInfoClassName)
                    .build(),
            )
            .build()

        return FileSpec
            .builder(outputObjectClassName)
            .addType(objectSpec)
            .build()
    }

    private companion object {
        const val SQLITE_BINARY_API_ROOT_PACKAGE = "at.released.wasm.sqlite.binary.base"
        val WASM_SQLITE_CONFIGURATION_CLASS_NAME = ClassName(SQLITE_BINARY_API_ROOT_PACKAGE, "WasmSqliteConfiguration")
        val WASM_SQLITE_EXTENDED_BUILD_INFO_CLASS_NAME =
            ClassName(SQLITE_BINARY_API_ROOT_PACKAGE, "WasmSqliteExtendedBuildInfo")
        val WASM_SQLITE_COMPILER_SETTINGS_CLASS_NAME =
            ClassName(SQLITE_BINARY_API_ROOT_PACKAGE, "WasmSqliteCompilerSettings")
        val SOURCE_URL_CLASS_NAME = ClassName("at.released.cassettes.base", "AssetUrl")

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
