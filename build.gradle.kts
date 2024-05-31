
plugins {
    id("ru.pixnews.wasm.sqlite.binary.gradle.lint.detekt")
    id("ru.pixnews.wasm.sqlite.binary.gradle.lint.diktat")
    id("ru.pixnews.wasm.sqlite.binary.gradle.lint.spotless")
}

tasks.register("styleCheck") {
    group = "Verification"
    description = "Runs code style checking tools (excluding tests)"
    dependsOn("detektCheck", "spotlessCheck", "diktatCheck")
}
