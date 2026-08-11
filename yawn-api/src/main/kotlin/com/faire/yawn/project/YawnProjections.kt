package com.faire.yawn.project

import com.faire.yawn.YawnDef
import com.faire.yawn.query.YawnCompilationContext
import org.hibernate.criterion.Projection
import org.hibernate.criterion.Projections
import org.hibernate.type.StandardBasicTypes
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

/**
 * Yawn equivalent of Hibernate [Projections].
 * A utility object to create type-safe [YawnQueryProjection].
 */
object YawnProjections {
    internal class Count<SOURCE : Any, FROM>(
        private val columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ) : YawnQueryProjection<SOURCE, Long> {
        override fun compile(
            context: YawnCompilationContext,
        ): Projection = Projections.count(columnDef.generatePath(context))

        override fun project(value: Any?): Long = value as Long
    }

    fun <SOURCE : Any, FROM> count(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnQueryProjection<SOURCE, Long> {
        return Count(columnDef)
    }

    internal class CountDistinct<SOURCE : Any, FROM>(
        private val columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ) : YawnQueryProjection<SOURCE, Long> {
        override fun compile(
            context: YawnCompilationContext,
        ): Projection = Projections.countDistinct(columnDef.generatePath(context))

        override fun project(value: Any?): Long = value as Long
    }

    fun <SOURCE : Any, FROM> countDistinct(
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

    internal class AvgNullable<SOURCE : Any, FROM>(
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

    internal class GroupBy<SOURCE : Any, FROM>(
        private val columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ) : YawnQueryProjection<SOURCE, FROM> {
        override fun compile(
            context: YawnCompilationContext,
        ): Projection = Projections.groupProperty(columnDef.generatePath(context))

        @Suppress("UNCHECKED_CAST")
        override fun project(value: Any?): FROM = value as FROM
    }

    fun <SOURCE : Any, FROM> groupBy(
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
            val alias = context.generateResultAlias()
            return Projections.sqlProjection(
                "'$constant' as $alias",
                arrayOf(alias),
                arrayOf(StandardBasicTypes.STRING),
            )
        }

        override fun project(value: Any?): String = value as String
    }

    fun <SOURCE : Any> selectConstant(constant: String): YawnQueryProjection<SOURCE, String> {
        return SelectConstant(constant)
    }

    /**
     * Projects a single value computed by a raw SQL [expression], e.g. `SUM(quantity * price)`.
     *
     * Use this instead of implementing [YawnQueryProjection] by hand when all you need is one custom SQL value.
     * The result composes anywhere an ordinary column does; see [YawnSingleValueProjection].
     *
     * Reference columns through [YawnSqlScope.sql], which substitutes the physical column backing a property,
     * already qualified by its table's alias:
     *
     * ```
     * project(YawnProjections.sqlValue<Long> { "SUM(${books.numberOfPages.sql} * 2)" })
     * ```
     *
     * The type argument decides how the result is mapped, and should be nullable if the expression can evaluate
     * to `NULL`. As with any raw SQL projection, this is a claim Yawn takes at face value rather than something
     * it can verify: it is up to you to ensure the expression really does produce that type.
     *
     * Do not name the result yourself (no `AS total`): Yawn selects it under an alias it generates, unique within
     * the query. Note also that raw SQL cannot bind parameters, so any value in [expression] is inlined verbatim
     * — never interpolate untrusted input into it.
     */
    inline fun <SOURCE : Any, reified TO> sqlValue(
        noinline expression: YawnSqlScope<SOURCE>.() -> String,
    ): YawnSingleValueProjection<SOURCE, TO> {
        val type = typeOf<TO>()
        return sqlValueOf(
            resultType = type.classifier as? KClass<*> ?: error("Cannot project to $type"),
            expression = expression,
        )
    }

    @PublishedApi
    internal fun <SOURCE : Any, TO> sqlValueOf(
        resultType: KClass<*>,
        expression: YawnSqlScope<SOURCE>.() -> String,
    ): YawnSingleValueProjection<SOURCE, TO> {
        return YawnSingleValueProjection(ProjectionLeaf.SqlValue(expression, resultType)) {
            @Suppress("UNCHECKED_CAST")
            it as TO
        }
    }

    internal class Coalesce<SOURCE : Any, FROM>(
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
            val alias = context.generateResultAlias()
            return Projections.sqlProjection(
                "null as $alias",
                arrayOf(alias),
                arrayOf(StandardBasicTypes.STRING),
            )
        }

        override fun project(value: Any?): FROM? = null
    }

    fun <SOURCE : Any, T : Any> `null`(): YawnQueryProjection<SOURCE, T?> {
        return Null()
    }

    internal class PairProjection<SOURCE : Any, A, B>(
        private val firstProjection: YawnQueryProjection<SOURCE, A>,
        private val secondProjection: YawnQueryProjection<SOURCE, B>,
    ) : YawnQueryProjection<SOURCE, Pair<A, B>> {
        override fun compile(context: YawnCompilationContext): Projection {
            return Projections.projectionList()
                .add(firstProjection.compile(context))
                .add(secondProjection.compile(context))
        }

        override fun project(value: Any?): Pair<A, B> {
            val queryResult = value as Array<*>
            return Pair(firstProjection.project(queryResult[0]), secondProjection.project(queryResult[1]))
        }
    }

    fun <SOURCE : Any, A, B> pair(
        firstProjection: YawnQueryProjection<SOURCE, A>,
        secondProjection: YawnQueryProjection<SOURCE, B>,
    ): YawnQueryProjection<SOURCE, Pair<A, B>> {
        return PairProjection(firstProjection, secondProjection)
    }

    internal class TripleProjection<SOURCE : Any, A, B, C>(
        private val firstProjection: YawnQueryProjection<SOURCE, A>,
        private val secondProjection: YawnQueryProjection<SOURCE, B>,
        private val thirdProjection: YawnQueryProjection<SOURCE, C>,
    ) : YawnQueryProjection<SOURCE, Triple<A, B, C>> {
        override fun compile(context: YawnCompilationContext): Projection {
            return Projections.projectionList()
                .add(firstProjection.compile(context))
                .add(secondProjection.compile(context))
                .add(thirdProjection.compile(context))
        }

        override fun project(value: Any?): Triple<A, B, C> {
            val queryResult = value as Array<*>
            return Triple(
                firstProjection.project(queryResult[0]),
                secondProjection.project(queryResult[1]),
                thirdProjection.project(queryResult[2]),
            )
        }
    }

    fun <SOURCE : Any, A, B, C> triple(
        firstProjection: YawnQueryProjection<SOURCE, A>,
        secondProjection: YawnQueryProjection<SOURCE, B>,
        thirdProjection: YawnQueryProjection<SOURCE, C>,
    ): YawnQueryProjection<SOURCE, Triple<A, B, C>> {
        return TripleProjection(firstProjection, secondProjection, thirdProjection)
    }
}
