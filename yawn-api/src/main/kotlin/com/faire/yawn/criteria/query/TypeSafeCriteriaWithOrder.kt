package com.faire.yawn.criteria.query

import com.faire.yawn.YawnTableDef
import com.faire.yawn.query.YawnQuery
import com.faire.yawn.query.YawnQueryOrder

/**
 * A type-safe Yawn queries DSL that supports orders.
 * This serves for both [TypeSafeCriteriaQuery], [ProjectionTypeSafeCriteriaQuery] and [ProjectedTypeSafeCriteriaQuery].
 */
sealed interface TypeSafeCriteriaWithOrder<SOURCE : Any, T : Any> {
    fun order(vararg orders: YawnQueryOrder<SOURCE>)
    fun orderAsc(property: YawnTableDef<SOURCE, *>.ColumnDef<*>)
    fun orderDesc(property: YawnTableDef<SOURCE, *>.ColumnDef<*>)
}

internal class TypeSafeCriteriaWithOrderDelegate<SOURCE : Any, T : Any>(
    private val query: YawnQuery<SOURCE, T>,
) : TypeSafeCriteriaWithOrder<SOURCE, T> {
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
