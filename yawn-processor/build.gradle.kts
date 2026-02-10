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
    // ksp
    implementation(libs.ksp.api)
    implementation(libs.ksp.common.deps)

    implementation(project(":yawn-api"))

    testImplementation(project(":yawn-ksp-test-fixtures"))
    testImplementation(libs.assertj)
    testImplementation(libs.junit.jupiter.api)

    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
