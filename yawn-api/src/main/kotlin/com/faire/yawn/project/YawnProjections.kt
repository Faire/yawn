package com.faire.yawn.project

import com.faire.yawn.YawnDef
import com.faire.yawn.project.AggregateKind.AVG
import com.faire.yawn.project.AggregateKind.COUNT
import com.faire.yawn.project.AggregateKind.COUNT_DISTINCT
import com.faire.yawn.project.AggregateKind.GROUP_BY
import com.faire.yawn.project.AggregateKind.MAX
import com.faire.yawn.project.AggregateKind.MIN
import com.faire.yawn.project.AggregateKind.SUM

/**
 * Yawn's projection factory object. This is the type-safe equivalent of Hibernate's `Projections`.
 *
 * All methods produce [YawnProjector] instances that describe projections as [ProjectionNode] trees.
 * The [ProjectorResolver] flattens these trees into [ResolvedProjection]s at query build time,
 * and [ResolvedProjectionAdapter] bridges them to Hibernate for execution.
 */
object YawnProjections {
    fun <SOURCE : Any, FROM> count(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnProjector<SOURCE, Long> {
        return YawnValueProjector { ProjectionNode.aggregateAs(COUNT, columnDef) }
    }

    fun <SOURCE : Any, FROM> countDistinct(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnProjector<SOURCE, Long> {
        return YawnValueProjector { ProjectionNode.aggregateAs(COUNT_DISTINCT, columnDef) }
    }

    @JvmName("sumNullable")
    fun <SOURCE : Any, FROM : Number?> sum(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnProjector<SOURCE, Long?> {
        return YawnValueProjector { ProjectionNode.aggregateAs(SUM, columnDef) }
    }

    fun <SOURCE : Any, FROM : Number> sum(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnProjector<SOURCE, Long> {
        return YawnValueProjector { ProjectionNode.aggregateAs(SUM, columnDef) }
    }

    @JvmName("avgNullable")
    fun <SOURCE : Any, FROM : Number?> avg(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnProjector<SOURCE, Double?> {
        return YawnValueProjector { ProjectionNode.aggregateAs(AVG, columnDef) }
    }

    fun <SOURCE : Any, FROM : Number> avg(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnProjector<SOURCE, Double> {
        return YawnValueProjector { ProjectionNode.aggregateAs(AVG, columnDef) }
    }

    fun <SOURCE : Any, FROM : Comparable<FROM>?> max(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnProjector<SOURCE, FROM> {
        return YawnValueProjector { ProjectionNode.aggregate(MAX, columnDef) }
    }

    fun <SOURCE : Any, FROM : Comparable<FROM>?> min(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnProjector<SOURCE, FROM> {
        return YawnValueProjector { ProjectionNode.aggregate(MIN, columnDef) }
    }

    fun <SOURCE : Any, FROM> groupBy(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnProjector<SOURCE, FROM> {
        return YawnValueProjector { ProjectionNode.aggregate(GROUP_BY, columnDef) }
    }

    fun <SOURCE : Any> rowCount(): YawnProjector<SOURCE, Long> {
        return YawnValueProjector { ProjectionNode.rowCount() }
    }

<<<<<<< HEAD
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
=======
    fun <SOURCE : Any> selectConstant(constant: String): YawnProjector<SOURCE, String> {
        return YawnValueProjector {
            ProjectionNode.sql(
                sqlExpression = "'$constant' as $CONSTANT_ALIAS",
                aliases = listOf(CONSTANT_ALIAS),
                resultTypes = listOf(String::class),
>>>>>>> 5beba20 (feat: Implementation using the new projection structure [example])
            )
        }
    }

    fun <SOURCE : Any, T : Any> `null`(): YawnProjector<SOURCE, T?> {
        return YawnValueProjector {
            ProjectionNode.sql(
                sqlExpression = "null as $CONSTANT_ALIAS",
                aliases = listOf(CONSTANT_ALIAS),
                resultTypes = listOf(String::class),
            )
        }
    }

    fun <SOURCE : Any, TO> distinct(
        projection: YawnValueProjector<SOURCE, TO>,
    ): YawnValueProjector<SOURCE, TO> {
        return YawnValueProjector {
            ProjectionNode.Value(
                ProjectionLeaf.Modifier(ModifierKind.DISTINCT, projection.projection().leaf),
            )
        }
    }

    fun <SOURCE : Any, FROM : Any> coalesce(
        projection: YawnProjector<SOURCE, FROM?>,
        defaultValue: FROM,
<<<<<<< HEAD
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
=======
    ): YawnProjector<SOURCE, FROM> {
        return YawnProjector { ProjectionNode.mapped(projection) { it ?: defaultValue } }
>>>>>>> 5beba20 (feat: Implementation using the new projection structure [example])
    }

    fun <SOURCE : Any, A, B> pair(
        firstProjection: YawnProjector<SOURCE, A>,
        secondProjection: YawnProjector<SOURCE, B>,
    ): YawnProjector<SOURCE, Pair<A, B>> {
        return YawnProjector {
            ProjectionNode.composite(firstProjection, secondProjection) { a, b -> a to b }
        }
    }

    fun <SOURCE : Any, A, B, C> triple(
        firstProjection: YawnProjector<SOURCE, A>,
        secondProjection: YawnProjector<SOURCE, B>,
        thirdProjection: YawnProjector<SOURCE, C>,
    ): YawnProjector<SOURCE, Triple<A, B, C>> {
        return YawnProjector {
            ProjectionNode.composite(firstProjection, secondProjection, thirdProjection) { a, b, c ->
                Triple(a, b, c)
            }
        }
    }
}
