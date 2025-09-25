import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.JavadocJar

plugins {
    id("yawn.kotlin")
    id("com.vanniktech.maven.publish")
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
