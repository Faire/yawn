plugins {
    id("yawn.gradle-plugin")
    `kotlin-dsl`
}

dependencies {
    implementation(gradleKotlinDsl())
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)

    testImplementation(libs.navatwo.gradle.better.testing.junit5)
    testImplementation(libs.navatwo.gradle.better.testing.asserts)
    testImplementation(libs.assertj)
    testImplementation(libs.junit.jupiter.api)

    testRuntimeOnly(libs.junit.platform.launcher)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

/**
 * Add classpath entries to the build script classpath for test projects run with [GradleRunner].
 *
 * @see com.faire.gradle.test.PluginTestClasspathLoader
 */
val testProjectPluginClasspath: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val dumpPluginTestClasspathEntries by tasks.registering(Task::class) {
    // pluginUnderTestMetadata is owned by the `java-gradle-plugin` plugin that is applied by this plugin
    val pluginTestPluginClasspathEntriesFile = project.layout.buildDirectory.file("pluginUnderTestMetadata/extraClasspath.txt")

    inputs.files(testProjectPluginClasspath)
    outputs.file(pluginTestPluginClasspathEntriesFile)

    // Create a file collection, otherwise the task fails configuration-cache checks because we can't serialize
    // configuration references.
    val configurationFiles = files(project.configurations.named("testProjectPluginClasspath"))

    doLast {
        val outputFile = pluginTestPluginClasspathEntriesFile.get().asFile
        outputFile.parentFile.mkdirs()

        outputFile.bufferedWriter().use { writer ->
            for (file in configurationFiles.sortedBy { it.path }) {
                writer.appendLine(file.path)
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()

    systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
    systemProperty(
        "net.navatwo.gradle.testkit.junit5.testKitDirectory",
        rootProject.projectDir.resolve(".gradle/testKits").toString()
    )

    inputs.files(fileTree("src/test/projects").exclude("**/build/**"))

    dependsOn(dumpPluginTestClasspathEntries)
}
