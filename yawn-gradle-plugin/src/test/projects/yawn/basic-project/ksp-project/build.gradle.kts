plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.devtools.ksp:symbol-processing-api:1.9.23-1.0.20")

    ksp("dev.zacsweers.autoservice:auto-service-ksp:1.1.0")

    implementation("com.google.auto.service:auto-service-annotations:1.0.2")
}
