/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

class DownloadableDistributionPaths(
    rootProject: Project,
    val rootVersion: Provider<String>,
    val root: Directory = rootProject.layout.projectDirectory.dir("build/localMaven"),
) {
    val downloadableReleaseDirName: Provider<String> = rootVersion.map { "maven-wasm-sqlite-binary-$it" }
    val downloadableReleaseRoot: Provider<Directory> = root.dir(downloadableReleaseDirName)
    val distributionDir: Directory = rootProject.layout.projectDirectory.dir("build/distribution")
}
