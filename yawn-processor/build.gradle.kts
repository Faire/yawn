plugins {
    id("yawn.library")
    id("com.google.devtools.ksp")
}

dependencies {
    compileOnly(libs.google.auto.service.annotations)
    annotationProcessor("com.google.auto.service:auto-service:${libs.versions.google.auto.service.get()}")

    implementation(libs.javax.persistence)
    // kotlinpoet
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
    // ksp - use compileOnly to avoid forcing a specific KSP version on consumers
    compileOnly(libs.ksp.api)
    compileOnly(libs.ksp.common.deps)

    implementation(project(":yawn-api"))
}
