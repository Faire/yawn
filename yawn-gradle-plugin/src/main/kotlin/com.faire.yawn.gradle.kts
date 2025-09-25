plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp")
}

val kspProject: Provider<String> = providers.gradleProperty("yawn.ksp.project")
dependencies {
    compileOnly(project(kspProject.get()))
    ksp(project(kspProject.get()))
}
