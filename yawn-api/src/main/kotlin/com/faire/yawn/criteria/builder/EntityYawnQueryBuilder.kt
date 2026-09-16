package com.faire.yawn.criteria.builder

import com.faire.yawn.YawnDef
import com.faire.yawn.YawnTableDef
import com.faire.yawn.YawnTableDefParent.AssociationTableDefParent
import com.faire.yawn.criteria.join.EntityCriteriaWithJoinRef
import com.faire.yawn.criteria.query.EntityYawnQueryScope
import com.faire.yawn.criteria.query.ProjectedYawnQueryScope
import com.faire.yawn.criteria.query.YawnQueryScopeWithJoinDelegate
import com.faire.yawn.pagination.Page
import com.faire.yawn.pagination.PaginationResult
import com.faire.yawn.project.AggregateKind
import com.faire.yawn.project.ProjectionNode
import com.faire.yawn.project.YawnProjections
import com.faire.yawn.project.YawnProjector
import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.project.YawnValueProjector
import com.faire.yawn.query.YawnQuery
import com.faire.yawn.query.YawnQueryFactory
import com.faire.yawn.query.YawnQueryOrder
import org.hibernate.sql.JoinType

/**
 * A builder for Yawn entity queries (i.e. without projections), specification of [YawnQueryBuilder].
 *
 * Use the method [applyFilter] to further refine the query with methods such as `addEq`, etc.
 * That will give you a lambda context within [EntityYawnQueryScope] for query refinement.
 *
 * Use the method [applyProjection] to define a projection and switch to a [ProjectedYawnQueryBuilder].
 *
 * @param T the type of the entity being queried.
 * @param DEF the table definition of the entity being queried.
 */
