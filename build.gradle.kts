import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.JavadocJar

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
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
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    extensions.configure<MavenPublishBaseExtension> {
        configure(
            KotlinJvm(
                javadocJar = JavadocJar.Dokka("dokkaHtml"),
                sourcesJar = true
            )
        )

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
                developer {
                    id.set("quinn")
                    name.set("Quinn Budan")
                    email.set("quinn.budan@faire.com")
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

