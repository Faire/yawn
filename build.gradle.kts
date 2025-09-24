plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
}

allprojects {
    version = providers.gradleProperty("version").orElse("0.0.0-SNAPSHOT").get()
}
