package com.faire.yawn.criteria.query

import com.faire.yawn.YawnDef
import com.faire.yawn.YawnTableDef
import com.faire.yawn.criteria.builder.DetachedProjectedYawnBuilder
import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.query.YawnQuery
import com.faire.yawn.query.YawnQueryWithCriterion

/**
 * An abstract super-class for all Yawn query context DSL classes; not be used directly.
 *
 * These classes provide the context used inside the Yawn criteria builder lambdas, providing methods such as
 * `addEq` and `join` depending on the type of query being built.
 *
 * The full inheritance chain is as follows:
 * * [BaseYawnQueryScope], everyone inherits, includes the general filter methods (`addEq`, etc.)
 * * * [EntityYawnQueryScope], the most common case, adds the join methods
 * * * [ProjectedYawnQueryScope], for defining projections, adds the project methods
 * * * [ProjectionYawnQueryScope], for further refine define projections, no additions
 *
 * @param SOURCE the type of the original table that the criteria is based off of.
 * @param T the type being queried (either a projection or an entity).
 * @param DEF the table or projection definition of the entity being queried.
 */
abstract class BaseYawnQueryScope<SOURCE : Any, T : Any, DEF : YawnDef<SOURCE, T>> protected constructor(
    protected val query: YawnQueryWithCriterion<SOURCE, T>,
) {
    /**
     * Creates a correlatable projected subquery that can be used on the parent query in criteria.
     *
     * @param ST the type of the entity being sub-queried.
     * @param DEF the table definition of the entity being sub-queried.
     * @param PROJECTION the type of the projection being returned by the subquery.
     *
     * @param tableDef the table definition of the entity being sub-queried.
     * @param lambda to be used to add criteria to the subquery.
     */
    inline fun <reified ST : Any, DEF : YawnTableDef<SOURCE, ST>, PROJECTION : Any?> createProjectedSubQuery(
        tableDef: DEF,
        noinline lambda: ProjectedYawnQueryScope<SOURCE, ST, DEF, PROJECTION>.(
            tableDef: DEF,
        ) -> YawnQueryProjection<SOURCE, PROJECTION>,
    ): DetachedProjectedYawnBuilder<SOURCE, ST, DEF, PROJECTION> {
        val query = YawnQuery<SOURCE, ST>(ST::class.java)
        return DetachedProjectedYawnBuilder.create(tableDef, query, lambda)
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
