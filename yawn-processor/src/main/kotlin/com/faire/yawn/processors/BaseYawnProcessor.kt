package com.faire.yawn.processors

import com.faire.ksp.getEffectiveVisibility
import com.faire.yawn.YawnDef
import com.faire.yawn.YawnTableDefParent
import com.faire.yawn.generators.addGeneratedAnnotation
import com.faire.yawn.generators.`object`.YawnReferenceObjectGenerator
import com.faire.yawn.util.YawnContext
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import java.io.PrintStream
import kotlin.reflect.KClass

/**
 * Generates our custom metamodel files associated with:
 *
 * * entities annotated with [com.faire.yawn.YawnEntity] via [YawnEntityProcessor]
 * * projection data classes annotated with [com.faire.yawn.project.YawnProjection] via [YawnProjectionProcessor]
 *
 * The generated files are used to power Yawn APIs.
 */
internal abstract class BaseYawnProcessor(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    protected abstract val annotationClass: KClass<out Annotation>
    protected abstract val yawnDefClass: KClass<out YawnDef<*, *>>

    abstract val objectRefGenerator: YawnReferenceObjectGenerator

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val ksClassDeclarationWithYawnAnnotation = resolver
            .getSymbolsWithAnnotation(annotationClass.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()

        for (ksClassDeclaration in ksClassDeclarationWithYawnAnnotation) {
            generateFile(ksClassDeclaration)
        }

        return listOf()
    }

    private fun generateFile(classDeclaration: KSClassDeclaration) {
        val packageName = classDeclaration.packageName.asString()
        val newClassName = generateYawnDefClassName(classDeclaration.toClassName())

        val yawnContext = buildYawnContext(classDeclaration, newClassName)
        val objectDef = objectRefGenerator.generate(yawnContext)
        val classDef = generateClassDefinition(yawnContext)

        val fileSpec = FileSpec.builder(packageName, newClassName)
            .addType(objectDef)
            .addType(classDef)
            .build()

        val outputFile = codeGenerator.createNewFile(
            Dependencies.ALL_FILES,
            classDeclaration.packageName.asString(),
            newClassName,
        )

        fileSpec.writeTo(PrintStream(outputFile))
    }

    private fun buildYawnContext(
        classDeclaration: KSClassDeclaration,
        newClassName: String,
    ): YawnContext {
        val sourceTypeVariable = TypeVariableName("SOURCE", Any::class.asTypeName())
        val superClassName = yawnDefClass.asClassName()
            .parameterizedBy(sourceTypeVariable, classDeclaration.toClassName())
        return YawnContext(
            classDeclaration = classDeclaration,
            superClassName = superClassName,
            sourceTypeVariable = sourceTypeVariable,
            newClassName = ClassName(classDeclaration.packageName.asString(), newClassName),
        )
    }

    private fun generateClassDefinition(
        yawnContext: YawnContext,
    ): TypeSpec {
        val classBuilder = TypeSpec.classBuilder(yawnContext.newClassName)
            .addGeneratedAnnotation(BaseYawnProcessor::class)
            .addModifiers(KModifier.OPEN)
            .addModifiers(yawnContext.classDeclaration.getEffectiveVisibility())
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(nullableParentParameter)
                    .build(),
            )
            .addTypeVariable(yawnContext.sourceTypeVariable)
            .superclass(yawnContext.superClassName)
            .run { additionalClassBuilder(yawnContext, this) }

        for (propertyDeclaration in yawnContext.classDeclaration.getAllProperties()) {
            val property = generateProperty(yawnContext, propertyDeclaration)
                ?: continue

            classBuilder.addProperty(property)
        }
        return classBuilder.build()
    }

    protected open fun additionalClassBuilder(
        yawnContext: YawnContext,
        classBuilder: TypeSpec.Builder,
    ): TypeSpec.Builder {
        return classBuilder
    }

    protected abstract fun generateProperty(
        yawnContext: YawnContext,
        property: KSPropertyDeclaration,
    ): PropertySpec?

    protected abstract fun generateYawnDefClassName(originalClassName: ClassName): String

    companion object {
        const val PARENT_PARAMETER_NAME = "parent"
        val parentType = YawnTableDefParent::class.asTypeName()

        private val nullableParentParameter = ParameterSpec.Companion.builder(
            PARENT_PARAMETER_NAME,
            parentType,
        ).build()
    }
}
