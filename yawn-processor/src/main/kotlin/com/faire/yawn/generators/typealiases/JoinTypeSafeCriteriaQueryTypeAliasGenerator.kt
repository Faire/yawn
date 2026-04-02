package com.faire.yawn.generators.typealiases

import com.faire.ksp.getUniqueSimpleName
import com.faire.yawn.criteria.query.JoinTypeSafeCriteriaQuery
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName

/**
 * Generates: `typealias DbBookJoinCriteriaQuery = JoinTypeSafeCriteriaQuery<DbBook, DbBook, DbBookTableDef<DbBook>>`
 */
internal object JoinTypeSafeCriteriaQueryTypeAliasGenerator : YawnTableDefTypeAliasGenerator {
    override fun getName(entityType: ClassName): String = "${entityType.getUniqueSimpleName()}JoinCriteriaQuery"

    override fun getType(entityType: ClassName, tableDefType: ParameterizedTypeName): ParameterizedTypeName {
        return JoinTypeSafeCriteriaQuery::class.asClassName().parameterizedBy(
            entityType,
            entityType,
            tableDefType,
        )
    }
}
