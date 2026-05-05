package com.faire.yawn.query

import com.faire.yawn.YawnTableDef
import org.hibernate.NullPrecedence
import org.hibernate.NullPrecedence.NONE
import org.hibernate.criterion.Order

/**
 * Part of an [YawnQuery] representing each single ORDER BY clause.
 *
 * Compiles into a Hibernate's [Order].
 * It restricts construction of this class by requiring the [SOURCE] of the query.
 *
 * @property property the property by which to order
 * @property direction the direction by which to order, either ascending or descending
 * @property nullPrecedence the precedence of null values, either first, last, or none
 */
data class YawnQueryOrder<SOURCE : Any>(
    val property: YawnTableDef<SOURCE, *>.ColumnDef<*>,
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
            property: YawnTableDef<SOURCE, *>.ColumnDef<*>,
            nullPrecedence: NullPrecedence = NONE,
        ): YawnQueryOrder<SOURCE> {
            return YawnQueryOrder(property, Direction.ASC, nullPrecedence)
        }

        fun <SOURCE : Any> desc(
            property: YawnTableDef<SOURCE, *>.ColumnDef<*>,
            nullPrecedence: NullPrecedence = NONE,
        ): YawnQueryOrder<SOURCE> {
            return YawnQueryOrder(property, Direction.DESC, nullPrecedence)
        }
    }
}
