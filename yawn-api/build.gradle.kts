plugins {
    id("yawn.library")
}

dependencies {
    api(libs.kotlin.stdlib)
    api(libs.hibernate.core)

    implementation(libs.kotlin.reflect)

    testImplementation(libs.assertj)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
