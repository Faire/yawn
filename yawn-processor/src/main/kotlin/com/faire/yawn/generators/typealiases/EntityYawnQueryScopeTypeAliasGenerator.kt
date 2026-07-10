package com.faire.yawn.generators.typealiases

import com.faire.ksp.getUniqueSimpleName
import com.faire.yawn.criteria.query.EntityYawnQueryScope
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName

/**
 * Generates: `typealias DbBookEntityQueryScope = EntityYawnQueryScope<DbBook, DbBookTableDefType>`
 */
internal object EntityYawnQueryScopeTypeAliasGenerator : YawnTableDefTypeAliasGenerator {
    override fun getName(entityType: ClassName): String = "${entityType.getUniqueSimpleName()}EntityQueryScope"

    override fun getType(
        entityType: ClassName,
        tableDefType: ParameterizedTypeName,
    ): ParameterizedTypeName = EntityYawnQueryScope::class.asClassName().parameterizedBy(entityType, tableDefType)
}
