package com.faire.yawn

import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.query.YawnCompilationContext
import org.hibernate.criterion.Projection
import org.hibernate.criterion.Projections

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
   * @param F the type of the column.
   */
  abstract inner class YawnColumnDef<F> : YawnQueryProjection<SOURCE, F> {
    abstract fun generatePath(context: YawnCompilationContext): String

    override fun compile(context: YawnCompilationContext): Projection {
      return Projections.property(generatePath(context))
    }

    override fun project(value: Any?): F {
      @Suppress("UNCHECKED_CAST")
      return value as F
    }
  }
}
