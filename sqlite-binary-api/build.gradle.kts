
plugins {
    id("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.kotlin")
    id("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.publish")
}

group = "ru.pixnews.wasm-sqlite-open-helper"
version = wasmSqliteVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "wsoh_sqlite_driver_binary_api_version",
    envVariableName = "WSOH_SQLITE_DRIVER_BINARY_API_VERSION",
).get()

kotlin {
    jvm()
}
