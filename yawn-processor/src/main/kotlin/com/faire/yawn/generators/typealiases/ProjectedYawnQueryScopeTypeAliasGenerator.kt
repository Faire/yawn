package com.faire.yawn.generators.typealiases

import com.faire.ksp.getUniqueSimpleName
import com.faire.yawn.criteria.query.ProjectedYawnQueryScope
import com.faire.yawn.util.YawnContext
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName

/**
 * Generates:
 * `typealias DbBookProjectedQueryScope<PROJECTION> = ProjectedYawnQueryScope<DbBook, DbBook, DbBookTableDefType, PROJECTION>`
 *
 * For sharing a fragment of a projected query:
 * ```
 * private fun DbBookProjectedQueryScope<Long>.totalPagesByAuthor(books: DbBookTableDefType): ... {
 *     val authors = join(books.author)
 *     ...
 * }
 * ```
 *
 * The source is pinned to the entity rather than left open, since a scope whose source is unknown cannot use any of
 * the DSL: every column-taking method is parameterized by it. That covers a projected query rooted at its own
 * entity, which is all but a handful of them; a projected *subquery* takes its source from the enclosing query
 * instead (see [com.faire.yawn.criteria.query.BaseYawnQueryScope.createProjectedSubQuery]) and spells
 * [ProjectedYawnQueryScope] out in full.
 */
internal object ProjectedYawnQueryScopeTypeAliasGenerator : YawnTableDefTypeAliasGenerator {
    private val projectionTypeVariable = TypeVariableName("PROJECTION", ANY.copy(nullable = true))

    override fun getName(entityType: ClassName): String = "${entityType.getUniqueSimpleName()}ProjectedQueryScope"

    override fun getType(entityType: ClassName, tableDefType: ParameterizedTypeName): ParameterizedTypeName {
        return ProjectedYawnQueryScope::class.asClassName().parameterizedBy(
            entityType,
            entityType,
            tableDefType,
            projectionTypeVariable,
        )
    }

    override fun getAdditionalTypeAliasBuilder(
        yawnContext: YawnContext,
    ): TypeAliasSpec.Builder.() -> TypeAliasSpec.Builder = {
        addTypeVariable(projectionTypeVariable)
    }
}
