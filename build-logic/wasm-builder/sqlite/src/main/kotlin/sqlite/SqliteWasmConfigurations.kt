/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.builder.sqlite

import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.ConsumableConfiguration
import org.gradle.api.artifacts.DependencyScopeConfiguration
import org.gradle.api.artifacts.ResolvableConfiguration
import org.gradle.api.artifacts.type.ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE
import org.gradle.api.artifacts.type.ArtifactTypeDefinition.DIRECTORY_TYPE
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Category.LIBRARY
import org.gradle.api.attributes.LibraryElements.HEADERS_CPLUSPLUS
import org.gradle.api.attributes.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE
import org.gradle.api.attributes.LibraryElements.LINK_ARCHIVE
import org.gradle.api.attributes.Usage.C_PLUS_PLUS_API
import org.gradle.api.attributes.Usage.NATIVE_LINK
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.named
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

public object SqliteWasmConfigurations {
    public const val WASM_LIBRARIES: String = "wasmLibraries"
    public const val WASM_STATIC_LIBRARIES_CLASSPATH: String = "wasmStaticLibrariesClasspath"
    public const val WASM_HEADERS_CLASSPATH: String = "wasmHeadersClasspath"
    public const val WASM_SQLITE_RELEASE_ELEMENTS: String = "wasmSqliteReleaseElements"
    public const val WASM_SQLITE_DEBUG_ELEMENTS: String = "wasmSqliteDebugElements"
    public const val WASM_SQLITE_EMSCRIPTEN_ARCHIVE_ELEMENTS: String = "wasmSqliteEmscriptenArchiveElements"
    public const val CATEGORY_EMSCRIPTEN_RELEASE_ARCHIVE: String = "emscripten-release-archive"

    @Suppress("UnstableApiUsage")
    internal class Factory(
        private val objects: ObjectFactory,
        private val configurations: ConfigurationContainer,
    ) {
        fun build(): Configurations {
            val wasmLibraries = createDependencyScopeWasmLibraries().get()

            return Configurations(
                wasmReleaseElements = createWasmSqliteReleaseElements().get(),
                wasmDebugElements = createWasmSqliteDebugElements().get(),
                wasmSqliteEmscriptenArchiveElements = createWasmSqliteEmscriptenArchiveElements().get(),
                wasmLibraries = wasmLibraries,
                wasmStaticLibraries = createWasmStaticLibrariesClasspath(wasmLibraries).get(),
                wasmHeaders = createWasmHeadersClasspath(wasmLibraries).get(),
            )
        }

        private fun createDependencyScopeWasmLibraries() = configurations.dependencyScope(WASM_LIBRARIES)

        private fun createWasmSqliteReleaseElements() = configurations.consumable(WASM_SQLITE_RELEASE_ELEMENTS) {
            attributes.configureConsumableAttributes(
                debuggable = false,
                optimized = true,
                usePthreads = true,
            )
        }

        private fun createWasmSqliteDebugElements() = configurations.consumable(WASM_SQLITE_DEBUG_ELEMENTS) {
            attributes.configureConsumableAttributes(
                debuggable = true,
                optimized = true,
                usePthreads = true,
            )
        }

        private fun createWasmSqliteEmscriptenArchiveElements() =
            configurations.consumable(WASM_SQLITE_EMSCRIPTEN_ARCHIVE_ELEMENTS) {
                attributes {
                    attribute(USAGE_ATTRIBUTE, objects.wasmRuntimeUsage)
                    attribute(CATEGORY_ATTRIBUTE, objects.named(CATEGORY_EMSCRIPTEN_RELEASE_ARCHIVE))
                    setEmscriptenOs()
                }
            }

        private fun createWasmStaticLibrariesClasspath(
            wasmLibraries: DependencyScopeConfiguration,
        ) = configurations.resolvable(WASM_STATIC_LIBRARIES_CLASSPATH) {
            description = "Static libraries from included libraries used to link Sqlite"
            extendsFrom(wasmLibraries)
            attributes {
                attribute(USAGE_ATTRIBUTE, objects.named(NATIVE_LINK))
                attribute(CATEGORY_ATTRIBUTE, objects.named(LIBRARY))
                attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LINK_ARCHIVE))
                attribute(ARTIFACT_TYPE_ATTRIBUTE, DIRECTORY_TYPE)
                attribute(LINKAGE_ATTRIBUTE, STATIC)
                setEmscriptenOs()
            }
        }

        private fun createWasmHeadersClasspath(
            wasmLibraries: DependencyScopeConfiguration,
        ) = configurations.resolvable("wasmHeadersClasspath") {
            description = "Header files from included WebAssembly libraries used to compile SQLite"
            extendsFrom(wasmLibraries)
            attributes {
                attribute(USAGE_ATTRIBUTE, objects.named(C_PLUS_PLUS_API))
                attribute(CATEGORY_ATTRIBUTE, objects.named(LIBRARY))
                attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(HEADERS_CPLUSPLUS))
                attribute(ARTIFACT_TYPE_ATTRIBUTE, DIRECTORY_TYPE)
                attribute(LINKAGE_ATTRIBUTE, STATIC)
                setEmscriptenOs()
            }
        }

        private fun AttributeContainer.configureConsumableAttributes(
            debuggable: Boolean = false,
            optimized: Boolean = true,
            usePthreads: Boolean = false,
        ) {
            attribute(USAGE_ATTRIBUTE, objects.wasmRuntimeUsage)
            attribute(CATEGORY_ATTRIBUTE, objects.named(LIBRARY))
            attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.wasmBinaryLibraryElements)

            setEmscriptenOs()
            attribute(DEBUGGABLE_ATTRIBUTE, debuggable)
            attribute(OPTIMIZED_ATTRIBUTE, optimized)
            attribute(EMSCRIPTEN_USE_PTHREADS_ATTRIBUTE, usePthreads)
        }

        private fun AttributeContainer.setEmscriptenOs() {
            attribute(ARCHITECTURE_ATTRIBUTE, objects.wasm32Architecture)
            attribute(OPERATING_SYSTEM_ATTRIBUTE, objects.emscriptenOperatingSystem)
        }

        class Configurations(
            val wasmReleaseElements: ConsumableConfiguration,
            val wasmDebugElements: ConsumableConfiguration,
            val wasmSqliteEmscriptenArchiveElements: ConsumableConfiguration,
            val wasmLibraries: DependencyScopeConfiguration,
            val wasmStaticLibraries: ResolvableConfiguration,
            val wasmHeaders: ResolvableConfiguration,
        )
    }
}
