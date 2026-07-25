import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("yawn.detekt")
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
    compilerOptions {
        jvmTarget = javaVersionText.map { JvmTarget.fromTarget(it) }
        freeCompilerArgs.add("-Xjsr305=strict")
        // Compile interface default methods as real JVM default methods, rather than to `DefaultImpls` with a
        // bridge generated into each implementor. Without this, a SAM-converted `fun interface` does not pick up
        // a default that its *super*-interface supplies for an abstract member, and calling it throws
        // AbstractMethodError at runtime - see `YawnProjector`, which defaults `YawnQueryProjection`'s members.
        // This becomes the compiler default in Kotlin 2.2.
        freeCompilerArgs.add("-Xjvm-default=all")
        allWarningsAsErrors.set(true)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
