package com.faire.yawn.criteria.builder

import com.faire.yawn.YawnTableDef
import com.faire.yawn.criteria.query.ProjectedTypeSafeCriteriaQuery
import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.query.YawnQuery
import com.faire.yawn.query.YawnQueryFactory

/**
 * A type-safe builder for Yawn queries with projections.
 *
 * Note that since we are projecting, the [RETURNS] type parameter will be different from [T].
 *
 * @param T the type of the entity being queried.
 * @param DEF the table definition of the entity being queried.
 * @param RETURNS the type being projected to.
 */
class ProjectedTypeSafeCriteriaBuilder<T : Any, DEF : YawnTableDef<T, T>, RETURNS : Any?>(
    tableDef: DEF,
    queryFactory: YawnQueryFactory,
    query: YawnQuery<T, T>,
) : BaseTypeSafeCriteriaBuilder<T, DEF, RETURNS, ProjectedTypeSafeCriteriaBuilder<T, DEF, RETURNS>>(
    tableDef,
    queryFactory,
    query,
) {
    override fun builderReturn(): ProjectedTypeSafeCriteriaBuilder<T, DEF, RETURNS> = this

    override fun clone(): ProjectedTypeSafeCriteriaBuilder<T, DEF, RETURNS> {
        return ProjectedTypeSafeCriteriaBuilder(tableDef, queryFactory, query.copy())
    }

    // to be used by `create` only
    internal fun applyFilter(
        lambda: ProjectedTypeSafeCriteriaQuery<T, T, DEF, RETURNS>.(tableDef: DEF) -> YawnQueryProjection<T, RETURNS>,
    ) {
        ProjectedTypeSafeCriteriaQuery.applyLambda<T, T, DEF, RETURNS>(query) {
            check(query.projection == null) { "At most one projection can be configured per query." }
            val projection = lambda(tableDef)
            query.projection = projection
            mapper = { projection.project(it) }
        }
    }

    /**
     * Apply additional filters (WHERE, JOIN, ORDER BY clauses) to this projected query.
     *
     * Note: You cannot call `project()` within this lambda as the projection has already been set.
     * This method is for adding conditions to filter the projected results.
     *
     * Example:
     * ```
     * session.query(BookTable)
     *     .applyProjection { books ->
     *         project(YawnProjections.pair(
     *             YawnProjections.rowCount(),
     *             YawnProjections.sum(books.pages)
     *         ))
     *     }
     *     .applyFilter { books ->
     *         val authors = join(books.author)
     *         addIn(authors.name, listOf("Author1", "Author2"))
     *     }
     *     .uniqueResult()
     * ```
     */
    fun applyFilter(
        lambda: ProjectedTypeSafeCriteriaQuery<T, T, DEF, RETURNS>.(tableDef: DEF) -> Unit,
    ): ProjectedTypeSafeCriteriaBuilder<T, DEF, RETURNS> {
        ProjectedTypeSafeCriteriaQuery.applyLambda<T, T, DEF, RETURNS>(query) {
            lambda(tableDef)
        }
        return this
    }

    companion object {
        fun <T : Any, DEF : YawnTableDef<T, T>, PROJECTION : Any?> create(
            tableDef: DEF,
            queryFactory: YawnQueryFactory,
            query: YawnQuery<T, T>,
            lambda:
            ProjectedTypeSafeCriteriaQuery<T, T, DEF, PROJECTION>.(tableDef: DEF) -> YawnQueryProjection<T, PROJECTION>,
        ): ProjectedTypeSafeCriteriaBuilder<T, DEF, PROJECTION> {
            val typeSafeCriteria = ProjectedTypeSafeCriteriaBuilder<T, DEF, PROJECTION>(tableDef, queryFactory, query)
            typeSafeCriteria.applyFilter(lambda)
            return typeSafeCriteria
        }
    }
}
