package com.faire.yawn.criteria.query

import com.faire.yawn.YawnTableDef
import com.faire.yawn.YawnTableDefParent.AssociationTableDefParent
import com.faire.yawn.query.YawnQuery
import org.hibernate.sql.JoinType

/**
 * A type-safe Yawn queries DSL that supports joins.
 * This serves for both [TypeSafeCriteriaQuery] and [ProjectionTypeSafeCriteriaQuery].
 */
sealed interface TypeSafeCriteriaWithJoin<SOURCE : Any, T : Any> {
  fun <F : Any, D : YawnTableDef<SOURCE, F>> join(
      column: YawnTableDef<SOURCE, *>.JoinColumnDef<F, D>,
      joinType: JoinType = JoinType.INNER_JOIN,
      lambda: JoinTypeSafeCriteriaQuery<SOURCE, F, D>.(tableDef: D) -> Unit = {},
  ): D
}

internal class TypeSafeCriteriaWithJoinDelegate<SOURCE : Any, T : Any>(
    private val query: YawnQuery<SOURCE, T>,
) : TypeSafeCriteriaWithJoin<SOURCE, T> {
  override fun <F : Any, D : YawnTableDef<SOURCE, F>> join(
      column: YawnTableDef<SOURCE, *>.JoinColumnDef<F, D>,
      joinType: JoinType,
      lambda: JoinTypeSafeCriteriaQuery<SOURCE, F, D>.(tableDef: D) -> Unit,
  ): D {
    val join = query.registerJoin(column, joinType, lambda)
    return column.joinTableDef(join.parent)
  }

  internal fun <D : YawnTableDef<SOURCE, F>, F : Any> registerJoin(
      column: YawnTableDef<SOURCE, *>.JoinColumnDef<F, D>,
      joinType: JoinType,
  ): AssociationTableDefParent {
    return query.registerJoin(column, joinType).parent
  }
}
