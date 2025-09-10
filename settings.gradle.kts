plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "yawn"

include(
    "yawn-api",
    "yawn-processor", 
    "yawn-database-test",
    "yawn-integration-test"
)

// Set project directories to match the yawn/* structure
project(":yawn-api").projectDir = file("yawn/yawn-api")
project(":yawn-processor").projectDir = file("yawn/yawn-processor")
project(":yawn-database-test").projectDir = file("yawn/yawn-database-test")
project(":yawn-integration-test").projectDir = file("yawn/yawn-integration-test")