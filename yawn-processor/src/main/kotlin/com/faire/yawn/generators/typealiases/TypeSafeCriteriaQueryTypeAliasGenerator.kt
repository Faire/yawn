package com.faire.yawn.generators.typealiases

import com.faire.ksp.getUniqueSimpleName
import com.faire.yawn.criteria.query.TypeSafeCriteriaQuery
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName

/**
 * Generates: `typealias DbBookCriteriaQuery = TypeSafeCriteriaQuery<DbBook, DbBookTableDef<DbBook>>`
 */
internal object TypeSafeCriteriaQueryTypeAliasGenerator : YawnTableDefTypeAliasGenerator {
    override fun getName(entityType: ClassName): String = "${entityType.getUniqueSimpleName()}CriteriaQuery"

    override fun getType(
        entityType: ClassName,
        tableDefType: ParameterizedTypeName,
    ): ParameterizedTypeName = TypeSafeCriteriaQuery::class.asClassName().parameterizedBy(entityType, tableDefType)
}
