pluginManagement {
    includeBuild("build-logic/settings")
    includeBuild("build-logic/project") { name = "sohb-gradle-project-plugins" }
    includeBuild("build-logic/wasm-builder") { name = "sohb-gradle-wasm-builder-plugins" }
    includeBuild("build-logic/chicory-aot-gradle") { name = "sohb-chicory-aot-plugin" }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
    id("ru.pixnews.wasm.sqlite.binary.gradle.settings.root")
}

// Workaround for https://github.com/gradle/gradle/issues/26020
buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.3")
        classpath("com.diffplug.spotless:spotless-plugin-gradle:7.0.2")
        classpath("com.saveourtool.diktat:diktat-gradle-plugin:2.0.0")
        classpath("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.8")
        classpath(
            group = "org.jetbrains.kotlinx.binary-compatibility-validator",
            name = "org.jetbrains.kotlinx.binary-compatibility-validator.gradle.plugin",
            version = "0.17.0",
        )
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.20-RC")
    }
}

rootProject.name = "wasm-sqlite-open-helper-binary"

include("common-xdg")
include("sqlite-binary-api")
include("wasm-binary-reader")
include("icu-wasm")
include("sqlite-android-wasm-emscripten-icu-mt-pthread-348")
include("sqlite-android-wasm-emscripten-icu-348")
include("sqlite-wasm-emscripten-348")
include("sqlite-wasm-emscripten-aot-348")
include("sqlite-wasm-emscripten-mt-pthread-348")
