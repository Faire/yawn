package com.faire.yawn.criteria.query

import com.faire.yawn.project.AliasedYawnQueryProjection
import com.faire.yawn.project.YawnPathProvider
import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.query.YawnQuery
import com.faire.yawn.query.YawnQueryOrder

/**
 * A delegatable interface for Query DSL classes supporting ORDER clauses (via [order], etc.).
 * This serves [EntityYawnQueryScope], [ProjectionYawnQueryScope] and [ProjectedYawnQueryScope].
 *
 * [orderAsc]/[orderDesc] accept a plain [com.faire.yawn.YawnTableDef.ColumnDef]. To order by a projected/aggregate
 * expression instead (e.g. `max(createdAt)`), see [orderAscBy]/[orderDescBy] below - a distinct name is needed
 * since a member function of this name would otherwise shadow an extension of the same name entirely, regardless
 * of parameter types.
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

/**
 * Orders by a projected/aggregate expression (e.g. `YawnProjections.max(col)`), ascending.
 *
 * Unlike a plain column, such an expression has no property name of its own for Hibernate to order by, so this
 * assigns it a unique SQL alias internally and returns the now-orderable projection - pass the *returned*
 * instance to `project(...)` (nesting it inside a `pair`/`triple`/`@YawnProjection` data class is fine), since
 * Hibernate can only order by an alias that is actually present in the query's SELECT list.
 *
 * ```kotlin
 * yawn.project(VisitTable) { visits ->
 *     val mostRecentVisit = orderDescBy(YawnProjections.max(visits.createdAt))
 *     project(YawnProjections.pair(YawnProjections.groupBy(visits.brandId), mostRecentVisit))
 * }.list()
 * ```
 */
fun <SOURCE : Any, TO> YawnQueryScopeWithOrder<SOURCE, *>.orderAscBy(
    projection: YawnQueryProjection<SOURCE, TO>,
): YawnQueryProjection<SOURCE, TO> {
    val aliased = AliasedYawnQueryProjection(projection)
    order(YawnQueryOrder.asc(aliased))
    return aliased
}

/** Descending counterpart of [orderAscBy]. */
fun <SOURCE : Any, TO> YawnQueryScopeWithOrder<SOURCE, *>.orderDescBy(
    projection: YawnQueryProjection<SOURCE, TO>,
): YawnQueryProjection<SOURCE, TO> {
    val aliased = AliasedYawnQueryProjection(projection)
    order(YawnQueryOrder.desc(aliased))
    return aliased
}