class EntityYawnQueryBuilder<T : Any, DEF : YawnTableDef<T, T>>(
    tableDef: DEF,
    queryFactory: YawnQueryFactory,
    query: YawnQuery<T, T>,
) : YawnQueryBuilder<T, DEF, T, EntityYawnQueryBuilder<T, DEF>>(tableDef, queryFactory, query) {
    override fun builderReturn(): EntityYawnQueryBuilder<T, DEF> = this
    override fun clone(): EntityYawnQueryBuilder<T, DEF> {
        return EntityYawnQueryBuilder(tableDef, queryFactory, query.clone())
    }

    inner class YawnJoinRef<F : Any, D : YawnTableDef<T, F>>(
        private val columnDef: DEF.() -> YawnTableDef<T, *>.JoinColumnDef<F, D>,
        private val parent: AssociationTableDefParent,
    ) {
        fun get(tableDef: DEF): D {
            val column = tableDef.columnDef()
            return column.joinTableDef(parent)
        }
    }

    fun <F : Any, D : YawnTableDef<T, F>> joinRef(
        joinType: JoinType = JoinType.INNER_JOIN,
        columnDef: DEF.() -> YawnTableDef<T, *>.JoinColumnDef<F, D>,
    ): YawnJoinRef<F, D> {
        val joinColumnDef = tableDef.columnDef()
        val joinParent = YawnQueryScopeWithJoinDelegate(query).registerJoin(joinColumnDef, joinType = joinType)
        return YawnJoinRef(columnDef, joinParent)
    }

    fun <F : Any, D : YawnTableDef<T, F>> applyJoinRef(
        joinRef: YawnJoinRef<F, D>,
        lambda: EntityYawnQueryScope<T, DEF>.(joinedTableDef: D) -> Unit,
    ): EntityYawnQueryBuilder<T, DEF> {
        return applyFilter { tableDef ->
            val joinedTableDef = joinRef.get(tableDef)
            lambda(joinedTableDef)
        }
    }

    fun <F1 : Any, D1 : YawnTableDef<T, F1>, F2 : Any, D2 : YawnTableDef<T, F2>> applyJoinRefs(
        ref1: YawnJoinRef<F1, D1>,
        ref2: YawnJoinRef<F2, D2>,
        lambda: EntityYawnQueryScope<T, DEF>.(table1: D1, table2: D2) -> Unit,
    ): EntityYawnQueryBuilder<T, DEF> {
        return applyFilter { tableDef ->
            val table1 = ref1.get(tableDef)
            val table2 = ref2.get(tableDef)
            lambda(table1, table2)
        }
    }

    fun <F1 : Any, D1 : YawnTableDef<T, F1>, F2 : Any, D2 : YawnTableDef<T, F2>, F3 : Any, D3 : YawnTableDef<T, F3>> applyJoinRefs(
        ref1: YawnJoinRef<F1, D1>,
        ref2: YawnJoinRef<F2, D2>,
        ref3: YawnJoinRef<F3, D3>,
        lambda: EntityYawnQueryScope<T, DEF>.(table1: D1, table2: D2, table3: D3) -> Unit,
    ): EntityYawnQueryBuilder<T, DEF> {
        return applyFilter { tableDef ->
            val table1 = ref1.get(tableDef)
            val table2 = ref2.get(tableDef)
            val table3 = ref3.get(tableDef)
            lambda(table1, table2, table3)
        }
    }

    fun <RETURNS : Any?> applyProjection(
        lambda: ProjectedYawnQueryScope<T, T, DEF, RETURNS>.(tableDef: DEF) -> YawnQueryProjection<T, RETURNS>,
    ): ProjectedYawnQueryBuilder<T, DEF, RETURNS> {
        return ProjectedYawnQueryBuilder.create(tableDef, queryFactory, query, lambda)
    }

    fun countDistinct(
        uniqueColumn: DEF.() -> YawnTableDef<T, *>.ColumnDef<*>,
    ): Long {
        return applyProjection { table ->
            project(YawnProjections.countDistinct(table.uniqueColumn()))
        }.uniqueResult() ?: 0
    }

    /**
     * @param forceAnsiCompliance the page-of-keys query of the two-phase fetch always groups by the unique column
     * (see [avoidEagerFetchFanout]); `true` makes it `GROUP BY` every `ORDER BY` column as well, which ANSI SQL,
     * MySQL `ONLY_FULL_GROUP_BY`, H2 and Postgres require whenever an ordered column is not functionally dependent
     * on the grouped key (e.g. a column of a joined collection; a column of the root entity never needs it). It
     * implies the same two-phase fetch as [avoidEagerFetchFanout].
     *
     * Only the [orders] passed to this call are grouped, and each of them must be a plain column of the root
     * entity or of a joined table: association paths such as `asc(books.author)`, projected expressions from
     * `orderAscBy`/`orderDescBy`, and hand-written path providers are rejected with an
     * [UnsupportedOperationException] before any SQL runs.
     *
     * @param avoidEagerFetchFanout Pagination can silently return fewer than [Page.pageSize] entities when the
     * queried entity (or one it's joined to) has an eager `@OneToMany`/`@ManyToMany` association: fetching that
     * association can make a single entity take up more than its fair share of a page, crowding out entities
     * that should have made the cut.
     *
     * Setting this to `true` avoids that by deciding which entities belong on the page first, and only then
     * fetching those entities (along with their associations) - so a page's worth of associations can never
     * crowd out a page's worth of entities.
     *
     * The page-of-keys query groups by [uniqueColumn], so a collection the criteria join explicitly cannot crowd
     * entities off the page either. Ordering by that collection's own columns needs [forceAnsiCompliance] on
     * strict databases.
     *
     * Defaults to `false` to preserve existing behavior for callers whose entities have no eager collection
     * associations, since this costs an extra query. This may end up becoming the default (or the only) behavior
     * once it has seen enough real-world use to justify that cost unconditionally; the flag exists so that can
     * happen as a gradual, opt-in rollout rather than a behavior change forced on every caller at once.
     */
    fun <ID : Any> listPaginatedWithTotalResults(
        page: Page,
        orders: List<DEF.() -> YawnQueryOrder<T>>,
        uniqueColumn: DEF.() -> YawnTableDef<T, *>.ColumnDef<ID>,
        forceAnsiCompliance: Boolean = false,
        avoidEagerFetchFanout: Boolean = false,
    ): PaginationResult<T> {
        // Resolved up front so an unsupported order fails before any SQL runs.
        val groupedOrderColumns = if (forceAnsiCompliance) ansiCompliantOrderColumns(orders) else listOf()
        val totalResults = clone().countDistinct(uniqueColumn)
        val entities = if (forceAnsiCompliance || avoidEagerFetchFanout) {
            listPaginatedByIds(page, orders, uniqueColumn, groupedOrderColumns)
        } else {
            listPaginated(
                page = page,
                orders = orders,
            )
        }

        return page.toResults(totalResults, entities)
    }

    /**
     * See the `avoidEagerFetchFanout` doc on [listPaginatedWithTotalResults] for why this two-phase fetch exists.
     * Do not "simplify" this back into a single paginated entity query: that reintroduces silent truncation for
     * any entity with an eager `@OneToMany`/`@ManyToMany` association.
     *
     * The page-of-keys query always groups by the unique column, as the legacy helper did: without that, a
     * collection the criteria join explicitly fans the keys out to one row per joined row (Hibernate only skips
     * the eager fetch in a projection query), and a single entity can fill the whole page. Grouping by the unique
     * column alone leaves the `ORDER BY` columns ungrouped, which strict databases accept only for columns that
     * are functionally dependent on the key; see `forceAnsiCompliance` for the rest.
     *
     * @param groupedOrderColumns the order columns to group by on top of the unique column (see
     * `forceAnsiCompliance` on [listPaginatedWithTotalResults]); empty when the caller did not ask for that.
     */
    private fun <ID : Any> listPaginatedByIds(
        page: Page,
        orders: List<DEF.() -> YawnQueryOrder<T>>,
        uniqueColumn: DEF.() -> YawnTableDef<T, *>.ColumnDef<ID>,
        groupedOrderColumns: List<YawnDef<T, *>.YawnColumnDef<*>>,
    ): List<T> {
        val pagedIds = clone()
            .applyProjection { table -> project(groupedKeyProjector(table.uniqueColumn(), groupedOrderColumns)) }
            .paginate(page = page, orders = orders)
            .list()

        if (pagedIds.isEmpty()) {
            return listOf()
        }

        // No offset/maxResults matching the page size here: the IN clause below already restricts the query to
        // exactly this page's entities, so re-applying pagination on top of the (possibly fanned-out) join would
        // reintroduce the same truncation bug this method exists to avoid. maxResults is instead a generous safety
        // cap on raw SQL rows, since an eager collection join can still fan a handful of entities out to many rows.
        // The re-applied `orders` reproduce the correct relative order among this page's entities, and `distinct()`
        // collapses the duplicate root-entity references Hibernate returns for fanned-out join rows.
        return clone()
            .applyFilter { table -> addIn(table.uniqueColumn(), pagedIds) }
            .applyOrders(orders)
            .offset(0)
            .maxResults(EAGER_FETCH_FANOUT_REFETCH_MAX_RESULTS)
            .list()
            .distinct()
    }

    /**
     * The columns an ANSI-compliant page-of-keys query has to group by on top of the unique column: one per
     * [orders] entry. Only plain columns can be grouped, so anything else is rejected here, before any SQL runs.
     * The order lambdas are pure column references, so evaluating them again for the actual query is harmless.
     */
    private fun ansiCompliantOrderColumns(
        orders: List<DEF.() -> YawnQueryOrder<T>>,
    ): List<YawnDef<T, *>.YawnColumnDef<*>> {
        return orders.map { order ->
            val property = order(tableDef).property
            if (property !is YawnDef<*, *>.YawnColumnDef<*>) {
                throw UnsupportedOperationException(
                    "forceAnsiCompliance=true requires every order to be a plain column of the root entity or of a " +
                        "joined table; ordering by an association path, a projected expression or a custom path " +
                        "provider is not supported",
                )
            }
            // A YawnColumnDef is a YawnPathProvider of its own SOURCE, and this order is a YawnQueryOrder<T>, so
            // the column's SOURCE is T; the `is` check above just cannot recover the outer type argument.
            @Suppress("UNCHECKED_CAST")
            property as YawnDef<T, *>.YawnColumnDef<*>
        }
    }

    /**
     * Selects [uniqueColumn] as a `GROUP BY` column, so the page-of-keys query yields one row per entity however
     * the criteria's joins fan the rows out, together with [orderColumns] when the query also has to be a valid
     * grouped query under ANSI SQL; only the unique column's value survives into the result. Identical columns are
     * deduplicated by the projection resolver, so an order on the unique column itself is harmless.
     */
    private fun <ID : Any> groupedKeyProjector(
        uniqueColumn: YawnTableDef<T, *>.ColumnDef<ID>,
        orderColumns: List<YawnDef<T, *>.YawnColumnDef<*>>,
    ): YawnProjector<T, ID> {
        val groupedColumns = (listOf(uniqueColumn) + orderColumns).map { column ->
            YawnValueProjector<T, Any?> { ProjectionNode.aggregateAs(AggregateKind.GROUP_BY, column) }
        }
        return YawnProjector {
            ProjectionNode.composite(groupedColumns) { values ->
                @Suppress("UNCHECKED_CAST")
                values[0] as ID
            }
        }
    }

    fun rowCount(): Long {
        return applyProjection {
            project(YawnProjections.rowCount())
        }.maxResults(1).uniqueResult() ?: 0
    }

    fun exists(): Boolean {
        return applyProjection {
            project(YawnProjections.selectConstant("1"))
        }.maxResults(1).uniqueResult() != null
    }

    fun <FROM : Comparable<FROM>> maxValueOf(
        column: DEF.() -> YawnTableDef<T, *>.ColumnDef<FROM>,
    ): FROM? {
        return applyProjection { table ->
            project(YawnProjections.max(table.column()))
        }.uniqueResult()
    }

    fun <FROM : Comparable<FROM>> minValueOf(
        column: DEF.() -> YawnTableDef<T, *>.ColumnDef<FROM>,
    ): FROM? {
        return applyProjection { table ->
            project(YawnProjections.min(table.column()))
        }.uniqueResult()
    }

    fun orderAsc(
        column: DEF.() -> YawnTableDef<T, *>.ColumnDef<*>,
    ): EntityYawnQueryBuilder<T, DEF> {
        applyOrder { YawnQueryOrder.asc(tableDef.column()) }
        return this
    }

    fun orderDesc(
        column: DEF.() -> YawnTableDef<T, *>.ColumnDef<*>,
    ): EntityYawnQueryBuilder<T, DEF> {
        applyOrder { YawnQueryOrder.desc(tableDef.column()) }
        return this
    }

    fun applyOrder(
        order: DEF.() -> YawnQueryOrder<T>,
    ): EntityYawnQueryBuilder<T, DEF> {
        return applyOrders(listOf(order))
    }

    /**
     * Gets an existing join reference for the given column definition, or creates a new one if none exists.
     * This method prevents duplicate join errors by reusing existing joins when possible.
     */
    fun <F : Any, D : YawnTableDef<T, F>> getOrCreateJoinRef(
        joinType: JoinType = JoinType.INNER_JOIN,
        columnDef: DEF.() -> YawnTableDef<T, *>.JoinColumnDef<F, D>,
    ): YawnJoinRef<F, D> {
        val joinColumnDef = tableDef.columnDef()

        // Check if a join already exists for this column definition
        val existingJoin = query.joins.find { existingJoin ->
            existingJoin.columnDef === joinColumnDef
        }

        return if (existingJoin != null) {
            // Reuse existing join by creating a YawnJoinRef that references the existing join's parent
            YawnJoinRef(columnDef, existingJoin.parent)
        } else {
            // Create new join if none exists
            joinRef(joinType, columnDef)
        }
    }

    companion object {
        /**
         * A generous cap on raw SQL rows for the id-based re-fetch in [listPaginatedByIds].
         * This is not a pagination limit - the query is already restricted to a single page's worth of ids via an
         * IN clause - it only needs to be large enough that an eager collection join's fan-out never truncates it.
         */
        private const val EAGER_FETCH_FANOUT_REFETCH_MAX_RESULTS = 100_000

        /**
         * Create a [EntityYawnQueryBuilder] for a given [query], wiring in the generics from a provided [tableDef].
         * The lambda is optional if you want to immediately apply some filtering.
         */
        fun <T : Any, DEF : YawnTableDef<T, T>> create(
            tableDef: DEF,
            queryFactory: YawnQueryFactory,
            query: YawnQuery<T, T>,
            lambda: EntityYawnQueryScope<T, DEF>.(tableDef: DEF) -> Unit = {},
        ): EntityYawnQueryBuilder<T, DEF> {
            val criteria = EntityYawnQueryBuilder(tableDef, queryFactory, query)
            criteria.applyFilter(lambda)
            return criteria
        }
    }

    /**
     * Helper to create a join and return a wrapper containing both the criteria and the join reference.
     * This is useful when you want to create a join reference to be reused later.
     *
     * Example:
     * ```
     * val result = session.query(BookTable).attachJoinRef { author }
     * result.criteria.applyJoinRef(result.joinRef) { authors ->
     *     addLike(authors.name, "J.%")
     * }
     * ```
     *
     * Note that is equivalent to using the `joinRef` method directly and managing your own references.
     *
     * @param joinType the type of join to perform (defaults to INNER_JOIN)
     * @param columnDef a lambda that returns the join column definition
     * @return a [EntityCriteriaWithJoinRef] containing both the criteria and the join reference
     */
    fun <F : Any, D : YawnTableDef<T, F>> attachJoinRef(
        joinType: JoinType = JoinType.INNER_JOIN,
        columnDef: DEF.() -> YawnTableDef<T, *>.JoinColumnDef<F, D>,
    ): EntityCriteriaWithJoinRef<T, DEF, F, D> {
        val joinRef = getOrCreateJoinRef(joinType, columnDef)
        return EntityCriteriaWithJoinRef(this, joinRef)
    }
}
