plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

includeBuild("build-logic")

rootProject.name = "yawn"

include(
    "yawn-api",
    "yawn-database-test",
    "yawn-gradle-plugin",
    "yawn-processor",
    "yawn-integration-test",
)
