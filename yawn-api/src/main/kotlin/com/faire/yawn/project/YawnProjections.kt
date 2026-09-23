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
    ): YawnValueProjector<SOURCE, Long> {
        return YawnValueProjector { ProjectionNode.aggregateAs(COUNT, columnDef) }
    }

    fun <SOURCE : Any, FROM> countDistinct(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnValueProjector<SOURCE, Long> {
        return YawnValueProjector { ProjectionNode.aggregateAs(COUNT_DISTINCT, columnDef) }
    }

    @JvmName("sumNullable")
    fun <SOURCE : Any, FROM : Number?> sum(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnValueProjector<SOURCE, Long?> {
        return YawnValueProjector { ProjectionNode.aggregateAs(SUM, columnDef) }
    }

    fun <SOURCE : Any, FROM : Number> sum(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnValueProjector<SOURCE, Long> {
        return YawnValueProjector { ProjectionNode.aggregateAs(SUM, columnDef) }
    }

    @JvmName("avgNullable")
    fun <SOURCE : Any, FROM : Number?> avg(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnValueProjector<SOURCE, Double?> {
        return YawnValueProjector { ProjectionNode.aggregateAs(AVG, columnDef) }
    }

    fun <SOURCE : Any, FROM : Number> avg(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnValueProjector<SOURCE, Double> {
        return YawnValueProjector { ProjectionNode.aggregateAs(AVG, columnDef) }
    }

    fun <SOURCE : Any, FROM : Comparable<FROM>?> max(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnValueProjector<SOURCE, FROM> {
        return YawnValueProjector { ProjectionNode.aggregate(MAX, columnDef) }
    }

    fun <SOURCE : Any, FROM : Comparable<FROM>?> min(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnValueProjector<SOURCE, FROM> {
        return YawnValueProjector { ProjectionNode.aggregate(MIN, columnDef) }
    }

    fun <SOURCE : Any, FROM> groupBy(
        columnDef: YawnDef<SOURCE, *>.YawnColumnDef<FROM>,
    ): YawnValueProjector<SOURCE, FROM> {
        return YawnValueProjector { ProjectionNode.aggregate(GROUP_BY, columnDef) }
    }

    fun <SOURCE : Any> rowCount(): YawnValueProjector<SOURCE, Long> {
        return YawnValueProjector { ProjectionNode.rowCount() }
    }

    /**
     * Selects a literal string, which needs no column and so is rendered as raw SQL. The query factory selects it
     * under an alias it generates, so several constants in one projection cannot collide on the same column.
     */
    fun <SOURCE : Any> selectConstant(constant: String): YawnValueProjector<SOURCE, String> {
        return YawnValueProjector {
            ProjectionNode.Value(ProjectionLeaf.SqlValue({ "'$constant'" }, String::class))
        }
    }

    /** Selects a literal `NULL`; see [selectConstant] for how it is aliased. */
    fun <SOURCE : Any, T : Any> `null`(): YawnValueProjector<SOURCE, T?> {
        return YawnValueProjector {
            ProjectionNode.Value(ProjectionLeaf.SqlValue({ "null" }, String::class))
        }
    }

    fun <SOURCE : Any, FROM : Any> coalesce(
        projection: YawnProjector<SOURCE, FROM?>,
        defaultValue: FROM,
    ): YawnProjector<SOURCE, FROM> {
        return mapping(projection) { it ?: defaultValue }
    }

    /**
     * Transforms a projected value into another type, in memory.
     *
     * Use this to keep a projection class type-safe when a column does not already hold the type it wants - a
     * conversion, a wrapper, a value compliance rule - rather than projecting to an intermediate representation
     * and mapping it afterwards.
     *
     * Note this does not change the query in any way: the same column is selected, and [transform] runs on each
     * row as its results come back.
     */
    fun <SOURCE : Any, FROM, TO> mapping(
        projection: YawnProjector<SOURCE, FROM>,
        transform: (FROM) -> TO,
    ): YawnProjector<SOURCE, TO> {
        return YawnProjector { ProjectionNode.mapped(projection, transform) }
    }

    /** Combines two projected values into one, in memory; see [mapping]. */
    fun <SOURCE : Any, A, B, TO> mapping(
        firstProjection: YawnProjector<SOURCE, A>,
        secondProjection: YawnProjector<SOURCE, B>,
        transform: (A, B) -> TO,
    ): YawnProjector<SOURCE, TO> {
        return YawnProjector { ProjectionNode.composite(firstProjection, secondProjection, transform) }
    }

    /** Combines three projected values into one, in memory; see [mapping]. */
    fun <SOURCE : Any, A, B, C, TO> mapping(
        firstProjection: YawnProjector<SOURCE, A>,
        secondProjection: YawnProjector<SOURCE, B>,
        thirdProjection: YawnProjector<SOURCE, C>,
        transform: (A, B, C) -> TO,
    ): YawnProjector<SOURCE, TO> {
        return YawnProjector {
            ProjectionNode.composite(firstProjection, secondProjection, thirdProjection, transform)
        }
    }

    fun <SOURCE : Any, A, B> pair(
        firstProjection: YawnProjector<SOURCE, A>,
        secondProjection: YawnProjector<SOURCE, B>,
    ): YawnProjector<SOURCE, Pair<A, B>> {
        return mapping(firstProjection, secondProjection) { a, b -> a to b }
    }

    fun <SOURCE : Any, A, B, C> triple(
        firstProjection: YawnProjector<SOURCE, A>,
        secondProjection: YawnProjector<SOURCE, B>,
        thirdProjection: YawnProjector<SOURCE, C>,
    ): YawnProjector<SOURCE, Triple<A, B, C>> {
        return mapping(firstProjection, secondProjection, thirdProjection) { a, b, c -> Triple(a, b, c) }
    }

    /**
     * Like [pair], but returns a [YawnProjectionPair] exposing each child as a [ProjectionSlot], so that
     * [com.faire.yawn.criteria.query.orderAscBy]/[com.faire.yawn.criteria.query.orderDescBy] can order by one of
     * them from inside the configuring block of
     * [com.faire.yawn.criteria.query.ProjectedYawnQueryScope.project]'s two-argument overload, rather than
     * pre-wrapping it before the pair is built:
     *
     * ```kotlin
     * project(
     *     YawnProjections.orderablePair(YawnProjections.groupBy(visits.brandId), YawnProjections.max(visits.createdAt)),
     * ) { pair ->
     *     orderDescBy(pair.second)
     * }
     * ```
     */
    fun <SOURCE : Any, A, B> orderablePair(
        firstProjection: YawnValueProjector<SOURCE, A>,
        secondProjection: YawnValueProjector<SOURCE, B>,
    ): YawnProjectionPair<SOURCE, A, B> {
        return YawnProjectionPair(ProjectionSlot(firstProjection), ProjectionSlot(secondProjection))
    }

    /** Three-way counterpart to [orderablePair]; see its documentation. */
    fun <SOURCE : Any, A, B, C> orderableTriple(
        firstProjection: YawnValueProjector<SOURCE, A>,
        secondProjection: YawnValueProjector<SOURCE, B>,
        thirdProjection: YawnValueProjector<SOURCE, C>,
    ): YawnProjectionTriple<SOURCE, A, B, C> {
        return YawnProjectionTriple(
            ProjectionSlot(firstProjection),
            ProjectionSlot(secondProjection),
            ProjectionSlot(thirdProjection),
        )
    }
}
