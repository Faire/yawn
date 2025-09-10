plugins {
  `kotlin-dsl`
}

dependencies {
  implementation(gradleKotlinDsl())
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
  implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.0.21-1.0.27")
}

gradlePlugin {
  plugins {
    create("com.faire.yawn") {
      id = "com.faire.yawn"
      implementationClass = "com.faire.gradle.yawn.FaireYawnPlugin"
    }
  }
}
