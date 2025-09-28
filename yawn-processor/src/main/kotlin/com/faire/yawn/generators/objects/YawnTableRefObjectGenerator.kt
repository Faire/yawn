package com.faire.yawn.generators.objects

import com.faire.ksp.getAnnotationsByType
import com.faire.ksp.getEffectiveVisibility
import com.faire.yawn.YawnTableDefParent.SubqueryTableDefParent
import com.faire.yawn.YawnTableRef
import com.faire.yawn.generators.addGeneratedAnnotation
import com.faire.yawn.processors.BaseYawnProcessor.Companion.PARENT_PARAMETER_NAME
import com.faire.yawn.processors.BaseYawnProcessor.Companion.parentType
import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnNamesGenerator.generateTableObjectName
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import javax.persistence.Table

internal object YawnTableRefObjectGenerator : YawnReferenceObjectGenerator {
    /**
     * Generate a [com.faire.yawn.YawnTableRef] objetc for the generated [com.faire.yawn.YawnTableRef].
     * This acts as a singleton for the top level (non-aliased) table and is referenced by the user to perform queries.
     *
     * The output code will look like:
     * object FooTable : YawnTableRef<DbFoo, FooTableDef<DbFoo>> {
     *   override fun create(parent: YawnTableDefParent): FooTableDef<DbFoo> = FooTableDef(parent)
     * }
     */
    override fun generate(yawnContext: YawnContext): TypeSpec {
        val classDeclaration = yawnContext.classDeclaration
        val newClassName = yawnContext.newClassName.simpleName

        val originalClassName = classDeclaration.toClassName()
        val objectName = generateTableObjectName(originalClassName)

        val typeParameter = ClassName(originalClassName.packageName, newClassName).parameterizedBy(originalClassName)
        val superInterface = YawnTableRef::class.asClassName().parameterizedBy(originalClassName, typeParameter)

        return TypeSpec.objectBuilder(objectName)
            .addGeneratedAnnotation(YawnTableRefObjectGenerator::class)
            .addSuperinterface(superInterface)
            .addModifiers(classDeclaration.getEffectiveVisibility())
            .addFunctions(generateFactoryFunctions(classDeclaration, originalClassName, yawnContext.newClassName))
            .build()
    }

    private fun generateFactoryFunctions(
        classDeclaration: KSClassDeclaration,
        originalClassName: ClassName,
        newClassName: ClassName,
    ): List<FunSpec> {
        val returnType = ClassName(originalClassName.packageName, newClassName.simpleName)

        return listOf(
            generateCreateFunction(
                originalClassName = originalClassName,
                newClassName = newClassName,
                returnType = returnType,
            ),
            generateForSubQueryFunction(
                classDeclaration = classDeclaration,
                newClassName = newClassName,
                returnType = returnType,
            ),
        )
    }

    private fun generateCreateFunction(
        originalClassName: ClassName,
        newClassName: ClassName,
        returnType: ClassName,
    ): FunSpec {
        return FunSpec.builder("create")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(PARENT_PARAMETER_NAME, parentType)
            .returns(returnType.parameterizedBy(originalClassName))
            .addCode("return %T(%N)", newClassName, PARENT_PARAMETER_NAME)
            .build()
    }

    private fun generateForSubQueryFunction(
        classDeclaration: KSClassDeclaration,
        newClassName: ClassName,
        returnType: ClassName,
    ): FunSpec {
        val entityName = classDeclaration.getAnnotationsByType<Table>()
            .singleOrNull()
            ?.arguments
            ?.singleOrNull { it.name?.asString() == "name" }
            ?.value
            as? String
            ?: "detached"
        val parentSourceType = TypeVariableName("PARENT_SOURCE", Any::class.asTypeName())

        return FunSpec.builder("forSubQuery")
            .addTypeVariable(parentSourceType)
            .addModifiers(KModifier.OVERRIDE)
            .returns(returnType.parameterizedBy(parentSourceType))
            .addStatement("return %T(%T(%S))", newClassName, SubqueryTableDefParent::class.asClassName(), entityName)
            .build()
    }
}
