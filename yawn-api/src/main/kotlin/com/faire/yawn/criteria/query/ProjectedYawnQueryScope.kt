package com.faire.yawn.criteria.query

import com.faire.yawn.YawnTableDef
import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.query.YawnQuery

/**
 * A context providing the DSL for Yawn projected queries.
 *
 * It supports WHERE (via [addEq], etc.), JOIN (via [join], etc.), and ORDER clauses (via [order], etc.).
 * Since this refines a projected query, it requires calling the [project] method to return the projection of the query,
 * and the [PROJECTION] type parameter is different from [T].
 *
 * @param T the type of the entity being queried.
 * @param DEF the table definition of the entity being queried.
 * @param PROJECTION the type being projected to (i.e. the result of the query).
 */
class ProjectedYawnQueryScope<SOURCE : Any, T : Any, DEF : YawnTableDef<SOURCE, T>, PROJECTION : Any?>
private constructor(
    query: YawnQuery<SOURCE, T>,
) : BaseYawnQueryScope<SOURCE, T, DEF>(query),
    YawnQueryScopeWithWhere<SOURCE, T> by YawnQueryScopeWithWhereDelegate(query),
    YawnQueryScopeWithJoin<SOURCE, T> by YawnQueryScopeWithJoinDelegate(query),
    YawnQueryScopeWithOrder<SOURCE, T> by YawnQueryScopeWithOrderDelegate(query) {

    private var projectionCalled: Boolean = false

    private fun ensureUniqueProjection() {
        if (projectionCalled) {
            error("Projection already called")
        } else {
            projectionCalled = true
        }
    }

    fun project(
        projection: YawnQueryProjection<SOURCE, PROJECTION>,
    ): YawnQueryProjection<SOURCE, PROJECTION> {
        ensureUniqueProjection()
        return projection
    }

    companion object {
        internal fun <SOURCE : Any, T : Any, DEF : YawnTableDef<SOURCE, T>, PROJECTION : Any?> applyLambda(
            query: YawnQuery<SOURCE, T>,
            lambda: ProjectedYawnQueryScope<SOURCE, T, DEF, PROJECTION>.() -> Unit,
        ): ProjectedYawnQueryScope<SOURCE, T, DEF, PROJECTION> {
            return ProjectedYawnQueryScope<SOURCE, T, DEF, PROJECTION>(query).apply(lambda)
        }
    }
}
