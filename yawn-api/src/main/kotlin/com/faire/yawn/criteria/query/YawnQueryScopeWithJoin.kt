package com.faire.yawn.criteria.query

import com.faire.yawn.YawnTableDef
import com.faire.yawn.YawnTableDefParent.AssociationTableDefParent
import com.faire.yawn.query.YawnQuery
import org.hibernate.sql.JoinType

/**
 * A delegatable interface for Query DSL classes supporting JOIN clauses (via [join]).
 * This serves for both [EntityYawnQueryScope] and [ProjectionYawnQueryScope].
 */
sealed interface YawnQueryScopeWithJoin<SOURCE : Any, T : Any> {
    fun <F : Any, D : YawnTableDef<SOURCE, F>> join(
        column: YawnTableDef<SOURCE, *>.JoinColumnDef<F, D>,
        joinType: JoinType = JoinType.INNER_JOIN,
        lambda: JoinYawnQueryScope<SOURCE, F, D>.(tableDef: D) -> Unit = {},
    ): D
}

internal class YawnQueryScopeWithJoinDelegate<SOURCE : Any, T : Any>(
    private val query: YawnQuery<SOURCE, T>,
) : YawnQueryScopeWithJoin<SOURCE, T> {
    override fun <F : Any, D : YawnTableDef<SOURCE, F>> join(
        column: YawnTableDef<SOURCE, *>.JoinColumnDef<F, D>,
        joinType: JoinType,
        lambda: JoinYawnQueryScope<SOURCE, F, D>.(tableDef: D) -> Unit,
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
