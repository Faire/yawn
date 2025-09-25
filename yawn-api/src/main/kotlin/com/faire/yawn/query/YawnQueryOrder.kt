package com.faire.yawn.query

import com.faire.yawn.YawnTableDef
import org.hibernate.NullPrecedence
import org.hibernate.NullPrecedence.NONE
import org.hibernate.criterion.Order

/**
 * A type-safe wrapper around [Order].
 *
 * It is type safe by restricting construction of this class by requiring the [SOURCE] of the query.
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
