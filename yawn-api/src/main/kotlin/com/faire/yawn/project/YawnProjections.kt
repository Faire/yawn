package com.faire.yawn.project

import com.faire.yawn.YawnDef
import com.faire.yawn.project.YawnProjections.mapping
import com.faire.yawn.query.YawnCompilationContext
import org.hibernate.criterion.Projection
import org.hibernate.criterion.Projections
import org.hibernate.type.StandardBasicTypes

/**
 * Yawn equivalent of Hibernate [Projections].
 * A utility object to create type-safe [YawnQueryProjection].
 */
object YawnProjections {
    internal class Distinct<SOURCE : Any, TO>(
        private val projection: YawnQueryProjection<SOURCE, TO>,
    ) : YawnQueryProjection<SOURCE, TO> {
        override fun compile(
            context: YawnCompilationContext,
        ): Projection = Projections.distinct(projection.compile(context))

        override fun project(value: Any?): TO = projection.project(value)
    }

    fun <SOURCE : Any, TO> distinct(
        projection: YawnQueryProjection<SOURCE, TO>,
    ): YawnQueryProjection<SOURCE, TO> {
        return Distinct(projection)
    }

    internal class Count<SOURCE : Any, FROM : Any?>(
        private val columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ) : YawnQueryProjection<SOURCE, Long> {
        override fun compile(
            context: YawnCompilationContext,
        ): Projection = Projections.count(columnDef.generatePath(context))

        override fun project(value: Any?): Long = value as Long
    }

