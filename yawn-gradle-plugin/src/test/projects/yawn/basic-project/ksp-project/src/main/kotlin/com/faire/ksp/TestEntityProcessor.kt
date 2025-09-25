package com.faire.ksp

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * This is just an arbitrary processor used for testing the KSP setup.
 *
 * For each annotation class X it will create an empty XBar class.
 * That has no real purpose, it is just an easy way to determine if the processor successfully ran or not.
 */
class TestEntityProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation("com.faire.ksp.Flag")
            .filterIsInstance<KSClassDeclaration>()
            .forEach { generateFile(it) }

        return listOf()
    }

    /**
     * Given a class that was selected in the [process] method, this will create a new file within the same package with
     * a single class that has the same name as the original class, but with "Bar" appended to it.
     *
     * Again, this has absolutely no purpose other than testing that KSP is configured and running correctly.
     */
    private fun generateFile(ksClass: KSClassDeclaration) {
        val newClassName = ksClass.simpleName.asString() + "Bar"

        val outputFile = codeGenerator.createNewFile(
            Dependencies.ALL_FILES,
            ksClass.packageName.asString(),
            newClassName
        )

        outputFile.writer().use { writer ->
            writer.append("package ${ksClass.packageName.asString()}\n\n")
            writer.append("class $newClassName")
        }
    }
}
