/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage")

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.gradle.kotlin.dsl.withType
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.localsnapshot.CleanupDownloadableReleaseDirectoryTask
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.localsnapshot.DistributionAggregationConfigurations.Companion.createMavenSnapshotReleaseElements
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish.createWasmSqliteVersionsExtension

/*
 * Convention plugin with publishing defaults
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.vanniktech.maven.publish.base")
}

private val wasmVersions = createWasmSqliteVersionsExtension()
val localMavenSnapshotRoot: Provider<Directory> = layout.buildDirectory.dir("localMaven").zip(
    wasmVersions.rootVersion.map { "maven-wasm-sqlite-binary-$it" },
    Directory::dir,
)

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

mavenPublishing {
    publishing {
        repositories {
            maven {
                name = "LocalMavenSnapshot"
                setUrl(localMavenSnapshotRoot)
            }
            maven {
                name = "PixnewsS3"
                setUrl("s3://maven.pixnews.ru/")
                credentials(AwsCredentials::class) {
                    accessKey = providers.environmentVariable("YANDEX_S3_ACCESS_KEY_ID").getOrElse("")
                    secretKey = providers.environmentVariable("YANDEX_S3_SECRET_ACCESS_KEY").getOrElse("")
                }
            }
        }
    }

    signAllPublications()

    configure(
        KotlinMultiplatform(javadocJar = JavadocJar.None()),
    )

    pom {
        name.set(project.name)
        description.set("SQLite WebAssembly binaries for wasm-sqlite-open-helper")
        url.set("https://github.com/illarionov/wasm-sqlite-driver-binary")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("illarionov")
                name.set("Alexey Illarionov")
                email.set("alexey@0xdc.ru")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/illarionov/wasm-sqlite-driver-binary.git")
            developerConnection.set("scm:git:ssh://github.com:illarionov/wasm-sqlite-driver-binary.git")
            url.set("https://github.com/illarionov/wasm-sqlite-driver-binary")
        }
    }
}

val mavenSnapshotReleaseElements = createMavenSnapshotReleaseElements().get()

@Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
val cleanupDownloadableReleaseRootTask = tasks.register<CleanupDownloadableReleaseDirectoryTask>(
    "cleanupDownloadableRelease",
) {
    inputDirectory.set(localMavenSnapshotRoot)
}

tasks.withType<PublishToMavenRepository>().configureEach {
    dependsOn(cleanupDownloadableReleaseRootTask)
}

val publishAllPublicationsTask = tasks.named("publishAllPublicationsToLocalMavenSnapshotRepository")
publishAllPublicationsTask.configure {
    dependsOn(cleanupDownloadableReleaseRootTask)
}

mavenSnapshotReleaseElements.outgoing {
    artifact(localMavenSnapshotRoot) {
        builtBy(publishAllPublicationsTask)
    }
}
