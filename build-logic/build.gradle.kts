plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.jvm) apply false
}

dependencies {
    implementation(gradleKotlinDsl())

    implementation(libs.jetbrains.dokka)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.vanniktech.maven.publish)
    implementation(libs.detekt.gradle.plugin)
}
