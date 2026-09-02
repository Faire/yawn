import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    id("yawn.kotlin")
    id("org.jetbrains.dokka")
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
        description.set(
            """
            Yawn is a Kotlin ORM-wrapper that provides a type-safe, expressive, Criteria-style query 
            syntax using custom KSP-generated entity metadata.
            """.trimIndent()
        )
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

kotlin {
    compilerOptions {
        // Compile interface bodies as real JVM default methods, rather than into synthetic `DefaultImpls`
        // classes that every implementor needs a generated forwarder to reach. SAM conversions do not generate
        // that forwarder for a default *inherited* by a `fun interface`, so the call throws. This is
        // https://youtrack.jetbrains.com/issue/KT-80400, fixed upstream in 2.3.0-Beta1 and the compiler default
        // from 2.2. `YawnValueProjector` inherits its defaults from `YawnProjector`, so it is exactly this case;
        // `ResolvedProjectionAdapterTest.a projector can be projected directly` fails without this flag.
        //
        // `all-compatibility` keeps emitting `DefaultImpls` alongside, so already-published consumers still link.
        // Only the module declaring the interface needs the flag; consumers of the artifact do not.
        freeCompilerArgs.add("-Xjvm-default=all-compatibility")
    }
}
