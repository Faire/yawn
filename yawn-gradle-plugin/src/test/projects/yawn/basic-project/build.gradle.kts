plugins {
    id("com.faire.yawn")
}

repositories {
    mavenCentral()
}

val version = providers.gradleProperty("com.faire.yawn.version").get()
dependencies {
    implementation("com.faire.yawn:yawn-api:$version")
}
