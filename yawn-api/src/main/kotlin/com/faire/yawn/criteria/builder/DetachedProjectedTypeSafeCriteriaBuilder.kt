package com.faire.yawn.criteria.builder

import com.faire.yawn.YawnTableDef
import com.faire.yawn.criteria.builder.DetachedProjectedTypeSafeCriteriaBuilder.Companion.create
import com.faire.yawn.criteria.query.ProjectedTypeSafeCriteriaQuery
import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.query.YawnCompilationContext
import com.faire.yawn.query.YawnQuery
import com.faire.yawn.query.YawnQueryRestriction.And
import org.hibernate.criterion.DetachedCriteria

/**
 * A type-safe builder for detached Yawn queries with projections.
 *
 * Note that since we are projection, the [RETURNS] type parameter will be different from [T]
 *
 * Also note that we do not support `applyFilter` here, because:
 * * you can only project once per query
 * * the projection is the return type of the Query DSL lambda
 * For those reasons, with projections you can only have one lambda, which is provided in the [create] method.
 *
 * @param T the type of the entity being queried
 * @param DEF the table definition of the entity being queried
 * @param RETURNS the type being projected to
 */
class DetachedProjectedTypeSafeCriteriaBuilder<SOURCE : Any, T : Any, DEF : YawnTableDef<SOURCE, T>, RETURNS : Any?>(
    val query: YawnQuery<SOURCE, T>,
    private val tableDef: DEF,
) {

    fun compile(context: YawnCompilationContext): DetachedCriteria {
        return buildDetachedCriteria(context)
    }

    // to be used by `create` only
    internal fun applyFilter(
        lambda:
        ProjectedTypeSafeCriteriaQuery<SOURCE, T, DEF, RETURNS>.(tableDef: DEF) -> YawnQueryProjection<SOURCE, RETURNS>,
    ) {
        ProjectedTypeSafeCriteriaQuery.applyLambda<SOURCE, T, DEF, RETURNS>(query) {
            check(query.projection == null) { "At most one projection can be configured per query." }
            val projection = lambda(tableDef)
            query.projection = projection
        }
    }

    // TODO (yawn): Factor out the factory
    private fun buildDetachedCriteria(context: YawnCompilationContext): DetachedCriteria {
        val alias = checkNotNull(context.generateAlias(tableDef.parent)) { "Unable to alias subquery" }
        val detachedCriteria = DetachedCriteria.forClass(query.clazz, alias)

        for (join in query.joins) {
            val alias = checkNotNull(context.generateAlias(join.parent)) {
                "Unable to alias join in subquery"
            }

            if (join.joinCriteria.isNotEmpty()) {
                val criterion = And(join.joinCriteria)
                detachedCriteria.createAlias(
                    join.path(context),
                    alias,
                    join.joinType,
                    criterion.compile(context),
                )
            } else {
                detachedCriteria.createAlias(join.path(context), alias, join.joinType)
            }
        }

        for (criterion in query.criteria.map { it.yawnRestriction.compile(context) }) {
            detachedCriteria.add(criterion)
        }

        for (order in query.orders) {
            detachedCriteria.addOrder(order.compile(context))
        }

        val hibernateProjection = query.projection?.compile(context)
        if (hibernateProjection != null) {
            detachedCriteria.setProjection(hibernateProjection)
        }

        return detachedCriteria
    }

    companion object {
        fun <SOURCE : Any, T : Any, DEF : YawnTableDef<SOURCE, T>, PROJECTION : Any?> create(
            tableDef: DEF,
            query: YawnQuery<SOURCE, T>,
            lambda: ProjectedTypeSafeCriteriaQuery<SOURCE, T, DEF, PROJECTION>.(
                tableDef: DEF,
            ) -> YawnQueryProjection<SOURCE, PROJECTION>,
        ): DetachedProjectedTypeSafeCriteriaBuilder<SOURCE, T, DEF, PROJECTION> {
            val typeSafeCriteria = DetachedProjectedTypeSafeCriteriaBuilder<SOURCE, T, DEF, PROJECTION>(query, tableDef)
            typeSafeCriteria.applyFilter(lambda)
            return typeSafeCriteria
        }
    }
}
