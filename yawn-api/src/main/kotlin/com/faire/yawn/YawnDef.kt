package com.faire.yawn

import com.faire.yawn.project.ProjectionNode
import com.faire.yawn.project.YawnValueProjector
import com.faire.yawn.query.YawnCompilationContext

/**
 * A Yawn definition that can be queried, i.e. either a [YawnTableDef] or a [com.faire.yawn.project.YawnProjectionDef].
 * It will contain a collection of columns that allows for Yawn queries to be type safe.
 *
 * @param SOURCE the type of the original table that the criteria is based off of.
 * @param D the type of the entity or projection.
 */
abstract class YawnDef<SOURCE : Any, D : Any> {
    /**
     * Base class for all Yawn Column-like definitions.
     * This can be either a column from a table or a projection.
     *
     * Implements [YawnValueProjector] so that columns can be used directly as projections
     * in both the v2 [ProjectionNode] tree and as arguments to [com.faire.yawn.project.YawnProjections] methods.
     *
     * @param F the type of the column.
     */
    abstract inner class YawnColumnDef<F> : YawnValueProjector<SOURCE, F> {
        abstract fun generatePath(context: YawnCompilationContext): String

        open fun adaptValue(value: F): Any? {
            return value
        }

        override fun projection(): ProjectionNode.Value<SOURCE, F> = ProjectionNode.property(this)
    }
}
