package com.faire.yawn.query

import com.faire.yawn.YawnTableDef
import com.faire.yawn.project.YawnPathProvider
import org.hibernate.NullPrecedence
import org.hibernate.NullPrecedence.NONE
import org.hibernate.criterion.Order

/**
 * Part of an [YawnQuery] representing each single ORDER BY clause.
 *
 * Compiles into a Hibernate's [Order].
 * It restricts construction of this class by requiring the [SOURCE] of the query.
 *
 * @property property the property by which to order. Usually a [YawnTableDef.ColumnDef]; to order by a projected/
 * aggregate expression instead (e.g. ordering grouped rows by `max(createdAt)`), use the
 * [com.faire.yawn.criteria.query.orderAsc]/[com.faire.yawn.criteria.query.orderDesc] extension functions rather
 * than constructing a [YawnQueryOrder] directly.
 * @property direction the direction by which to order, either ascending or descending
 * @property nullPrecedence the precedence of null values, either first, last, or none
 */
data class YawnQueryOrder<SOURCE : Any>(
    val property: YawnPathProvider<SOURCE>,
    val direction: Direction,
    val nullPrecedence: NullPrecedence,
) {
    /**
     * Sort direction, either ascending or descending.
     */
    enum class Direction {
        ASC,
        DESC,
    }

    fun compile(context: YawnCompilationContext): Order {
        val path = property.generatePath(context)
        return when (direction) {
            Direction.ASC -> Order.asc(path).nulls(nullPrecedence)
            Direction.DESC -> Order.desc(path).nulls(nullPrecedence)
        }
    }

    companion object {
        fun <SOURCE : Any> asc(
            property: YawnPathProvider<SOURCE>,
            nullPrecedence: NullPrecedence = NONE,
        ): YawnQueryOrder<SOURCE> {
            return YawnQueryOrder(property, Direction.ASC, nullPrecedence)
        }

        fun <SOURCE : Any> desc(
            property: YawnPathProvider<SOURCE>,
            nullPrecedence: NullPrecedence = NONE,
        ): YawnQueryOrder<SOURCE> {
            return YawnQueryOrder(property, Direction.DESC, nullPrecedence)
        }
    }
}
