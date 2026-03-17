package com.faire.yawn.project

/**
 * Maps a raw result row (as a list of values corresponding to resolved projection leaves)
 * into the desired projection type.
 *
 * This is an internal type produced by the [ProjectorResolver] resolution engine.
 * You should never need to implement this directly; use the simple lambdas pointing to [ProjectionNode.Value],
 * [ProjectionNode.Composite], or [ProjectionNode.Mapped].
 */
internal fun interface ProjectionMapper<TO> {
    fun map(results: List<Any?>): TO
}
