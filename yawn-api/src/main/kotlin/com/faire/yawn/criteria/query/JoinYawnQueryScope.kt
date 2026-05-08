package com.faire.yawn.criteria.query

import com.faire.yawn.YawnTableDef
import com.faire.yawn.query.YawnQueryWithCriterion

class JoinYawnQueryScope<SOURCE : Any, T : Any, DEF : YawnTableDef<SOURCE, T>> private constructor(
    query: YawnQueryWithCriterion<SOURCE, T>,
) : BaseYawnQueryScope<SOURCE, T, DEF>(query),
    YawnQueryScopeWithWhere<SOURCE, T> by YawnQueryScopeWithWhereDelegate(query) {
    companion object {
        internal fun <SOURCE : Any, T : Any, DEF : YawnTableDef<SOURCE, T>> applyLambda(
            query: YawnQueryWithCriterion<SOURCE, T>,
            tableDef: DEF,
            lambda: JoinYawnQueryScope<SOURCE, T, DEF>.(tableDef: DEF) -> Unit,
        ): JoinYawnQueryScope<SOURCE, T, DEF> {
            return JoinYawnQueryScope<SOURCE, T, DEF>(query).apply { lambda(tableDef) }
        }
    }
}
