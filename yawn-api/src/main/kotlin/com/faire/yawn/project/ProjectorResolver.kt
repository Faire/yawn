package com.faire.yawn.project

/**
 * Yawn's projection resolution engine, which allows for complex nested projection arrangements that are then
 * flattened into an ORM-friendly node structure.
 *
 * Walks a [ProjectionNode] tree and produces a [ResolvedProjection] by:
 * - Flattening [ProjectionNode.Composite] nodes (their children become top-level)
 * - Eliminating [ProjectionNode.Constant] nodes (no SQL, value folded into mapper)
 * - Eliminating [ProjectionNode.Mapped] nodes (transform folded into mapper)
 * - Keeping [ProjectionNode.Value] nodes in a flat list
 * - Deduplicating identical [ProjectionLeaf] instances (same leaf shares an index)
 * - Composing a [ProjectionMapper] that reconstructs the full result from the flat list
 */
class ProjectorResolver<SOURCE : Any> {
    private val nodes = mutableListOf<ProjectionNode.Value<SOURCE, *>>()
    private val dedupedLeafsToIndices = mutableMapOf<ProjectionLeaf<SOURCE>, Int>()

    /**
     * Resolves a [YawnProjector] into a [ResolvedProjection].
     */
    fun <TO> resolve(projector: YawnProjector<SOURCE, TO>): ResolvedProjection<SOURCE, TO> {
        val mapper = resolveNode(projector.projection())
        return DefaultResolvedProjection(nodes.toList(), mapper)
    }

    private fun <TO> resolveNode(node: ProjectionNode<SOURCE, TO>): ProjectionMapper<TO> {
        return when (node) {
            is ProjectionNode.Value -> resolveValue(node)
            is ProjectionNode.Composite -> resolveComposite(node)
            is ProjectionNode.Constant -> ProjectionMapper { node.value }
            is ProjectionNode.Mapped<SOURCE, *, TO> -> resolveMapped(node)
        }
    }

    private fun <TO> resolveValue(node: ProjectionNode.Value<SOURCE, TO>): ProjectionMapper<TO> {
        val idx = dedupedLeafsToIndices.getOrPut(node.leaf) {
            nodes.add(node)
            nodes.size - 1
        }
        return ProjectionMapper { results -> node.mapper(results[idx]) }
    }

    private fun <TO> resolveComposite(node: ProjectionNode.Composite<SOURCE, TO>): ProjectionMapper<TO> {
        val childMappers = node.children.map { resolveNode(it.projection()) }
        return ProjectionMapper { results -> node.mapper(childMappers.map { it.map(results) }) }
    }

    private fun <FROM, TO> resolveMapped(node: ProjectionNode.Mapped<SOURCE, FROM, TO>): ProjectionMapper<TO> {
        val innerMapper = resolveNode(node.from.projection())
        return ProjectionMapper { results -> node.transform(innerMapper.map(results)) }
    }
}

/**
 * Resolves a projector's tree once, up front, into the [ResolvedProjectionAdapter] that will execute it.
 *
 * Anything already expressed as a [YawnQueryProjection] has no tree to walk and is returned unchanged. Use this
 * wherever a projection is stored and then used repeatedly, because [YawnQueryProjection.project] runs once per
 * result row: [YawnProjector] can satisfy that interface on its own, but only by re-resolving the whole tree on
 * every single call.
 */
fun <SOURCE : Any, TO> YawnQueryProjection<SOURCE, TO>.resolveOnce(): YawnQueryProjection<SOURCE, TO> {
    if (this !is YawnProjector<SOURCE, TO>) return this

    return ResolvedProjectionAdapter(ProjectorResolver<SOURCE>().resolve(this))
}
