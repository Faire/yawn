import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
  java
  kotlin("jvm")
  id("io.gitlab.arturbosch.detekt")
}

detekt {
  val rootConfig = rootProject.file("detekt.yaml")
  if (rootConfig.exists()) config.from(rootConfig)

  buildUponDefaultConfig = true
  allRules = true
  autoCorrect = true
  parallel = true
}

tasks.withType<Detekt>().configureEach {
  reports {
    xml.required.set(false)
    html.required.set(true)
    txt.required.set(false)
    sarif.required.set(true)
  }
  exclude("**/build/**", "**/.gradle/**")
}

tasks.named("check") { dependsOn("detekt") }

val libsProvider = rootProject.extensions.getByType<VersionCatalogsExtension>().find("libs")
dependencies {
  val libs = libsProvider.get()
  detektPlugins(libs.findLibrary("detekt-formatting").get())
  detektPlugins(libs.findLibrary("faire-detekt-rules").get())
}
