package com.faire.yawn.criteria.query

import com.faire.yawn.project.YawnProjectionDef
import com.faire.yawn.query.YawnQuery

/**
 * A context providing the DSL for further refining Yawn projections.
 *
 * It only supports WHERE clauses (via [addEq], etc.).
 *
 * @param T the type of the entity being queried.
 * @param DEF the table definition of the entity being queried.
 */
class ProjectionYawnQueryScope<T : Any, DEF : YawnProjectionDef<T, T>> private constructor(
    query: YawnQuery<T, T>,
) : BaseYawnQueryScope<T, T, DEF>(query),
    YawnQueryScopeWithWhere<T, T> by YawnQueryScopeWithWhereDelegate(query),
    YawnQueryScopeWithOrder<T, T> by YawnQueryScopeWithOrderDelegate(query) {
    companion object {
        @Suppress("unused")
        fun <T : Any, DEF : YawnProjectionDef<T, T>> create(
            tableDef: DEF,
            query: YawnQuery<T, T>,
            lambda: ProjectionYawnQueryScope<T, DEF>.(tableDef: DEF) -> Unit,
        ): ProjectionYawnQueryScope<T, DEF> {
            val criteria = ProjectionYawnQueryScope<T, DEF>(query)
            criteria.lambda(tableDef)
            return criteria
        }
    }
}
