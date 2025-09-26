package com.faire.yawn.criteria.query

import com.faire.yawn.project.YawnProjectionDef
import com.faire.yawn.query.YawnQuery

/**
 * A type-safe Yawn queries DSL for further refining projections.
 *
 * It includes only the base methods (addEq, etc.).
 *
 * @param T the type of the entity being queried.
 * @param DEF the table definition of the entity being queried.
 */
class ProjectionTypeSafeCriteriaQuery<T : Any, DEF : YawnProjectionDef<T, T>> private constructor(
    query: YawnQuery<T, T>,
) : BaseTypeSafeCriteriaQuery<T, T, DEF>(query),
    TypeSafeCriteriaWithWhere<T, T> by TypeSafeCriteriaWithWhereDelegate(query),
    TypeSafeCriteriaWithOrder<T, T> by TypeSafeCriteriaWithOrderDelegate(query) {
    companion object {
        @Suppress("unused")
        fun <T : Any, DEF : YawnProjectionDef<T, T>> create(
            tableDef: DEF,
            query: YawnQuery<T, T>,
            lambda: ProjectionTypeSafeCriteriaQuery<T, DEF>.(tableDef: DEF) -> Unit,
        ): ProjectionTypeSafeCriteriaQuery<T, DEF> {
            val typeSafeCriteria = ProjectionTypeSafeCriteriaQuery<T, DEF>(query)
            typeSafeCriteria.lambda(tableDef)
            return typeSafeCriteria
        }
    }
}
