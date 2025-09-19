import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.PublishingExtension
import org.gradle.plugins.signing.SigningExtension
import org.gradle.api.plugins.JavaPluginExtension

plugins {
    kotlin("jvm") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
    id("org.jetbrains.dokka") version "1.9.20" apply false
}

allprojects {
    group = "com.faire.yawn"
    version = "1.0.0"
    
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "org.jetbrains.dokka")

    configure<JavaPluginExtension> {
        withSourcesJar()
        withJavadocJar()
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

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                
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
                        connection.set("scm:git:git://github.com/faire/yawn.git")
                        developerConnection.set("scm:git:ssh://github.com/faire/yawn.git")
                        url.set("https://github.com/faire/yawn")
                    }
                }
            }
        }
        
        repositories {
            maven {
                name = "OSSRH"
                url = if (version.toString().endsWith("SNAPSHOT")) {
                    uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                } else {
                    uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                }
                credentials {
                    username = project.findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                    password = project.findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
                }
            }
        }
    }

    configure<SigningExtension> {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(the<PublishingExtension>().publications["maven"])
    }
}