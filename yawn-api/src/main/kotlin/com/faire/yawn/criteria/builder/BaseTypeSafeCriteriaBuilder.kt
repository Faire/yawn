package com.faire.yawn.criteria.builder

import com.faire.yawn.YawnTableDef
import com.faire.yawn.pagination.Page
import com.faire.yawn.pagination.PageNumber
import com.faire.yawn.query.CompiledYawnQuery
import com.faire.yawn.query.YawnQuery
import com.faire.yawn.query.YawnQueryFactory
import com.faire.yawn.query.YawnQueryHint
import com.faire.yawn.query.YawnQueryOrder

/**
 * An abstract super-class for the type-safe Yawn criteria builder; not be used directly.
 *
 * This will be either:
 * * [TypeSafeCriteriaBuilder], for normal queries (supports applyFilter {})
 * * [ProjectedTypeSafeCriteriaBuilder], for queries with projections (does not support extra applyFilter {})
 *
 * The reason for the distinction is that the latter does not support applyFilter, due to the fact that only
 * the lambda returning the projection can be applied. For non-projected queries, there is no problem in applying
 * multiple lambdas.
 *
 * @param T the type of the entity being queried.
 * @param DEF the table definition of the entity being queried.
 * @param RETURNS the return type to be used for the finalizer methods (list, uniqueResult, etc.).
 *                This will either be T for TypeSafeCriteriaBuilder, or the type of the projection for
 *                ProjectedTypeSafeCriteriaBuilder.
 * @param CRITERIA is the concrete type of the criteria being used;
 *        this is required to specify the return type of the builder-style methods.
 *        It will be one of the above-mentioned types.
 */
