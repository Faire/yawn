plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    testRuntimeOnly(libs.kotlin.reflect)

    implementation(libs.gson)
    implementation(libs.guice)
    implementation(libs.javax.persistence)
    implementation(project(":yawn-api"))

    // KSP dependencies
    compileOnly(project(":yawn-processor"))
    ksp(project(":yawn-processor"))

    testImplementation(libs.assertj)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.h2database.h2)
    testRuntimeOnly(libs.junit.platform.launcher)
}
