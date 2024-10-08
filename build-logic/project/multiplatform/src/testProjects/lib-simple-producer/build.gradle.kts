plugins {
    `maven-publish`
    id("org.jetbrains.kotlin.multiplatform")
    id("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.kotlin")
    id("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.wasm-resources")
}

kotlin {
    linuxX64()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    macosX64()
    macosArm64()
}

group = "com.example"
version = "9999"

val conf = configurations.consumable("wasmSqliteReleaseElements") {
    outgoing {
        artifact(layout.projectDirectory.file("resource.txt.wasm"))
    }
}.get()

val testingRepository = project.layout.buildDirectory.dir("repo")

publishedResources {
    releaseFiles.setFrom(layout.projectDirectory.file("resource.txt.wasm"))
    debugFiles.setFrom(layout.projectDirectory.file("resource-debug.txt.wasm"))
}

publishing {
    repositories {
        maven {
            name = "test"
            setUrl(testingRepository)
        }
    }
    publications.withType<MavenPublication>().all {
        version = "9999"
    }
}
