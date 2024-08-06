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
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish.DownloadableDistributionPaths
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish.createWasmSqliteVersionsExtension

/*
 * Convention plugin with publishing defaults
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.vanniktech.maven.publish.base")
}

private val wasmVersions = createWasmSqliteVersionsExtension()
val localMavenPaths = DownloadableDistributionPaths(
    rootProject,
    wasmVersions.rootVersion,
)

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

mavenPublishing {
    publishing {
        repositories {
            maven {
                name = "test"
                setUrl(localMavenPaths.root.dir("test"))
            }
            maven {
                name = "downloadableRelease"
                setUrl(localMavenPaths.downloadableReleaseRoot)
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

val rootCleanupDownloadableReleaseRootTask = rootProject.tasks.named("cleanupDownloadableRelease")
tasks.withType<PublishToMavenRepository>().configureEach {
    dependsOn(rootCleanupDownloadableReleaseRootTask)
}

val publishAllPublicationsTask = tasks.named("publishAllPublicationsToDownloadableReleaseRepository")
publishAllPublicationsTask.configure {
    dependsOn(rootCleanupDownloadableReleaseRootTask)
}

rootProject.tasks.named("publishAllPublicationsToDownloadableReleaseRepository").configure {
    dependsOn(publishAllPublicationsTask)
}
