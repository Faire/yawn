package com.faire.yawn.project

/**
 * A type-safe projection descriptor that can be resolved into a flat list of
 * [ProjectionNode.Value] nodes for query compilation.
 *
 * Implementations describe the shape of a projection by returning a [ProjectionNode]
 * from [projection]. The [ProjectorResolver] resolution engine then walks this tree,
 * flattening composites, eliminating constants and mapped transforms, deduplicating
 * identical leaves, and producing a [ResolvedProjection] that the query factory can compile.
 *
 * @param SOURCE the type of the entity being queried.
 * @param TO the result type of this projection.
 */
fun interface YawnProjector<SOURCE : Any, TO> {
    fun projection(): ProjectionNode<SOURCE, TO>
}

/**
 * A [YawnProjector] that is guaranteed to produce a [ProjectionNode.Value].
 *
 * This subtype exists mostly so that modifiers like distinct can enforce at compile time
 * that they only wrap single-value projections, not composites. But it can be used for other
 * contexts as needed.
 */
fun interface YawnValueProjector<SOURCE : Any, TO> : YawnProjector<SOURCE, TO> {
    override fun projection(): ProjectionNode.Value<SOURCE, TO>
}
