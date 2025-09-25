import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

group = "com.faire.yawn"

repositories {
    mavenCentral()
}

val javaVersionText = providers.fileContents(
    rootProject.layout.projectDirectory.file(".java-version"),
).asText.map { it.trim().substringBefore('.') }

java {
    withSourcesJar()
    toolchain {
        languageVersion = javaVersionText.map { JavaLanguageVersion.of(it) }
    }
}

kotlin {
    jvmToolchain {
        languageVersion = javaVersionText.map { JavaLanguageVersion.of(it) }
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget = javaVersionText.map { JvmTarget.fromTarget(it) }
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
