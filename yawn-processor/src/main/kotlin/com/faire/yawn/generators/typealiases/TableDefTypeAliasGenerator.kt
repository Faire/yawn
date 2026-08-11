package com.faire.yawn.generators.typealiases

import com.faire.ksp.getUniqueSimpleName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName

/**
 * Generates: `typealias DbBookTableDefType = DbBookTableDef<DbBook>`
 *
 * Names the definition of a query rooted at the entity, which is what a helper on one of the scope aliases takes:
 * `private fun DbBookEntityQueryScope.filterShortBooks(books: DbBookTableDefType)`.
 */
internal object TableDefTypeAliasGenerator : YawnTableDefTypeAliasGenerator {
    override fun getName(entityType: ClassName): String = "${entityType.getUniqueSimpleName()}TableDefType"

    override fun getType(
        entityType: ClassName,
        tableDefType: ParameterizedTypeName,
    ): ParameterizedTypeName = tableDefType
}