abstract class BaseTypeSafeCriteriaBuilder<
    T : Any,
    DEF : YawnTableDef<T, T>,
    RETURNS : Any?,
    CRITERIA : BaseTypeSafeCriteriaBuilder<T, DEF, RETURNS, CRITERIA>,
    >(
    protected val tableDef: DEF,
    protected val queryFactory: YawnQueryFactory,
    protected val query: YawnQuery<T, T>,
    // TODO(yawn): we need to consider if there is any performance impact to applying the mapper even if there
    // are no projections. while for uniqueResult we always need apply the cast, for the list we could
    // potentially skip an iteration.
    protected var mapper: (Any?) -> RETURNS = { value ->
        @Suppress("UNCHECKED_CAST")
        value as RETURNS
    },
) {
    /**
     * Following the "builder" pattern, each method on the various TypedCriteriaBuilders returns itself.
     * However, since we have an inheritance chain, in order for the type to match what you had before, we need this
     * override.
     * This should always just return `this`, but will ensure the correct [CRITERIA] typing.
     */
    protected abstract fun builderReturn(): CRITERIA

    /**
     * The various TypedCriteriaBuilders are mutable, which means changes are accumulated within the instance.
     * If you want to run two different queries with the same base, you can either create e method that returns
     * a new "base" instance each time, or, if you already have the instance, you can use the clone method.
     * Note that this relies on the data class `copy()` method being correctly implemented in the underlying
     * [YawnQuery] class.
     */
    protected abstract fun clone(): CRITERIA

    fun list(): List<RETURNS> {
        return compile().list().map { mapper(it) }
    }

    fun set(): Set<RETURNS> {
        return compile().list().asSequence()
            .map { mapper(it) }
            .toSet()
    }

    fun uniqueResult(): RETURNS? {
        return compile().uniqueResult()?.let { mapper(it) }
    }

    fun first(): RETURNS? {
        return maxResults(1).uniqueResult()
    }

    fun compile(): CompiledYawnQuery<T> {
        return queryFactory.compile(query, tableDef)
    }

    fun offset(offset: Int): CRITERIA {
        query.offset = offset
        return builderReturn()
    }

    fun maxResults(maxResults: Int): CRITERIA {
        query.maxResults = maxResults
        return builderReturn()
    }

    fun maxBy(orderProperty: DEF.() -> YawnTableDef<T, *>.ColumnDef<*>): RETURNS? = maxBy(*arrayOf(orderProperty))

    fun maxBy(vararg orderProperties: DEF.() -> YawnTableDef<T, *>.ColumnDef<*>): RETURNS? {
        val orders = orderProperties.map { YawnQueryOrder.desc(it(tableDef)) }
        query.orders.addAll(orders)

        return maxResults(1).uniqueResult()
    }

    fun minBy(orderProperty: DEF.() -> YawnTableDef<T, *>.ColumnDef<*>): RETURNS? = minBy(*arrayOf(orderProperty))

    fun minBy(vararg orderProperties: DEF.() -> YawnTableDef<T, *>.ColumnDef<*>): RETURNS? {
        val orders = orderProperties.map { YawnQueryOrder.asc(it(tableDef)) }
        query.orders.addAll(orders)

        return maxResults(1).uniqueResult()
    }

    @Deprecated("Use paginate with Page instead.")
    fun paginateZeroIndexed(
        pageNumber: Int,
        pageSize: Int,
        orders: List<DEF.() -> YawnQueryOrder<T>>,
    ): CRITERIA {
        val page = PageNumber.zeroIndexed(pageNumber) / pageSize
        return paginate(page, orders)
    }

    fun paginate(
        page: Page,
        orders: List<DEF.() -> YawnQueryOrder<T>>,
    ): CRITERIA {
        applyOrders(orders)
        return offset(page.computeOffset())
            .maxResults(page.pageSize)
    }

    @Deprecated("Use listPaginated with Page instead.")
    fun listPaginatedZeroIndexed(
        pageNumber: Int,
        pageSize: Int,
        orders: List<DEF.() -> YawnQueryOrder<T>>,
    ): List<RETURNS> {
        val page = PageNumber.zeroIndexed(pageNumber) / pageSize
        return listPaginated(page, orders)
    }

    fun listPaginated(
        page: Page,
        orders: List<DEF.() -> YawnQueryOrder<T>>,
    ): List<RETURNS> {
        return paginate(page, orders).list()
    }

    fun setPaginated(
        page: Page,
        orders: List<DEF.() -> YawnQueryOrder<T>>,
    ): Set<RETURNS> {
        return paginate(page, orders).set()
    }

    inline fun doPaginated(
        pageSize: Int,
        orders: List<DEF.() -> YawnQueryOrder<T>>,
        action: (List<RETURNS>) -> Unit,
    ) {
        // only apply the orders once
        applyOrders(orders)

        var page = PageNumber.starting() / pageSize
        do {
            val results = listPaginated(page, listOf())
            page = page.next()
            action(results)
        } while (results.size == pageSize)
    }

    fun listBatched(
        batchSize: Int,
        orders: List<DEF.() -> YawnQueryOrder<T>>,
    ): List<RETURNS> {
        return collectionBatched(batchSize, orders, results = mutableListOf())
    }

    fun setBatched(
        batchSize: Int,
        orders: List<DEF.() -> YawnQueryOrder<T>>,
    ): Set<RETURNS> {
        return collectionBatched(batchSize, orders, results = mutableSetOf())
    }

    private fun <C : MutableCollection<RETURNS>> collectionBatched(
        batchSize: Int,
        orders: List<DEF.() -> YawnQueryOrder<T>>,
        results: C,
    ): C {
        doPaginated(
            pageSize = batchSize,
            orders = orders,
            action = { results.addAll(it) },
        )
        return results
    }

    fun applyOrders(orders: List<DEF.() -> YawnQueryOrder<T>>): CRITERIA {
        query.orders.addAll(orders.map { it(tableDef) })
        return builderReturn()
    }

    /**
     * Currently, Yawn does not track database indexes in order to provide type-safe wrappers,
     * so query hints are just provided as Strings.
     */
    fun addQueryHint(hint: String): CRITERIA {
        query.queryHints.add(YawnQueryHint(hint))
        return builderReturn()
    }
}
