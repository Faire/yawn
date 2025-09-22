plugins {
  `kotlin-dsl`
}

dependencies {
  implementation(gradleKotlinDsl())
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.ksp.gradle.plugin)
}

gradlePlugin {
  plugins {
    create("com.faire.yawn") {
      id = "com.faire.yawn"
      implementationClass = "com.faire.gradle.yawn.FaireYawnPlugin"
    }
  }
}
