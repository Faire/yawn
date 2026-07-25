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

    fun <SOURCE : Any> selectConstant(constant: String): YawnProjector<SOURCE, String> {
        return YawnValueProjector {
            ProjectionNode.Value(
                ProjectionLeaf.SqlValue(
                    render = { "'$constant'" },
                    resultType = String::class,
                ),
            )
        }
    }

    fun <SOURCE : Any, T : Any> `null`(): YawnProjector<SOURCE, T?> {
        return YawnValueProjector {
            ProjectionNode.Value(
                ProjectionLeaf.SqlValue(
                    render = { "null" },
                    resultType = String::class,
                ),
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
        projection: YawnQueryProjection<SOURCE, FROM?>,
        defaultValue: FROM,
    ): YawnProjector<SOURCE, FROM> {
        return YawnProjector { ProjectionNode.mapped(projection.asProjector()) { it ?: defaultValue } }
    }

    fun <SOURCE : Any, A, B> pair(
        firstProjection: YawnQueryProjection<SOURCE, A>,
        secondProjection: YawnQueryProjection<SOURCE, B>,
    ): YawnProjector<SOURCE, Pair<A, B>> {
        return YawnProjector {
            ProjectionNode.composite(firstProjection.asProjector(), secondProjection.asProjector()) { a, b ->
                a to b
            }
        }
    }

    fun <SOURCE : Any, A, B, C> triple(
        firstProjection: YawnQueryProjection<SOURCE, A>,
        secondProjection: YawnQueryProjection<SOURCE, B>,
        thirdProjection: YawnQueryProjection<SOURCE, C>,
    ): YawnProjector<SOURCE, Triple<A, B, C>> {
        return YawnProjector {
            ProjectionNode.composite(
                firstProjection.asProjector(),
                secondProjection.asProjector(),
                thirdProjection.asProjector(),
            ) { a, b, c ->
                Triple(a, b, c)
            }
        }
    }
}
