package com.faire.yawn.criteria.query

import com.faire.yawn.YawnDef
import com.faire.yawn.YawnTableDef
import com.faire.yawn.criteria.builder.DetachedProjectedTypeSafeCriteriaBuilder
import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.query.YawnCriteriaQuery
import com.faire.yawn.query.YawnQuery

/**
 * An abstract super-class for all type-safe Yawn queries DSL; not be used directly.
 *
 * This breaks down into the following:
 * * [BaseTypeSafeCriteriaQuery], everyone inherits, includes the general filter methods (addEq, etc)
 * * [TypeSafeCriteriaQuery], the most common case, adds the join methods
 * * [ProjectedTypeSafeCriteriaQuery], for defining projections, adds the project methods
 * * [ProjectionTypeSafeCriteriaQuery], for further refine define projections, no additions
 *
 * @param SOURCE the type of the original table that the criteria is based off of.
 * @param T the type being queried (either a projection or an entity).
 * @param DEF the table or projection definition of the entity being queried.
 */
abstract class BaseTypeSafeCriteriaQuery<SOURCE : Any, T : Any, DEF : YawnDef<SOURCE, T>> protected constructor(
    protected val query: YawnCriteriaQuery<SOURCE, T>,
) {
    /**
     * Creates a correlatable projected subquery that can be used on the parent query in criteria.
     *
     * @param ST the type of the entity being subqueried.
     * @param DEF the table definition of the entity being subqueried.
     * @param PROJECTION the type of the projection being returned by the subquery.
     *
     * @param tableDef the table definition of the entity being subqueried.
     * @param lambda to be used to add criteria to the subquery.
     */
    inline fun <reified ST : Any, DEF : YawnTableDef<SOURCE, ST>, PROJECTION : Any?> createProjectedSubQuery(
        tableDef: DEF,
        noinline lambda:
        ProjectedTypeSafeCriteriaQuery<SOURCE, ST, DEF, PROJECTION>.(
            tableDef: DEF,
        ) -> YawnQueryProjection<SOURCE, PROJECTION>,
    ): DetachedProjectedTypeSafeCriteriaBuilder<SOURCE, ST, DEF, PROJECTION> {
        val query = YawnQuery<SOURCE, ST>(ST::class.java)
        return DetachedProjectedTypeSafeCriteriaBuilder.create(tableDef, query, lambda)
    }

    fun <F : Any> nullable(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
    ): YawnDef<SOURCE, *>.YawnColumnDef<F?> {
        // a Long _is_ a Long?
        // a Long? _is not_ a Long
        @Suppress("UNCHECKED_CAST")
        return column as YawnDef<SOURCE, *>.YawnColumnDef<F?>
    }
}
