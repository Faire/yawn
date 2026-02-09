package com.faire.yawn.generators.typealiases

import com.faire.ksp.getUniqueSimpleName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName

/**
 * Generates: `typealias DbBookTableDefType = DbBookTableDef<DbBook>`
 */
internal object TableDefTypeAliasGenerator : YawnTableDefTypeAliasGenerator {
    override fun getName(entityType: ClassName): String = "${entityType.getUniqueSimpleName()}TableDefType"

    override fun getType(
        entityType: ClassName,
        tableDefType: ParameterizedTypeName,
    ): ParameterizedTypeName = tableDefType
}
