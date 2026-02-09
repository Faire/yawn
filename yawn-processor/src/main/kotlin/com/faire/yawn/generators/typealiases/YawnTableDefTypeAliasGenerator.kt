package com.faire.yawn.generators.typealiases

import com.faire.ksp.getEffectiveVisibility
import com.faire.yawn.util.YawnContext
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * [YawnTypeAliasGenerator] for [com.faire.yawn.YawnTableDef]s and related types.
 */
internal interface YawnTableDefTypeAliasGenerator : YawnTypeAliasGenerator {
    override fun generate(yawnContext: YawnContext): TypeAliasSpec {
        val entityType = yawnContext.classDeclaration.toClassName()
        val tableDefType = yawnContext.newClassName.parameterizedBy(entityType)
        val visibility = yawnContext.classDeclaration.getEffectiveVisibility()

        val typeAliasName = getName(entityType)
        val typeAliasType = getType(entityType, tableDefType)

        return TypeAliasSpec.builder(typeAliasName, typeAliasType)
            .addModifiers(visibility)
            .additionalTypeAliasBuilder(yawnContext)
            .build()
    }

    fun getName(entityType: ClassName): String

    fun getType(entityType: ClassName, tableDefType: ParameterizedTypeName): ParameterizedTypeName

    fun TypeAliasSpec.Builder.additionalTypeAliasBuilder(yawnContext: YawnContext): TypeAliasSpec.Builder = this
}
