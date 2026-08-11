package com.faire.yawn.project

import com.faire.yawn.query.YawnCompilationContext
import org.hibernate.criterion.Projection

/**
 * A projection of a single value, such as a custom SQL expression (see [YawnProjections.sqlValue]).
 *
 * This implements both projection interfaces on purpose, so that a single projected value composes everywhere an
 * ordinary column does, with no adapter at the call site:
 * * as a [YawnValueProjector], inside [ProjectionNode.composite], [ProjectionNode.mapped] and modifiers;
 * * as a [YawnQueryProjection], inside [YawnProjections.pair], [YawnProjections.triple], the generated `create`
 *   function of a [YawnProjection] class, and directly as a query's whole projection.
 */
class YawnSingleValueProjection<SOURCE : Any, TO> internal constructor(
    private val leaf: ProjectionLeaf<SOURCE>,
    private val mapper: (Any?) -> TO,
) : YawnValueProjector<SOURCE, TO>, YawnQueryProjection<SOURCE, TO> {
    override fun projection(): ProjectionNode.Value<SOURCE, TO> = ProjectionNode.Value(leaf, mapper)

    /**
     * Resolving a single value is cheap and context-free, so the bridge into the [YawnQueryProjection] pipeline is
     * built once and reused. The expression itself is still rendered per query, from its own compilation context.
     */
    private val adapter: ResolvedProjectionAdapter<SOURCE, TO> by lazy {
        ResolvedProjectionAdapter(ProjectorResolver<SOURCE>().resolve(this))
    }

    override fun compile(context: YawnCompilationContext): Projection = adapter.compile(context)

    override fun project(value: Any?): TO = adapter.project(value)
}
