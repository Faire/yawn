package com.faire.yawn.criteria.query

import com.faire.yawn.YawnTableDef
import com.faire.yawn.query.YawnQuery
import com.faire.yawn.query.YawnQueryOrder

/**
 * A delegatable interface for Query DSL classes supporting ORDER clauses (via [order], etc.).
 * This serves [EntityYawnQueryScope], [ProjectionYawnQueryScope] and [ProjectedYawnQueryScope].
 */
sealed interface YawnQueryScopeWithOrder<SOURCE : Any, T : Any> {
    fun order(vararg orders: YawnQueryOrder<SOURCE>)
    fun orderAsc(property: YawnTableDef<SOURCE, *>.ColumnDef<*>)
    fun orderDesc(property: YawnTableDef<SOURCE, *>.ColumnDef<*>)
}

internal class YawnQueryScopeWithOrderDelegate<SOURCE : Any, T : Any>(
    private val query: YawnQuery<SOURCE, T>,
) : YawnQueryScopeWithOrder<SOURCE, T> {
    override fun order(vararg orders: YawnQueryOrder<SOURCE>) {
        for (order in orders) {
            query.orders.add(order)
        }
    }

    override fun orderAsc(property: YawnTableDef<SOURCE, *>.ColumnDef<*>) {
        order(YawnQueryOrder.asc(property))
    }

    override fun orderDesc(property: YawnTableDef<SOURCE, *>.ColumnDef<*>) {
        order(YawnQueryOrder.desc(property))
    }
}
