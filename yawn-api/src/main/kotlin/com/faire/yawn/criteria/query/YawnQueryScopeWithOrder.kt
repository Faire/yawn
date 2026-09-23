package com.faire.yawn.criteria.query

import com.faire.yawn.project.AliasedYawnValueProjector
import com.faire.yawn.project.ProjectionSlot
import com.faire.yawn.project.YawnPathProvider
import com.faire.yawn.project.YawnValueProjector
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
 * Unlike a plain column, such an expression has no name of its own to order by, so this gives it one and returns
 * the now-orderable projection - pass the *returned* instance to `project(...)` (nesting it inside a
 * `pair`/`triple`/`@YawnProjection` data class is fine). It can only be ordered by once it's also selected, so if
 * the returned value isn't projected, resolving the order will fail at query time.
 *
 * ```kotlin
 * yawn.project(VisitTable) { visits ->
 *     val mostRecentVisit = orderDescBy(YawnProjections.max(visits.createdAt))
 *     project(YawnProjections.pair(YawnProjections.groupBy(visits.brandId), mostRecentVisit))
 * }.list()
 * ```
 */
fun <SOURCE : Any, TO> YawnQueryScopeWithOrder<SOURCE, *>.orderAscBy(
    projection: YawnValueProjector<SOURCE, TO>,
): YawnValueProjector<SOURCE, TO> {
    val aliased = AliasedYawnValueProjector(projection)
    order(YawnQueryOrder.asc(aliased))
    return aliased
}

/** Descending counterpart of [orderAscBy]. */
fun <SOURCE : Any, TO> YawnQueryScopeWithOrder<SOURCE, *>.orderDescBy(
    projection: YawnValueProjector<SOURCE, TO>,
): YawnValueProjector<SOURCE, TO> {
    val aliased = AliasedYawnValueProjector(projection)
    order(YawnQueryOrder.desc(aliased))
    return aliased
}

/**
 * Orders by one child of an orderable composite (e.g. [com.faire.yawn.project.YawnProjectionPair], built via
 * [com.faire.yawn.project.YawnProjections.orderablePair]), ascending.
 *
 * Unlike [orderAscBy] above, there is no return value to thread back into `project(...)`: [slot] is replaced with
 * its aliased wrapper in place, so the composite picks it up automatically the next time it is resolved. Call this
 * from the configuring block of [ProjectedYawnQueryScope.project]'s two-argument overload, after the composite
 * itself has already been passed to `project(...)`:
 *
 * ```kotlin
 * project(
 *     YawnProjections.orderablePair(YawnProjections.groupBy(visits.brandId), YawnProjections.max(visits.createdAt)),
 * ) { pair ->
 *     orderDescBy(pair.second)
 * }
 * ```
 */
fun <SOURCE : Any, TO> YawnQueryScopeWithOrder<SOURCE, *>.orderAscBy(
    slot: ProjectionSlot<SOURCE, TO>,
) {
    val aliased = AliasedYawnValueProjector(slot.current)
    slot.current = aliased
    order(YawnQueryOrder.asc(aliased))
}

/** Descending counterpart of the [ProjectionSlot] overload of [orderAscBy]. */
fun <SOURCE : Any, TO> YawnQueryScopeWithOrder<SOURCE, *>.orderDescBy(
    slot: ProjectionSlot<SOURCE, TO>,
) {
    val aliased = AliasedYawnValueProjector(slot.current)
    slot.current = aliased
    order(YawnQueryOrder.desc(aliased))
}
