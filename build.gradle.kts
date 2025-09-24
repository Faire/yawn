import org.gradle.api.plugins.JavaPluginExtension
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.dokka) apply false
    id("com.vanniktech.maven.publish") version "0.34.0"
}

allprojects {
    group = "com.faire.yawn"
    version = providers.gradleProperty("version").orElse("0.0.0-SNAPSHOT").get()
    
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.vanniktech.maven.publish")

    configure<JavaPluginExtension> {
        withSourcesJar()
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    extensions.configure<MavenPublishBaseExtension> {
        publishToMavenCentral(automaticRelease = true)
        signAllPublications()

        pom {
            name.set("${project.group}:${project.name}")
            description.set("Yawn - Hibernate ORM type-safe wrapper")
            url.set("https://github.com/faire/yawn")
            
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://github.com/faire/yawn/blob/main/LICENSE")
                }
            }
            
            developers {
                developer {
                    id.set("luan")
                    name.set("Luan Nico")
                    email.set("luan@faire.com")
                }
            }

            scm {
                connection.set("scm:git:https://github.com/faire/yawn.git")
                developerConnection.set("scm:git:git@github.com:faire/yawn.git")
                url.set("https://github.com/faire/yawn")
            }
        }
    }
}

