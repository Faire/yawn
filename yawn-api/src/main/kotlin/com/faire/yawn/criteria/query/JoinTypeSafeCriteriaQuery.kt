package com.faire.yawn.criteria.query

import com.faire.yawn.YawnTableDef
import com.faire.yawn.query.YawnCriteriaQuery

class JoinTypeSafeCriteriaQuery<SOURCE : Any, T : Any, DEF : YawnTableDef<SOURCE, T>> private constructor(
    query: YawnCriteriaQuery<SOURCE, T>,
) : BaseTypeSafeCriteriaQuery<SOURCE, T, DEF>(query),
    TypeSafeCriteriaWithWhere<SOURCE, T> by TypeSafeCriteriaWithWhereDelegate(query) {
  companion object {
    internal fun <SOURCE : Any, T : Any, DEF : YawnTableDef<SOURCE, T>> applyLambda(
        query: YawnCriteriaQuery<SOURCE, T>,
        tableDef: DEF,
        lambda: JoinTypeSafeCriteriaQuery<SOURCE, T, DEF>.(tableDef: DEF) -> Unit,
    ): JoinTypeSafeCriteriaQuery<SOURCE, T, DEF> {
      return JoinTypeSafeCriteriaQuery<SOURCE, T, DEF>(query).apply { lambda(tableDef) }
    }
  }
}
