# yawn-gradle-plugin

The Yawn Gradle plugin is a quality-of-life tool to help you set up Yawn in your Gradle-based Kotlin projects.

It is especially useful on multi-module gradle projects, where you want to apply Yawn to multiple modules without having to repeat the same configuration.

However, as of now, it requires a bit of one-time boilerplate to setup (we are working on trying to improve it). In particular, you will need to duplicate the
Yawn version into your Gradle properties so the plugin can read it. That only needs to be done once on your root project.

All the plugin does is add both necessary `yawn-processor` dependencies (the `compileOnly` and `ksp` ones) to any module that applies the plugin. Essentially,
it is equivalent to:

```kotlin
    compileOnly("com.faire.yawn:yawn-processor:$version")
    ksp("com.faire.yawn:yawn-processor:$version")
```

## Usage

1. On your root project, add the `com.faire.yawn.version` property to your Gradle properties (with the latest version in place of `<VERSION>`)

```properties
com.faire.yawn.version=<VERSION>
```

1. Then, on each module you have Yawn entities, add both the plugin and the `yawn-api` dependency to your `build.gradle.kts` file:

```kotlin
plugins {
  id("com.faire.yawn")
}

dependencies {
  implementation("com.faire.yawn:yawn-api:<VERSION>")
}
```

1. Finally, on each module you will have Yawn queries, add the `yawn-api` dependency to your `build.gradle.kts` file:

```kotlin
dependencies {
  implementation("com.faire.yawn:yawn-api:<VERSION>")
}
```
