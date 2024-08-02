/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage", "GENERIC_VARIABLE_WRONG_DECLARATION")

package ru.pixnews.wasm.builder.sqlite

import org.gradle.api.artifacts.type.ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE
import org.gradle.api.artifacts.type.ArtifactTypeDefinition.DIRECTORY_TYPE
import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Category.LIBRARY
import org.gradle.api.attributes.LibraryElements.HEADERS_CPLUSPLUS
import org.gradle.api.attributes.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE
import org.gradle.api.attributes.LibraryElements.LINK_ARCHIVE
import org.gradle.api.attributes.Usage.C_PLUS_PLUS_API
import org.gradle.api.attributes.Usage.NATIVE_LINK
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.language.cpp.CppBinary.DEBUGGABLE_ATTRIBUTE
import org.gradle.language.cpp.CppBinary.LINKAGE_ATTRIBUTE
import org.gradle.language.cpp.CppBinary.OPTIMIZED_ATTRIBUTE
import org.gradle.nativeplatform.Linkage.STATIC
import org.gradle.nativeplatform.MachineArchitecture.ARCHITECTURE_ATTRIBUTE
import org.gradle.nativeplatform.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE
import ru.pixnews.wasm.builder.base.emscripten.EMSCRIPTEN_USE_PTHREADS_ATTRIBUTE
import ru.pixnews.wasm.builder.base.emscripten.emscriptenOperatingSystem
import ru.pixnews.wasm.builder.base.emscripten.wasm32Architecture
import ru.pixnews.wasm.builder.base.emscripten.wasmBinaryLibraryElements
import ru.pixnews.wasm.builder.base.emscripten.wasmRuntimeUsage
import ru.pixnews.wasm.builder.base.ext.capitalizeAscii
import ru.pixnews.wasm.builder.sqlite.internal.BuildDirPath.STRIPPED_RESULT_DIR
import ru.pixnews.wasm.builder.sqlite.internal.BuildDirPath.compileUnstrippedResultDir
import ru.pixnews.wasm.builder.sqlite.internal.SqliteAdditionalArgumentProvider
import ru.pixnews.wasm.builder.sqlite.internal.createSqliteSourceConfiguration
import ru.pixnews.wasm.builder.sqlite.internal.setupUnpackingSqliteAttributes

// Convention Plugin for building Sqlite WebAssembly using Emscripten
plugins {
    base
}

setupUnpackingSqliteAttributes(
    androidSqlitePatchFile = project.layout.projectDirectory.file(
        provider { "../sqlite-android-common/android/Android.patch" },
    ),
)

configurations {
    dependencyScope("wasmLibraries")

    consumable("wasmSqliteReleaseElements") {
        attributes {
            attribute(USAGE_ATTRIBUTE, objects.wasmRuntimeUsage)
            attribute(CATEGORY_ATTRIBUTE, objects.named(LIBRARY))
            attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.wasmBinaryLibraryElements)

            attribute(ARCHITECTURE_ATTRIBUTE, objects.wasm32Architecture)
            attribute(OPERATING_SYSTEM_ATTRIBUTE, objects.emscriptenOperatingSystem)

            attribute(DEBUGGABLE_ATTRIBUTE, false)
            attribute(OPTIMIZED_ATTRIBUTE, true)
            attribute(EMSCRIPTEN_USE_PTHREADS_ATTRIBUTE, true)
        }
    }
    consumable("wasmSqliteDebugElements") {
        attributes {
            attribute(USAGE_ATTRIBUTE, objects.wasmRuntimeUsage)
            attribute(CATEGORY_ATTRIBUTE, objects.named(LIBRARY))
            attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.wasmBinaryLibraryElements)

            attribute(ARCHITECTURE_ATTRIBUTE, objects.wasm32Architecture)
            attribute(OPERATING_SYSTEM_ATTRIBUTE, objects.emscriptenOperatingSystem)

            attribute(DEBUGGABLE_ATTRIBUTE, true)
            attribute(OPTIMIZED_ATTRIBUTE, true)
            attribute(EMSCRIPTEN_USE_PTHREADS_ATTRIBUTE, true)
        }
    }
    resolvable("wasmStaticLibrariesClasspath") {
        description = "Static libraries from included libraries used to link Sqlite"
        extendsFrom(configurations["wasmLibraries"])
        attributes {
            attribute(USAGE_ATTRIBUTE, objects.named(NATIVE_LINK))
            attribute(CATEGORY_ATTRIBUTE, objects.named(LIBRARY))
            attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LINK_ARCHIVE))
            attribute(ARTIFACT_TYPE_ATTRIBUTE, DIRECTORY_TYPE)
            attribute(LINKAGE_ATTRIBUTE, STATIC)

            attribute(ARCHITECTURE_ATTRIBUTE, objects.wasm32Architecture)
            attribute(OPERATING_SYSTEM_ATTRIBUTE, objects.emscriptenOperatingSystem)
        }
    }
    resolvable("wasmHeadersClasspath") {
        description = "Header files from included WebAssembly libraries used to compile SQLite"
        extendsFrom(configurations["wasmLibraries"])
        attributes {
            attribute(USAGE_ATTRIBUTE, objects.named(C_PLUS_PLUS_API))
            attribute(CATEGORY_ATTRIBUTE, objects.named(LIBRARY))
            attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(HEADERS_CPLUSPLUS))
            attribute(ARTIFACT_TYPE_ATTRIBUTE, DIRECTORY_TYPE)
            attribute(LINKAGE_ATTRIBUTE, STATIC)

            attribute(OPERATING_SYSTEM_ATTRIBUTE, objects.emscriptenOperatingSystem)
            attribute(ARCHITECTURE_ATTRIBUTE, objects.wasm32Architecture)
        }
    }
}

