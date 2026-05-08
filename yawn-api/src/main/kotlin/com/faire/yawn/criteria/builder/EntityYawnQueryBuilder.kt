package com.faire.yawn.criteria.builder

import com.faire.yawn.YawnTableDef
import com.faire.yawn.YawnTableDefParent.AssociationTableDefParent
import com.faire.yawn.criteria.join.EntityCriteriaWithJoinRef
import com.faire.yawn.criteria.query.EntityYawnQueryScope
import com.faire.yawn.criteria.query.ProjectedYawnQueryScope
import com.faire.yawn.criteria.query.YawnQueryScopeWithJoinDelegate
import com.faire.yawn.pagination.Page
import com.faire.yawn.pagination.PaginationResult
import com.faire.yawn.project.YawnProjections
import com.faire.yawn.project.YawnQueryProjection
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
        return EntityYawnQueryBuilder(tableDef, queryFactory, query.copy())
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

    fun listPaginatedWithTotalResults(
        page: Page,
        orders: List<DEF.() -> YawnQueryOrder<T>>,
        uniqueColumn: DEF.() -> YawnTableDef<T, *>.ColumnDef<*>,
        forceAnsiCompliance: Boolean = false,
    ): PaginationResult<T> {
        if (forceAnsiCompliance) {
            throw UnsupportedOperationException("forceAnsiCompliance=true is not supported yet in Yawn")
        }
        val totalResults = clone().countDistinct(uniqueColumn)
        val entities = listPaginated(
            page = page,
            orders = orders,
        )

        return page.toResults(totalResults, entities)
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
