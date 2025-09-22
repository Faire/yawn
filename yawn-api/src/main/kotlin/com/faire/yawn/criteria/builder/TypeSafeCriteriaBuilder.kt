package com.faire.yawn.criteria.builder

import com.faire.yawn.YawnTableDef
import com.faire.yawn.YawnTableDefParent.AssociationTableDefParent
import com.faire.yawn.criteria.query.ProjectedTypeSafeCriteriaQuery
import com.faire.yawn.criteria.query.TypeSafeCriteriaQuery
import com.faire.yawn.criteria.query.TypeSafeCriteriaWithJoinDelegate
import com.faire.yawn.project.YawnProjections
import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.query.YawnQuery
import com.faire.yawn.query.YawnQueryFactory
import com.faire.yawn.query.YawnQueryOrder
import org.hibernate.sql.JoinType

/**
 * A type-safe builder for Yawn queries without projections.
 *
 * Use the method [applyFilter] to further refine the query with type-safe methods like `addEq`, etc.
 *
 * @param T the type of the entity being queried.
 * @param DEF the table definition of the entity being queried.
 */
class TypeSafeCriteriaBuilder<T : Any, DEF : YawnTableDef<T, T>>(
    tableDef: DEF,
    queryFactory: YawnQueryFactory,
    query: YawnQuery<T, T>,
) : BaseTypeSafeCriteriaBuilder<T, DEF, T, TypeSafeCriteriaBuilder<T, DEF>>(tableDef, queryFactory, query) {
  override fun builderReturn(): TypeSafeCriteriaBuilder<T, DEF> = this
  override fun clone(): TypeSafeCriteriaBuilder<T, DEF> {
    return TypeSafeCriteriaBuilder(tableDef, queryFactory, query.copy())
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
    val joinParent = TypeSafeCriteriaWithJoinDelegate(query).registerJoin(joinColumnDef, joinType = joinType)
    return YawnJoinRef(columnDef, joinParent)
  }

  fun applyFilter(
      lambda: TypeSafeCriteriaQuery<T, DEF>.(tableDef: DEF) -> Unit,
  ): TypeSafeCriteriaBuilder<T, DEF> {
    TypeSafeCriteriaQuery.applyLambda<T, DEF>(query) { lambda(tableDef) }
    return this
  }

  fun <RETURNS : Any?> applyProjection(
      lambda: ProjectedTypeSafeCriteriaQuery<T, T, DEF, RETURNS>.(tableDef: DEF) -> YawnQueryProjection<T, RETURNS>,
  ): ProjectedTypeSafeCriteriaBuilder<T, DEF, RETURNS> {
    return ProjectedTypeSafeCriteriaBuilder.create(tableDef, queryFactory, query, lambda)
  }

  fun listBatched(
      batchSize: Int,
      orders: List<DEF.() -> YawnQueryOrder<T>>,
  ): List<T> {
    val results = mutableListOf<T>()
    doPaginated(
        pageSize = batchSize,
        orders = orders,
        action = { results.addAll(it) },
    )
    return results
  }

  fun listPaginatedZeroIndexed(
      pageNumber: Int,
      pageSize: Int,
      orders: List<DEF.() -> YawnQueryOrder<T>>,
  ): List<T> {
    check(pageSize >= 1) { "$pageSize is not a valid page size" }
    check(pageNumber >= 0) { "$pageNumber is not a valid page number" }

    return applyOrders(orders)
        .offset(pageNumber * pageSize)
        .maxResults(pageSize)
        .list()
  }

  fun countDistinct(
      uniqueColumn: DEF.() -> YawnTableDef<T, *>.ColumnDef<*>,
  ): Long {
    return applyProjection { table ->
      project(YawnProjections.countDistinct(table.uniqueColumn()))
    }.uniqueResult() ?: 0
  }

  fun listPaginatedWithTotalResultsZeroIndexed(
      pageNumber: Int,
      pageSize: Int,
      orders: List<DEF.() -> YawnQueryOrder<T>>,
      uniqueColumn: DEF.() -> YawnTableDef<T, *>.ColumnDef<*>,
      forceAnsiCompliance: Boolean = false,
  ): Pair<Long, List<T>> {
    if (forceAnsiCompliance) {
      throw UnsupportedOperationException("forceAnsiCompliance=true is not supported yet in Yawn")
    }
    val totalResults = clone().countDistinct(uniqueColumn)
    val entities = listPaginatedZeroIndexed(
        pageNumber = pageNumber,
        pageSize = pageSize,
        orders = orders,
    )

    return Pair(totalResults, entities)
  }

  inline fun doPaginated(
      pageSize: Int,
      orders: List<DEF.() -> YawnQueryOrder<T>>,
      action: (List<T>) -> Unit,
  ) {
    // only apply the orders once
    applyOrders(orders)

    var pageNumber = 0
    do {
      val results = listPaginatedZeroIndexed(pageNumber++, pageSize, listOf())
      action(results)
    } while (results.size == pageSize)
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
  ): TypeSafeCriteriaBuilder<T, DEF> {
    applyOrder { YawnQueryOrder.asc(tableDef.column()) }
    return this
  }

  fun orderDesc(
      column: DEF.() -> YawnTableDef<T, *>.ColumnDef<*>,
  ): TypeSafeCriteriaBuilder<T, DEF> {
    applyOrder { YawnQueryOrder.desc(tableDef.column()) }
    return this
  }

  fun applyOrder(
      order: DEF.() -> YawnQueryOrder<T>,
  ): TypeSafeCriteriaBuilder<T, DEF> {
    return applyOrders(listOf(order))
  }

  companion object {
    /**
     * Create a TypeSafeCriteria from a raw Criteria, wiring in the generics from a provided [tableDef].
     * The lambda is optional if you want to immediately apply some filtering.
     */
    fun <T : Any, DEF : YawnTableDef<T, T>> create(
        tableDef: DEF,
        queryFactory: YawnQueryFactory,
        query: YawnQuery<T, T>,
        lambda: TypeSafeCriteriaQuery<T, DEF>.(tableDef: DEF) -> Unit = {},
    ): TypeSafeCriteriaBuilder<T, DEF> {
      val typeSafeCriteria = TypeSafeCriteriaBuilder(tableDef, queryFactory, query)
      typeSafeCriteria.applyFilter(lambda)
      return typeSafeCriteria
    }
  }
}
