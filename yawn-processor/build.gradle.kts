plugins {
    id("yawn.library")
    id("com.google.devtools.ksp")
}

dependencies {
    // Auto-service for generating META-INF service files
    compileOnly(libs.google.auto.service.annotations)
    annotationProcessor("com.google.auto.service:auto-service:${libs.versions.google.auto.service.get()}")

    // Core dependencies
    implementation(libs.javax.persistence)
    implementation(project(":yawn-api"))

    // KSP dependencies for the processor itself
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.21-1.0.27")

    // KotlinPoet for code generation
    implementation("com.squareup:kotlinpoet:1.18.1")
    implementation("com.squareup:kotlinpoet-ksp:1.18.1")

    // Additional KSP utilities (might provide missing extension functions)
    implementation("com.google.devtools.ksp:symbol-processing-common-deps:2.0.21-1.0.27")
}
