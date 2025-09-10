package com.faire.gradle.yawn

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

class FaireYawnPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("org.jetbrains.kotlin.jvm")
        target.pluginManager.apply("com.google.devtools.ksp")
        
        val kspProject: Provider<String> = target.providers.gradleProperty("yawn.ksp.project")
        
        if (kspProject.isPresent) {
            target.dependencies {
                add("compileOnly", project(kspProject.get()))
                add("ksp", project(kspProject.get()))
            }
        }
    }
}
