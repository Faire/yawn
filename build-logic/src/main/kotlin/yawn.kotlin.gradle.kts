import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.support.KotlinCompilerOptions

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

val javaVersionText = providers.fileContents(
    rootProject.layout.projectDirectory.file(".java-version"),
).asText.map { it.trim().substringBefore('.') }

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(javaVersionText.map { JavaLanguageVersion.of(it) })
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(javaVersionText.map { JavaLanguageVersion.of(it) })
    }
}

//tasks.withType<KotlinCompilerOptions> {
//    compilerOptions {
//        jvmTarget.set(JvmTarget.JVM_21)
//        freeCompilerArgs.add("-Xjsr305=strict")
//    }
//}

tasks.withType<Test> {
    useJUnitPlatform()
}
