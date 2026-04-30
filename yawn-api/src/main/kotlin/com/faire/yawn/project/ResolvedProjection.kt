package com.faire.yawn.project

/**
 * The result of resolving a [YawnProjector] through the [ProjectorResolver] engine.
 *
 * Contains a flat list of [ProjectionNode.Value] nodes (all composites, constants, and mapped
 * transforms have been eliminated) and a function to map a result row back to the desired type.
 *
 * This interface is ORM-agnostic. The Query Factory is the one responsible for:
 * - Compiling each node's [ProjectionLeaf] to the underlying implementation projection type;
 * - Normalizing the ORM result row into a `List<Any?>` matching the order of [nodes];
 * - Calling [mapRow] to reconstruct the projected type.
 */
interface ResolvedProjection<SOURCE : Any, TO> {
    /**
     * The flat, deduped, re-ordered list of value nodes to compile into the query.
     * The query factory must compile them _in this order_, and result values
     * must be provided at matching indices when calling [mapRow].
     */
    val nodes: List<ProjectionNode.Value<SOURCE, *>>

    /**
     * The set of query-level modifiers (e.g. DISTINCT) that should be applied to the compiled projection.
     */
    val modifiers: Set<ModifierKind> get() = setOf()

    /**
     * Maps a raw result row to the projected type.
     *
     * @param values raw results in the same order as [nodes].
     */
    fun mapRow(values: List<Any?>): TO
}

internal class DefaultResolvedProjection<SOURCE : Any, TO>(
    override val nodes: List<ProjectionNode.Value<SOURCE, *>>,
    private val mapper: ProjectionMapper<TO>,
    override val modifiers: Set<ModifierKind> = setOf(),
) : ResolvedProjection<SOURCE, TO> {
    override fun mapRow(values: List<Any?>): TO = mapper.map(values)
}