private val sqliteExtension = extensions.create(
    "sqlite3Build",
    SqliteWasmBuilderExtension::class.java,
    provider {
        versionCatalogs.named("libs").findVersion("emscripten").get().toString()
    },
)

afterEvaluate {
    sqliteExtension.builds.configureEach {
        setupTasksForBuild(this, sqliteExtension.emscriptenVersion)
    }
}

private fun setupTasksForBuild(
    buildSpec: SqliteWasmBuildSpec,
    emscriptenVersion: Provider<String>,
) {
    val sqlite3c: FileCollection = if (buildSpec.sqlite3Source.isEmpty) {
        createSqliteSourceConfiguration(buildSpec.sqliteVersion)
    } else {
        buildSpec.sqlite3Source
    }
    val unstrippedWasmFileName = buildSpec.wasmUnstrippedFileName
    val unstrippedJsFileName = unstrippedWasmFileName.map { it.substringBeforeLast(".wasm") + ".mjs" }
    val strippedWasmFileName = buildSpec.wasmFileName
    val buildName = buildSpec.name.capitalizeAscii()

    val buildSqliteTask = buildSpec.buildTask
    buildSqliteTask.configure {
        val sqlite3cFile = sqlite3c.elements.map { it.first().asFile }

        group = "Build"
        description = "Compiles SQLite `$buildName` for Wasm"
        sourceFiles.from(buildSpec.additionalSourceFiles)
        outputFileName = unstrippedJsFileName
        outputDirectory = layout.buildDirectory.dir(compileUnstrippedResultDir(buildName))
        emscriptenSdk.emccVersion = emscriptenVersion
        includes.setFrom(
            sqlite3cFile.map { it.parentFile },
            buildSpec.additionalIncludes,
        )
        libs.setFrom(buildSpec.additionalLibs)

        val additionalArgsProvider = SqliteAdditionalArgumentProvider(
            sqlite3cFile,
            codeGenerationOptions = buildSpec.codeGenerationFlags,
            codeOptimizationOptions = buildSpec.codeOptimizationFlags,
            emscriptenConfigurationOptions = buildSpec.emscriptenFlags,
            exportedFunctions = buildSpec.exportedFunctions,
            sqliteConfigOptions = buildSpec.sqliteFlags,
        )
        additionalArgumentProviders.add(additionalArgsProvider)
    }

    val stripSqliteTask = buildSpec.stripTask
    stripSqliteTask.configure {
        group = "Build"
        description = "Strip compiled SQLite `$buildName` Wasm binary"
        source = buildSqliteTask.flatMap { it.outputDirectory.file(unstrippedWasmFileName.get()) }
        val dstDir = layout.buildDirectory.dir(STRIPPED_RESULT_DIR)
        destination = dstDir.zip(strippedWasmFileName) { dir, name -> dir.file(name) }
        doFirst {
            dstDir.get().asFile.let { dir ->
                dir.walkBottomUp()
                    .filter { it != dir }
                    .forEach(File::delete)
            }
        }
    }

    setupOutgoingArtifacts(
        buildSpec.strippedWasmOutput,
        buildSpec.unstrippedWasmOutput,
    )

    tasks.named("assemble").configure {
        dependsOn(stripSqliteTask)
    }
}

private fun setupOutgoingArtifacts(
    strippedWasm: Provider<RegularFile>,
    unstrippedWasm: Provider<RegularFile>,
) {
    configurations.named("wasmSqliteDebugElements").get().outgoing {
        artifact(unstrippedWasm)
    }
    configurations.named("wasmSqliteReleaseElements").get().outgoing {
        artifact(strippedWasm)
    }
}
