package com.faire.yawn.generators.typealiases

import com.faire.ksp.getUniqueSimpleName
import com.faire.yawn.criteria.query.JoinYawnQueryScope
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.asClassName

/**
 * Generates: `typealias DbBookJoinQueryScope = JoinYawnQueryScope<*, DbBook, *>`
 */
internal object JoinYawnQueryScopeTypeAliasGenerator : YawnTableDefTypeAliasGenerator {
    override fun getName(entityType: ClassName): String = "${entityType.getUniqueSimpleName()}JoinQueryScope"

    override fun getType(entityType: ClassName, tableDefType: ParameterizedTypeName): ParameterizedTypeName {
        return JoinYawnQueryScope::class.asClassName().parameterizedBy(
            STAR,
            entityType,
            STAR,
        )
    }
}
