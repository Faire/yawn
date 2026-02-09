package com.faire.yawn.generators.typealiases

import com.faire.ksp.getUniqueSimpleName
import com.faire.yawn.criteria.query.ProjectedTypeSafeCriteriaQuery
import com.faire.yawn.util.YawnContext
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName

/**
 * Generates: `typealias DbBookProjectedCriteriaQuery<PROJECTION> = ProjectedTypeSafeCriteriaQuery<DbBook, *, *, PROJECTION>`
 */
internal object ProjectedTypeSafeCriteriaQueryTypeAliasGenerator : YawnTableDefTypeAliasGenerator {
    private val projectionTypeVariable = TypeVariableName("PROJECTION", ANY.copy(nullable = true))

    override fun getName(entityType: ClassName): String = "${entityType.getUniqueSimpleName()}ProjectedCriteriaQuery"

    override fun getType(entityType: ClassName, tableDefType: ParameterizedTypeName): ParameterizedTypeName {
        return ProjectedTypeSafeCriteriaQuery::class.asClassName().parameterizedBy(
            entityType,
            STAR,
            STAR,
            projectionTypeVariable,
        )
    }

    override fun TypeAliasSpec.Builder.additionalTypeAliasBuilder(yawnContext: YawnContext): TypeAliasSpec.Builder {
        return addTypeVariable(projectionTypeVariable)
    }
}
