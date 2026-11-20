package com.faire.yawn.project

/**
 * An orderable counterpart to [YawnProjections.pair]: combines two single-value projections into a `Pair`, while
 * exposing each one as a [ProjectionSlot] so [com.faire.yawn.criteria.query.orderAscBy]/
 * [com.faire.yawn.criteria.query.orderDescBy] can order by one of them *after* the pair has already been built -
 * see [com.faire.yawn.criteria.query.ProjectedYawnQueryScope.project].
 *
 * Unlike [YawnProjections.pair], both children must be single-value projections (not nested composites), since only
 * a single SQL expression can be aliased and ordered by.
 */
class YawnProjectionPair<SOURCE : Any, A, B> internal constructor(
    val first: ProjectionSlot<SOURCE, A>,
    val second: ProjectionSlot<SOURCE, B>,
) : YawnProjector<SOURCE, Pair<A, B>> {
    override fun projection(): ProjectionNode<SOURCE, Pair<A, B>> {
        return ProjectionNode.composite(first, second) { a, b -> a to b }
    }
}

/** Three-way counterpart to [YawnProjectionPair]; see its documentation. */
class YawnProjectionTriple<SOURCE : Any, A, B, C> internal constructor(
    val first: ProjectionSlot<SOURCE, A>,
    val second: ProjectionSlot<SOURCE, B>,
    val third: ProjectionSlot<SOURCE, C>,
) : YawnProjector<SOURCE, Triple<A, B, C>> {
    override fun projection(): ProjectionNode<SOURCE, Triple<A, B, C>> {
        return ProjectionNode.composite(first, second, third) { a, b, c -> Triple(a, b, c) }
    }
}
