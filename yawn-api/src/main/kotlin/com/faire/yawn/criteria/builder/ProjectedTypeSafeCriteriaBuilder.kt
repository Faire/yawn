package com.faire.yawn.criteria.builder

import com.faire.yawn.YawnTableDef
import com.faire.yawn.criteria.builder.ProjectedTypeSafeCriteriaBuilder.Companion.create
import com.faire.yawn.criteria.query.ProjectedTypeSafeCriteriaQuery
import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.query.YawnQuery
import com.faire.yawn.query.YawnQueryFactory

/**
 * A type-safe builder for Yawn queries with projections.
 *
 * Note that since we are projecting, the [RETURNS] type parameter will be different from [T].
 *
 * Also note that we do not support `applyFilter` here, because:
 * * you can only project once per query
 * * the projection is the return type of the Query DSL lambda
 * For those reasons, with projections you can only have one lambda, which is provided in the [create] method.
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
