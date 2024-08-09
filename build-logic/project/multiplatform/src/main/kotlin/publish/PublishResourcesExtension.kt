/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish

import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.kotlin.dsl.property
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish.PublishResourcesExtension.PublishMethod.COMMON_MODULE
import java.io.Serializable
import javax.inject.Inject

public abstract class PublishResourcesExtension @Inject internal constructor(
    objects: ObjectFactory,
    providers: ProviderFactory,
    configurations: ConfigurationContainer,
) : Serializable {
    public val releaseFiles: ConfigurableFileCollection = objects.fileCollection().apply {
        val wasmReleaseElements = providers.provider {
            configurations.findByName("wasmSqliteReleaseElements")?.artifacts?.files
        }
        from(wasmReleaseElements)
    }
    public val debugFiles: ConfigurableFileCollection = objects.fileCollection().apply {
        val wasmReleaseElements = providers.provider {
            configurations.findByName("wasmSqliteDebugElements")?.artifacts?.files
        }
        from(wasmReleaseElements)
    }
    public val publishMethod: Property<PublishMethod> = objects.property<PublishMethod>().convention(COMMON_MODULE)

    public enum class PublishMethod {
        COMMON_MODULE,
        TARGETS,
    }

    public companion object {
        private const val serialVersionUID: Long = -1
    }
}
