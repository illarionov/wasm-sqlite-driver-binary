/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.gradle.kotlin.dsl.withType
import ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish.createWasmSqliteVersionsExtension

/*
 * Convention plugin with publishing defaults
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    id("com.vanniktech.maven.publish.base")
}

private val wasmVersions = createWasmSqliteVersionsExtension()

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

private val publishedMavenLocalRoot = project.rootProject.layout.buildDirectory.dir("localMaven")
private val downloadableReleaseDirName = wasmVersions.rootVersion.map { "maven-wasm-sqlite-binary-$it" }
private val downloadableReleaseRoot = publishedMavenLocalRoot.zip(downloadableReleaseDirName) { root, subdir ->
    root.dir(subdir)
}
private val distributionDir = project.rootProject.layout.buildDirectory.dir("distribution")

mavenPublishing {
    publishing {
        repositories {
            maven {
                name = "test"
                setUrl(publishedMavenLocalRoot.map { it.dir("test") })
            }
            maven {
                name = "downloadableRelease"
                setUrl(downloadableReleaseRoot)
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

tasks.register<Zip>("packageMavenDistribution") {
    archiveBaseName = "maven-wasm-sqlite-binary"
    destinationDirectory = distributionDir

    from(downloadableReleaseRoot)
    into(downloadableReleaseDirName)

    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false

    dependsOn(tasks.named("publishAllPublicationsToDownloadableReleaseRepository"))
}
