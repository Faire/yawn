package com.faire.yawn.project

import com.faire.yawn.YawnDef
import com.faire.yawn.query.YawnCompilationContext

/**
 * This is a base class used by Yawn to define all the generated KSP code for custom mapped projections.
 * Tag your projections with [YawnProjection] in order to have the definitions generated for you.
 * It is the equivalent of [com.faire.yawn.YawnTableDef], but for projections.
 *
 * @param D the type of the projection.
 * @param SOURCE the type of the original table that the criteria is based off of.
 */
abstract class YawnProjectionDef<SOURCE : Any, D : Any> : YawnDef<SOURCE, D>() {
    /**
     * A column definition for a projection.
     * It is the equivalent of [com.faire.yawn.YawnTableDef.ColumnDef], but for projections.
     *
     * @param T the type of the column.
     */
    inner class ProjectionColumnDef<T>(private val name: String) : YawnColumnDef<T>() {
        override fun generatePath(context: YawnCompilationContext): String = name
    }
}
