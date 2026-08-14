package com.faire.yawn.project

import com.faire.yawn.query.YawnCompilationContext
import org.hibernate.criterion.Projection
import org.hibernate.criterion.Projections
import java.util.concurrent.atomic.AtomicLong

/**
 * Wraps a [YawnQueryProjection] with a unique SQL alias, so that it can also be used as a
 * [com.faire.yawn.query.YawnQueryOrder] target via [YawnPathProvider] - see [orderable].
 *
 * Hibernate can only resolve `ORDER BY` against an alias that is present in the query's own SELECT list (unlike a
 * plain mapped column, an aggregate or other projected expression has no property name of its own to order by).
 * The *same* [AliasedYawnQueryProjection] instance returned by [orderable] must therefore be passed to `project(...)`
 * (directly, or nested inside a `pair`/`triple`/`@YawnProjection` data class/etc.) for ordering by it to work -
 * the alias is otherwise never selected, and Hibernate will fail to resolve it at query time.
 */
class AliasedYawnQueryProjection<SOURCE : Any, TO> internal constructor(
    private val projection: YawnQueryProjection<SOURCE, TO>,
) : YawnQueryProjection<SOURCE, TO>, YawnPathProvider<SOURCE> {
    private val alias: String = "${ALIAS_PREFIX}${aliasCounter.getAndIncrement()}"

    override fun compile(context: YawnCompilationContext): Projection {
        return Projections.alias(projection.compile(context), alias)
    }

    override fun project(value: Any?): TO = projection.project(value)

    override fun generatePath(context: YawnCompilationContext): String = alias

    private companion object {
        private const val ALIAS_PREFIX = "yawn_orderable_"
        private val aliasCounter = AtomicLong()
    }
}

/**
 * Wraps this projection with a unique alias so it can be used as an ORDER BY target, most commonly to sort grouped
 * rows by an aggregate (e.g. ordering `groupBy(brandId)` rows by their `max(createdAt)`, to get the most recently
 * touched groups first) - something a plain mapped column doesn't need, since it can already be ordered by directly.
 *
 * ```kotlin
 * val mostRecentVisit = YawnProjections.max(visits.createdAt).orderable()
 * session.project(VisitTable) { visits ->
 *     order(YawnQueryOrder.desc(mostRecentVisit))
 *     project(YawnProjections.pair(YawnProjections.groupBy(visits.brandId), mostRecentVisit))
 * }.list()
 * ```
 *
 * See [AliasedYawnQueryProjection] for why the same returned instance must also be passed to `project(...)`.
 */
fun <SOURCE : Any, TO> YawnQueryProjection<SOURCE, TO>.orderable(): AliasedYawnQueryProjection<SOURCE, TO> {
    return AliasedYawnQueryProjection(this)
}