    fun <SOURCE : Any, FROM : Any?> count(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnQueryProjection<SOURCE, Long> {
        return Count(columnDef)
    }

    internal class CountDistinct<SOURCE : Any, FROM : Any?>(
        private val columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ) : YawnQueryProjection<SOURCE, Long> {
        override fun compile(
            context: YawnCompilationContext,
        ): Projection = Projections.countDistinct(columnDef.generatePath(context))

        override fun project(value: Any?): Long = value as Long
    }

    fun <SOURCE : Any, FROM : Any?> countDistinct(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnQueryProjection<SOURCE, Long> {
        return CountDistinct(columnDef)
    }

    internal class SumNullable<SOURCE : Any, FROM : Number?>(
        private val columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ) : YawnQueryProjection<SOURCE, Long?> {
        override fun compile(
            context: YawnCompilationContext,
        ): Projection = Projections.sum(columnDef.generatePath(context))

        override fun project(value: Any?): Long? = value as Long?
    }

    @JvmName("sumNullable")
    fun <SOURCE : Any, FROM : Number?> sum(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnQueryProjection<SOURCE, Long?> {
        return SumNullable(columnDef)
    }

    internal class Sum<SOURCE : Any, FROM : Number>(
        private val columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ) : YawnQueryProjection<SOURCE, Long> {
        override fun compile(
            context: YawnCompilationContext,
        ): Projection = Projections.sum(columnDef.generatePath(context))

        override fun project(value: Any?): Long = value as Long
    }

    fun <SOURCE : Any, FROM : Number> sum(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnQueryProjection<SOURCE, Long> {
        return Sum(columnDef)
    }

    internal class AvgNullable<SOURCE : Any, FROM : Any?>(
        private val columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ) : YawnQueryProjection<SOURCE, Double?> {
        override fun compile(
            context: YawnCompilationContext,
        ): Projection = Projections.avg(columnDef.generatePath(context))

        override fun project(value: Any?): Double? = value as Double?
    }

    @JvmName("avgNullable")
    fun <SOURCE : Any, FROM : Number?> avg(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnQueryProjection<SOURCE, Double?> {
        return AvgNullable(columnDef)
    }

    internal class Avg<SOURCE : Any, FROM : Number>(
        private val columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ) : YawnQueryProjection<SOURCE, Double> {
        override fun compile(
            context: YawnCompilationContext,
        ): Projection = Projections.avg(columnDef.generatePath(context))

        override fun project(value: Any?): Double = value as Double
    }

    fun <SOURCE : Any, FROM : Number> avg(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnQueryProjection<SOURCE, Double> {
        return Avg(columnDef)
    }

    internal class Max<SOURCE : Any, FROM : Comparable<FROM>?>(
        private val columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ) : YawnQueryProjection<SOURCE, FROM> {
        override fun compile(
            context: YawnCompilationContext,
        ): Projection = Projections.max(columnDef.generatePath(context))

        @Suppress("UNCHECKED_CAST")
        override fun project(value: Any?): FROM = value as FROM
    }

    fun <SOURCE : Any, FROM : Comparable<FROM>?> max(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnQueryProjection<SOURCE, FROM> {
        return Max(columnDef)
    }

    internal class Min<SOURCE : Any, FROM : Comparable<FROM>?>(
        private val columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ) : YawnQueryProjection<SOURCE, FROM> {
        override fun compile(
            context: YawnCompilationContext,
        ): Projection = Projections.min(columnDef.generatePath(context))

        @Suppress("UNCHECKED_CAST")
        override fun project(value: Any?): FROM = value as FROM
    }

    fun <SOURCE : Any, FROM : Comparable<FROM>?> min(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnQueryProjection<SOURCE, FROM> {
        return Min(columnDef)
    }

    internal class GroupBy<SOURCE : Any, FROM : Any?>(
        private val columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ) : YawnQueryProjection<SOURCE, FROM> {
        override fun compile(
            context: YawnCompilationContext,
        ): Projection = Projections.groupProperty(columnDef.generatePath(context))

        @Suppress("UNCHECKED_CAST")
        override fun project(value: Any?): FROM = value as FROM
    }

    fun <SOURCE : Any, FROM : Any?> groupBy(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnQueryProjection<SOURCE, FROM> {
        return GroupBy(columnDef)
    }

    internal class RowCount<SOURCE : Any> : YawnQueryProjection<SOURCE, Long> {
        override fun compile(context: YawnCompilationContext): Projection = Projections.rowCount()

        override fun project(value: Any?): Long = value as Long
    }

    fun <SOURCE : Any> rowCount(): YawnQueryProjection<SOURCE, Long> {
        return RowCount()
    }

    internal class SelectConstant<SOURCE : Any>(
        private val constant: String,
    ) : YawnQueryProjection<SOURCE, String> {
        override fun compile(context: YawnCompilationContext): Projection {
            return Projections.sqlProjection(
                "'$constant' as $CONSTANT_ALIAS",
                arrayOf(CONSTANT_ALIAS),
                arrayOf(StandardBasicTypes.STRING),
            )
        }

        override fun project(value: Any?): String = value as String
    }

    fun <SOURCE : Any> selectConstant(constant: String): YawnQueryProjection<SOURCE, String> {
        return SelectConstant(constant)
    }

    internal class Coalesce<SOURCE : Any, FROM : Any?>(
        private val projection: YawnQueryProjection<SOURCE, FROM?>,
        private val defaultValue: FROM,
    ) : YawnQueryProjection<SOURCE, FROM> {
        override fun compile(
            context: YawnCompilationContext,
        ): Projection = projection.compile(context)

        @Suppress("UNCHECKED_CAST")
        override fun project(value: Any?): FROM = value as FROM? ?: defaultValue
    }

    fun <SOURCE : Any, FROM : Any> coalesce(
        projection: YawnQueryProjection<SOURCE, FROM?>,
        defaultValue: FROM,
    ): YawnQueryProjection<SOURCE, FROM> {
        return Coalesce(projection, defaultValue)
    }

    internal class Null<SOURCE : Any, FROM : Any> : YawnQueryProjection<SOURCE, FROM?> {
        override fun compile(context: YawnCompilationContext): Projection {
            return Projections.sqlProjection(
                "null as $CONSTANT_ALIAS",
                arrayOf(CONSTANT_ALIAS),
                arrayOf(StandardBasicTypes.STRING),
            )
        }

        override fun project(value: Any?): FROM? = null
    }

    fun <SOURCE : Any, T : Any> `null`(): YawnQueryProjection<SOURCE, T?> {
        return Null()
    }

    internal class PairProjection<SOURCE : Any, A : Any?, B : Any?>(
        firstProjection: YawnQueryProjection<SOURCE, A>,
        secondProjection: YawnQueryProjection<SOURCE, B>,
    ) : Mapping2Projection<SOURCE, A, B, Pair<A, B>>(firstProjection, secondProjection, { a, b -> Pair(a, b) })

    fun <SOURCE : Any, A : Any?, B : Any?> pair(
        firstProjection: YawnQueryProjection<SOURCE, A>,
        secondProjection: YawnQueryProjection<SOURCE, B>,
    ): YawnQueryProjection<SOURCE, Pair<A, B>> {
        return PairProjection(firstProjection, secondProjection)
    }

    internal class TripleProjection<SOURCE : Any, A : Any?, B : Any?, C : Any?>(
        firstProjection: YawnQueryProjection<SOURCE, A>,
        secondProjection: YawnQueryProjection<SOURCE, B>,
        thirdProjection: YawnQueryProjection<SOURCE, C>,
    ) : Mapping3Projection<SOURCE, A, B, C, Triple<A, B, C>>(
        firstProjection,
        secondProjection,
        thirdProjection,
        { a, b, c -> Triple(a, b, c) },
    )

    fun <SOURCE : Any, A : Any?, B : Any?, C : Any?> triple(
        firstProjection: YawnQueryProjection<SOURCE, A>,
        secondProjection: YawnQueryProjection<SOURCE, B>,
        thirdProjection: YawnQueryProjection<SOURCE, C>,
    ): YawnQueryProjection<SOURCE, Triple<A, B, C>> {
        return TripleProjection(firstProjection, secondProjection, thirdProjection)
    }

    /**
     * Provides an in-memory transformation over a column value to a different type.
     * Use this when using more complex data classes as projections to apply minor
     * type or value compliance transformations to database column values
     * while keeping your projection classes type-safe, without needing to use
     * intermediary representations.
     * NOTE: this _does not_ change the query and is post-processed in memory.
     */
    fun <SOURCE : Any, FROM, TO> mapping(
        column: YawnQueryProjection<SOURCE, FROM>,
        transform: (FROM) -> TO,
    ): YawnQueryProjection<SOURCE, TO> {
        return Mapping1Projection(column, transform)
    }

    /**
     * A 2-arity version of the [mapping] method.
     */
    fun <SOURCE : Any, C1, C2, TO> mapping(
        column1: YawnQueryProjection<SOURCE, C1>,
        column2: YawnQueryProjection<SOURCE, C2>,
        transform: (C1, C2) -> TO,
    ): YawnQueryProjection<SOURCE, TO> {
        return Mapping2Projection(column1, column2, transform)
    }

    internal open class Mapping1Projection<SOURCE : Any, FROM, TO>(
        private val column: YawnQueryProjection<SOURCE, FROM>,
        private val transform: (FROM) -> TO,
    ) : YawnQueryProjection<SOURCE, TO> {
        override fun compile(context: YawnCompilationContext): Projection = column.compile(context)

        override fun project(value: Any?): TO = transform(column.project(value))
    }

    internal open class Mapping2Projection<SOURCE : Any, C1, C2, TO>(
        private val column1: YawnQueryProjection<SOURCE, C1>,
        private val column2: YawnQueryProjection<SOURCE, C2>,
        private val transform: (C1, C2) -> TO,
    ) : YawnQueryProjection<SOURCE, TO> {
        override fun compile(context: YawnCompilationContext): Projection {
            return Projections.projectionList()
                .add(column1.compile(context))
                .add(column2.compile(context))
        }

        override fun project(value: Any?): TO {
            val queryResult = value as Array<*>
            return transform(column1.project(queryResult[0]), column2.project(queryResult[1]))
        }
    }

    internal open class Mapping3Projection<SOURCE : Any, C1, C2, C3, TO>(
        private val column1: YawnQueryProjection<SOURCE, C1>,
        private val column2: YawnQueryProjection<SOURCE, C2>,
        private val column3: YawnQueryProjection<SOURCE, C3>,
        private val transform: (C1, C2, C3) -> TO,
    ) : YawnQueryProjection<SOURCE, TO> {
        override fun compile(context: YawnCompilationContext): Projection {
            return Projections.projectionList()
                .add(column1.compile(context))
                .add(column2.compile(context))
                .add(column3.compile(context))
        }

        override fun project(value: Any?): TO {
            val queryResult = value as Array<*>
            return transform(
                column1.project(queryResult[0]),
                column2.project(queryResult[1]),
                column3.project(queryResult[2]),
            )
        }
    }
}

private const val CONSTANT_ALIAS = "_yawn_ct"
