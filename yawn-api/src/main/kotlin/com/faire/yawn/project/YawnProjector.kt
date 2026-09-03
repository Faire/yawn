package com.faire.yawn.project

import com.faire.yawn.query.YawnCompilationContext
import org.hibernate.criterion.Projection

/**
 * A projection descriptor that can be resolved into a flat list of [ProjectionNode.Value] nodes for query compilation.
 *
 * Implementations describe the shape of a projection by returning a [ProjectionNode] from [projection]. The
 * [ProjectorResolver] resolution engine then walks this tree, flattening composites, eliminating constants and mapped
 * transforms, deduplicating identical leaves, and producing a [ResolvedProjection] that the query factory can compile.
 *
 * Extends [YawnQueryProjection] so that a projector can be handed to anything that still speaks the older
 * interface - `project(...)` above all - without every such signature needing an overload. That is what lets the
 * two coexist while projections migrate onto [ProjectionNode].
 *
 * Note that [compile] and [project] resolve the tree on **every** call, and [project] is called once per result
 * row. Callers that hold on to a projection should resolve it once up front instead.
 *
 * @param SOURCE the type of the entity being queried.
 * @param TO the result type of this projection.
 */
fun interface YawnProjector<SOURCE : Any, TO> : YawnQueryProjection<SOURCE, TO> {
    fun projection(): ProjectionNode<SOURCE, TO>

    override fun compile(context: YawnCompilationContext): Projection = resolve().compile(context)

    override fun project(value: Any?): TO = resolve().project(value)

    private fun resolve(): ResolvedProjectionAdapter<SOURCE, TO> {
        return ResolvedProjectionAdapter(ProjectorResolver<SOURCE>().resolve(this))
    }
}

/**
 * A [YawnProjector] that is guaranteed to produce a [ProjectionNode.Value].
 *
 * This subtype exists mostly so that modifiers like distinct can enforce at compile time that they only wrap
 * single-value projections, not composites. But it can be used for other contexts as needed.
 */
fun interface YawnValueProjector<SOURCE : Any, TO> : YawnProjector<SOURCE, TO> {
    override fun projection(): ProjectionNode.Value<SOURCE, TO>
}
