package com.faire.yawn.criteria.query

import com.faire.yawn.YawnTableDef
import com.faire.yawn.query.YawnQuery

/**
 * A context providing the DSL for Yawn entity queries (i.e. without a projection).
 *
 * It supports WHERE (via [addEq], etc.), JOIN (via [join], etc.), and ORDER clauses (via [order], etc.).
 *
 * @param SOURCE the type of the entity being queried.
 * @param DEF the table definition of the entity being queried.
 */
class EntityYawnQueryScope<SOURCE : Any, DEF : YawnTableDef<SOURCE, SOURCE>> private constructor(
    query: YawnQuery<SOURCE, SOURCE>,
) : BaseYawnQueryScope<SOURCE, SOURCE, DEF>(query),
    YawnQueryScopeWithWhere<SOURCE, SOURCE> by YawnQueryScopeWithWhereDelegate(query),
    YawnQueryScopeWithJoin<SOURCE, SOURCE> by YawnQueryScopeWithJoinDelegate(query),
    YawnQueryScopeWithOrder<SOURCE, SOURCE> by YawnQueryScopeWithOrderDelegate(query) {
    companion object {
        internal fun <T : Any, DEF : YawnTableDef<T, T>> applyLambda(
            query: YawnQuery<T, T>,
            lambda: EntityYawnQueryScope<T, DEF>.() -> Unit,
        ): EntityYawnQueryScope<T, DEF> {
            return EntityYawnQueryScope<T, DEF>(query).apply(lambda)
        }
    }
}
