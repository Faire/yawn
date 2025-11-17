package com.faire.gradle.yawn

import net.navatwo.gradle.testkit.assertj.task
import net.navatwo.gradle.testkit.junit5.GradleProject
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.BuildDirectoryMode.PRISTINE
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@GradleTestKitConfiguration(
    projectsRoot = "src/test/projects/yawn",
)
internal class FaireYawnTest {
    @TempDir
    lateinit var dir: Path

    /**
     * This tests the setup provided by the KSP plugin
     * by using it to bind a test processor that creates *Bar version
     * of annotated classes.
     *
     * The test is not for the processor itself, but rather for the
     * setup of the plugin.
     */
    @Test
    @GradleProject("basic-project")
    @GradleTestKitConfiguration(
        buildDirectoryMode = PRISTINE,
    )
    fun `basic project with ksp processor`(
        @GradleProject.Runner runner: GradleRunner,
        @GradleProject.Root root: File,
    ) {
        val fooClassFolder = root.resolve("src/main/kotlin/com/faire/ksp/")
        fooClassFolder.mkdirs()
        val sourceFooClassFile = File.createTempFile("Foo", ".kt", fooClassFolder)
        val classContent = """
      package com.faire.ksp

      @com.faire.yawn.YawnEntity
      class Foo
        """.trimIndent()
        sourceFooClassFile.writeText(classContent)
        val result1 = runner.withArguments("build").build()

        assertThat(getBuiltFileFromQualifiedClassName(root, "com.faire.ksp.FooTableDef")).exists()

        // test update the Foo file, and make sure the generated file is updated
        sourceFooClassFile.writeText(classContent.replace("Foo", "Foo2"))

        val result2 = runner.withArguments("build").build()

        assertThat(getBuiltFileFromQualifiedClassName(root, "com.faire.ksp.Foo2TableDef")).exists()
        assertThat(getBuiltFileFromQualifiedClassName(root, "com.faire.ksp.FooTableDef"))
            .doesNotExist()

        assertThat(result1).task(":build").isSuccess()
        assertThat(result2).task(":build").isSuccess()
    }

    @ParameterizedTest
    @MethodSource("versions")
    fun buildsWithMatrix(kotlinVersion: String, kspVersion: String) {
        Files.writeString(
            dir.resolve("settings.gradle.kts"),
            """
      pluginManagement {
        repositories { mavenLocal(); gradlePluginPortal(); mavenCentral() }
      }
      dependencyResolutionManagement { repositories { mavenLocal(); mavenCentral() } }
            """.trimIndent(),
        )

        Files.writeString(
            dir.resolve("gradle.properties"),
            """
      com.faire.yawn.version=0.0.0-SNAPSHOT
            """.trimIndent(),
        )

        Files.writeString(
            dir.resolve("build.gradle.kts"),
            """
      plugins {
        kotlin("jvm") version "$kotlinVersion"
        id("com.google.devtools.ksp") version "$kspVersion"
        id("com.faire.yawn")
      }
      repositories { mavenLocal(); mavenCentral() }
      val version = providers.gradleProperty("com.faire.yawn.version").get()
      dependencies {
        implementation("com.faire.yawn:yawn-api:${"$"}version")
      }
            """.trimIndent(),
        )

        // Create source directory and a test entity file
        val srcDir = dir.resolve("src/main/kotlin/com/faire/test")
        Files.createDirectories(srcDir)
        Files.writeString(
            srcDir.resolve("TestEntity.kt"),
            """
      package com.faire.test

      import com.faire.yawn.YawnEntity

      @YawnEntity
      data class TestEntity(
        val id: Long,
        val name: String
      )
            """.trimIndent(),
        )

        val buildResult = GradleRunner.create()
            .withProjectDir(dir.toFile())
            .withArguments("build", "--stacktrace")
            .withPluginClasspath() // pulls your local plugin-under-test
            .forwardOutput()
            .build()

        assertTrue(buildResult.output.contains("BUILD SUCCESSFUL"))
        // Verify that the KSP processor generated the table definition
        val generatedFile = dir.resolve("build/generated/ksp/main/kotlin/com/faire/test/TestEntityTableDef.kt")
        assertTrue(Files.exists(generatedFile), "KSP processor should have generated TestEntityTableDef.kt")

        // Verify dependency tree: ensure we're not polluting with processor's KSP version
        // Check kspKotlinProcessorClasspath which shows the actual resolved processor dependencies
        val depsResult = GradleRunner.create()
            .withProjectDir(dir.toFile())
            .withArguments("dependencies", "--configuration", "kspKotlinProcessorClasspath")
            .withPluginClasspath()
            .build()

        // The processor is built with KSP 2.0.10-1.0.24
        // With compileOnly, we should NOT see any symbol-processing dependencies at all
        // (the user's KSP plugin provides them at runtime)
        assertFalse(
            depsResult.output.contains("symbol-processing-api"),
            "Dependency tree should NOT contain symbol-processing-api from processor with compileOnly",
        )
        assertFalse(
            depsResult.output.contains("symbol-processing-common-deps"),
            "Dependency tree should NOT contain symbol-processing-common-deps from processor with compileOnly",
        )

        // Verify the user's yawn-processor dependency is present
        assertTrue(
            depsResult.output.contains("com.faire.yawn:yawn-processor"),
            "Dependency tree should contain yawn-processor",
        )
    }

    companion object {
        @JvmStatic
        fun versions() = listOf(
            Arguments.of("2.0.10", "2.0.10-1.0.24"),
            Arguments.of("2.0.21", "2.0.21-1.0.28"),
        )
    }

    private fun getBuiltFileFromQualifiedClassName(
        root: File,
        qualifiedClassName: String,
    ): File {
        val classPath = qualifiedClassName.replace('.', '/')
        return root.resolve("build/generated/ksp/main/kotlin/$classPath.kt")
    }
}
