package com.faire.yawn.project

import com.faire.yawn.query.YawnCompilationContext

/**
 * Selects a single-value projection under a unique SQL alias, and is itself the [YawnPathProvider] naming it.
 *
 * This is an internal implementation detail of [com.faire.yawn.criteria.query.orderAscBy]/
 * [com.faire.yawn.criteria.query.orderDescBy] (see those for the public entry point to order by a projected/
 * aggregate expression) and should never be constructed directly: Hibernate can only resolve `ORDER BY` against an
 * alias that is present in the query's own SELECT list (unlike a plain mapped column, an aggregate or other
 * projected expression has no property name of its own to order by), so those functions return this same
 * instance for the caller to pass to `project(...)`, ensuring the alias is actually selected.
 *
 * The alias is generated lazily via [YawnCompilationContext.generateResultAlias] and cached per context - rather
 * than assigned once at construction - because the same query (and so the same instance of this class) can be
 * compiled more than once, e.g. re-executed, or cloned for a separate count query. Each compilation gets its own
 * alias, but caching per context means it doesn't matter whether the SELECT list or the ORDER BY clause happens
 * to compile this projection first within a single compilation - both resolve to the same alias.
 */
internal class AliasedYawnValueProjector<SOURCE : Any, TO>(
    private val projection: YawnValueProjector<SOURCE, TO>,
) : YawnValueProjector<SOURCE, TO>, YawnPathProvider<SOURCE> {
    private var aliasContext: YawnCompilationContext? = null
    private var alias: String? = null

    private fun aliasFor(context: YawnCompilationContext): String {
        if (aliasContext !== context) {
            aliasContext = context
            alias = context.generateResultAlias()
        }
        return checkNotNull(alias)
    }

    override fun projection(): ProjectionNode.Value<SOURCE, TO> {
        val inner = projection.projection()
        return ProjectionNode.Value(ProjectionLeaf.Aliased(inner.leaf, this), inner.mapper)
    }

    override fun generatePath(context: YawnCompilationContext): String = aliasFor(context)
}
