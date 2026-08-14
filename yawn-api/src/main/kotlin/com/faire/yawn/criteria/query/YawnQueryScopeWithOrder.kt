package com.faire.yawn.criteria.query

import com.faire.yawn.project.YawnPathProvider
import com.faire.yawn.query.YawnQuery
import com.faire.yawn.query.YawnQueryOrder

/**
 * A delegatable interface for Query DSL classes supporting ORDER clauses (via [order], etc.).
 * This serves [EntityYawnQueryScope], [ProjectionYawnQueryScope] and [ProjectedYawnQueryScope].
 *
 * [orderAsc]/[orderDesc] accept a plain [com.faire.yawn.YawnTableDef.ColumnDef] as well as a projected/aggregate
 * expression wrapped via [com.faire.yawn.project.orderable] (e.g. to order grouped rows by `max(createdAt)`).
 */
sealed interface YawnQueryScopeWithOrder<SOURCE : Any, T : Any> {
    fun order(vararg orders: YawnQueryOrder<SOURCE>)
    fun orderAsc(property: YawnPathProvider<SOURCE>)
    fun orderDesc(property: YawnPathProvider<SOURCE>)
}

internal class YawnQueryScopeWithOrderDelegate<SOURCE : Any, T : Any>(
    private val query: YawnQuery<SOURCE, T>,
) : YawnQueryScopeWithOrder<SOURCE, T> {
    override fun order(vararg orders: YawnQueryOrder<SOURCE>) {
        for (order in orders) {
            query.orders.add(order)
        }
    }

    override fun orderAsc(property: YawnPathProvider<SOURCE>) {
        order(YawnQueryOrder.asc(property))
    }

    override fun orderDesc(property: YawnPathProvider<SOURCE>) {
        order(YawnQueryOrder.desc(property))
    }
}
