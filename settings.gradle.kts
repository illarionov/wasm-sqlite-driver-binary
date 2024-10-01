pluginManagement {
    includeBuild("build-logic/settings")
}

plugins {
    id("ru.pixnews.wasm.sqlite.binary.gradle.settings.root")
}

// Workaround for https://github.com/gradle/gradle/issues/26020
buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.2")
        classpath("com.diffplug.spotless:spotless-plugin-gradle:6.25.0")
        classpath("com.saveourtool.diktat:diktat-gradle-plugin:2.0.0")
        classpath("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.6")
        classpath(
            group = "org.jetbrains.kotlinx.binary-compatibility-validator",
            name = "org.jetbrains.kotlinx.binary-compatibility-validator.gradle.plugin",
            version = "0.16.2",
        )
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0-Beta1")
    }
}

rootProject.name = "wasm-sqlite-open-helper-binary"

include("common-xdg")
include("common-tempfolder")
include("sqlite-binary-api")
include("wasm-binary-reader")
include("icu-wasm")
include("sqlite-android-wasm-emscripten-icu-mt-pthread-346")
include("sqlite-android-wasm-emscripten-icu-346")
include("sqlite-wasm-emscripten-346")
include("sqlite-wasm-emscripten-mt-pthread-346")
