package com.faire.gradle.yawn

import net.navatwo.gradle.testkit.assertj.task
import net.navatwo.gradle.testkit.junit5.GradleProject
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration
import net.navatwo.gradle.testkit.junit5.GradleTestKitConfiguration.BuildDirectoryMode.PRISTINE
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import java.io.File

@GradleTestKitConfiguration(
    projectsRoot = "src/test/projects/yawn",
)
internal class FaireYawnTest {
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

      @Flag
      class Foo
    """.trimIndent()
    sourceFooClassFile.writeText(classContent)
    val result1 = runner.withArguments("build").build()

    val expectedGeneratedContent = """
      package com.faire.ksp
      
      class FooBar
    """.trimIndent()

    val builtFooBarFile = getBuiltFileFromQualifiedClassName(root, "com.faire.ksp.FooBar")
    assertThat(builtFooBarFile).hasContent(expectedGeneratedContent)

    // test update the Foo file, and make sure the generated file is updated
    val expectedUpdatedGeneratedContent = expectedGeneratedContent.replace("FooBar", "Foo2Bar")
    sourceFooClassFile.writeText(classContent.replace("Foo", "Foo2"))

    val result2 = runner.withArguments("build").build()

    val builtFoo2BarFile = getBuiltFileFromQualifiedClassName(root, "com.faire.ksp.Foo2Bar")
    assertThat(builtFoo2BarFile).hasContent(expectedUpdatedGeneratedContent)
    assertThat(root.resolve("build/generated/ksp/main/kotlin/com/faire/ksp/FooBar.kt")).doesNotExist()

    assertThat(result1).task(":build").isSuccess()
    assertThat(result2).task(":build").isSuccess()
  }

  private fun getBuiltFileFromQualifiedClassName(
      root: File,
      qualifiedClassName: String,
  ): File {
    val classPath = qualifiedClassName.replace('.', '/')
    return root.resolve("build/generated/ksp/main/kotlin/$classPath.kt")
  }
}
