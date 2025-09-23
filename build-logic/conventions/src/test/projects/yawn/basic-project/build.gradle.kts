plugins {
  id("com.faire.yawn")
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":ksp-annotation"))
}
