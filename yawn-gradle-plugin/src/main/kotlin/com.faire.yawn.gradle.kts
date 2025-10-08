plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp")
}

val version = providers.gradleProperty("com.faire.yawn.version").get()
dependencies {
    compileOnly("com.faire.yawn:yawn-processor:$version")
    ksp("com.faire.yawn:yawn-processor:$version")
}
