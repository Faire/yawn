plugins {
    id("yawn.kotlin")
    id("com.google.devtools.ksp")
}

dependencies {
    api(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.reflect)

    api(project(":yawn-api"))
    implementation(libs.javax.persistence)

    // KSP dependencies
    compileOnly(project(":yawn-processor"))
    ksp(project(":yawn-processor"))

    testImplementation(libs.assertj)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
