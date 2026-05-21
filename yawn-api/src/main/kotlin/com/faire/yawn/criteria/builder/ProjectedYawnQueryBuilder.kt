package com.faire.yawn.criteria.builder

import com.faire.yawn.YawnTableDef
import com.faire.yawn.criteria.query.ProjectedYawnQueryScope
import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.query.YawnQuery
import com.faire.yawn.query.YawnQueryFactory

/**
 * A builder for Yawn queries with projections.
 *
 * Note that since we are projecting, the [RETURNS] type parameter will be different from [T].
 * Not all methods from [EntityYawnQueryBuilder] are available here - some operations are not possible with projections.
 *
 * @param T the type of the entity being queried.
 * @param DEF the table definition of the entity being queried.
 * @param RETURNS the type being projected to.
 */
class ProjectedYawnQueryBuilder<T : Any, DEF : YawnTableDef<T, T>, RETURNS : Any?>(
    tableDef: DEF,
    queryFactory: YawnQueryFactory,
    query: YawnQuery<T, T>,
) : YawnQueryBuilder<T, DEF, RETURNS, ProjectedYawnQueryBuilder<T, DEF, RETURNS>>(
    tableDef,
    queryFactory,
    query,
),
    YawnBuilderWithDistinct<ProjectedYawnQueryBuilder<T, DEF, RETURNS>> {
    override fun builderReturn(): ProjectedYawnQueryBuilder<T, DEF, RETURNS> = this

    override fun distinct(distinct: Boolean): ProjectedYawnQueryBuilder<T, DEF, RETURNS> {
        query.distinct = distinct
        return builderReturn()
    }

    override fun clone(): ProjectedYawnQueryBuilder<T, DEF, RETURNS> {
        return ProjectedYawnQueryBuilder(tableDef, queryFactory, query.copy())
    }

    // to be used by `create` only
    internal fun applyFilter(
        lambda: ProjectedYawnQueryScope<T, T, DEF, RETURNS>.(tableDef: DEF) -> YawnQueryProjection<T, RETURNS>,
    ) {
        ProjectedYawnQueryScope.applyLambda<T, T, DEF, RETURNS>(query) {
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
            ProjectedYawnQueryScope<T, T, DEF, PROJECTION>.(tableDef: DEF) -> YawnQueryProjection<T, PROJECTION>,
        ): ProjectedYawnQueryBuilder<T, DEF, PROJECTION> {
            val criteria = ProjectedYawnQueryBuilder<T, DEF, PROJECTION>(tableDef, queryFactory, query)
            criteria.applyFilter(lambda)
            return criteria
        }
    }
}
