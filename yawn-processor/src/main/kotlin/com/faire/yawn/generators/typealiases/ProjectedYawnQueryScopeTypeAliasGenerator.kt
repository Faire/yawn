package com.faire.yawn.generators.typealiases

import com.faire.ksp.getUniqueSimpleName
import com.faire.yawn.criteria.query.ProjectedYawnQueryScope
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
 * Generates: `typealias DbBookProjectedQueryScope<PROJECTION> = ProjectedYawnQueryScope<*, DbBook, *, PROJECTION>`
 */
internal object ProjectedYawnQueryScopeTypeAliasGenerator : YawnTableDefTypeAliasGenerator {
    private val projectionTypeVariable = TypeVariableName("PROJECTION", ANY.copy(nullable = true))

    override fun getName(entityType: ClassName): String = "${entityType.getUniqueSimpleName()}ProjectedQueryScope"

    override fun getType(entityType: ClassName, tableDefType: ParameterizedTypeName): ParameterizedTypeName {
        return ProjectedYawnQueryScope::class.asClassName().parameterizedBy(
            STAR,
            entityType,
            STAR,
            projectionTypeVariable,
        )
    }

    override fun getAdditionalTypeAliasBuilder(
        yawnContext: YawnContext,
    ): TypeAliasSpec.Builder.() -> TypeAliasSpec.Builder = {
        addTypeVariable(projectionTypeVariable)
    }
}
