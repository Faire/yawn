package com.faire.yawn.criteria.query

import com.faire.yawn.YawnTableDef
import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.query.YawnQuery

/**
 * A type-safe Yawn queries DSL specifically for projections.
 *
 * It includes the base methods (addEq, etc.), but also include the `join` methods.
 * Also, the [PROJECTION] type parameter is different from [T], because we are projecting.
 *
 * @param T the type of the entity being queried.
 * @param DEF the table definition of the entity being queried.
 * @param PROJECTION the type being projected to (i.e. the result of the query).
 */
class ProjectedTypeSafeCriteriaQuery<SOURCE : Any, T : Any, DEF : YawnTableDef<SOURCE, T>, PROJECTION : Any?>
private constructor(
    query: YawnQuery<SOURCE, T>,
) : BaseTypeSafeCriteriaQuery<SOURCE, T, DEF>(query),
    TypeSafeCriteriaWithWhere<SOURCE, T> by TypeSafeCriteriaWithWhereDelegate(query),
    TypeSafeCriteriaWithJoin<SOURCE, T> by TypeSafeCriteriaWithJoinDelegate(query),
    TypeSafeCriteriaWithOrder<SOURCE, T> by TypeSafeCriteriaWithOrderDelegate(query) {

    private var projectionCalled: Boolean = false

    private fun ensureUniqueProjection() {
        if (projectionCalled) {
            throw IllegalStateException("Projection already called")
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
            lambda: ProjectedTypeSafeCriteriaQuery<SOURCE, T, DEF, PROJECTION>.() -> Unit,
        ): ProjectedTypeSafeCriteriaQuery<SOURCE, T, DEF, PROJECTION> {
            return ProjectedTypeSafeCriteriaQuery<SOURCE, T, DEF, PROJECTION>(query).apply(lambda)
        }
    }
}
