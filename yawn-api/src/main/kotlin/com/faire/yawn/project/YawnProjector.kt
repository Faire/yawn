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
 * Extends [YawnQueryProjection] so a projector can be used anywhere the older interface is expected, which is what
 * lets the two coexist while everything migrates. Queries never rely on the [compile]/[project] implementations
 * here: [com.faire.yawn.criteria.query.ProjectedYawnQueryScope.project] resolves a projector once, up front, and
 * hands the query a [ResolvedProjectionAdapter]. These exist only so a projector handed to some other API that
 * still speaks [YawnQueryProjection] behaves correctly, and they resolve the tree on every call to do it.
 *
 * @param SOURCE the type of the entity being queried.
 * @param TO the result type of this projection.
 */
fun interface YawnProjector<SOURCE : Any, TO> : YawnQueryProjection<SOURCE, TO> {
    fun projection(): ProjectionNode<SOURCE, TO>

    override fun compile(context: YawnCompilationContext): Projection {
        return resolve().compile(context)
    }

    override fun project(value: Any?): TO {
        return resolve().project(value)
    }

    private fun resolve(): ResolvedProjectionAdapter<SOURCE, TO> {
        return ResolvedProjectionAdapter(ProjectorResolver<SOURCE>().resolve(this))
    }
}

/**
 * A [YawnProjector] that is guaranteed to produce a [ProjectionNode.Value].
 *
 * This subtype exists mostly so that modifiers like distinct can enforce at compile time that they only wrap
 * single-value projections, not composites. It is also what [com.faire.yawn.YawnDef.YawnColumnDef] implements,
 * since a column is always exactly one value.
 */
fun interface YawnValueProjector<SOURCE : Any, TO> : YawnProjector<SOURCE, TO> {
    override fun projection(): ProjectionNode.Value<SOURCE, TO>
}
