
plugins {
    id("ru.pixnews.wasm.sqlite.binary.gradle.lint.detekt")
    id("ru.pixnews.wasm.sqlite.binary.gradle.lint.diktat")
    id("ru.pixnews.wasm.sqlite.binary.gradle.lint.spotless")
    id("ru.pixnews.wasm.sqlite.binary.gradle.multiplatform.distribution")
}

dependencies {
    add("wasmArchiveAggregation", projects.sqliteAndroidWasmEmscriptenIcu346)
    add("wasmArchiveAggregation", projects.sqliteAndroidWasmEmscriptenIcuMtPthread346)
    add("wasmArchiveAggregation", projects.sqliteWasmEmscripten346)
    add("wasmArchiveAggregation", projects.sqliteWasmEmscriptenMtPthread346)
}

tasks.register("styleCheck") {
    group = "Verification"
    description = "Runs code style checking tools (excluding tests)"
    dependsOn("detektCheck", "spotlessCheck", "diktatCheck")
}

tasks.named("check").configure {
    dependsOn(gradle.includedBuild("sohb-gradle-project-plugins").task(":multiplatform:check"))
    dependsOn(gradle.includedBuild("sohb-gradle-project-plugins").task(":sqlite-build-info:check"))
    dependsOn(gradle.includedBuild("sohb-gradle-project-plugins").task(":sqlite-build-info-ext:check"))
    dependsOn(gradle.includedBuild("sohb-gradle-wasm-builder-plugins").task(":sqlite:check"))
}
