package com.faire.yawn.project

/**
 * A [YawnValueProjector] describing a projection of a single value, such as a custom SQL expression (see
 * [com.faire.yawn.criteria.query.ProjectedYawnQueryScope.sqlValue]); composable with other projection nodes.
 */
open class YawnSingleValueProjection<SOURCE : Any, TO>(
    private val leaf: ProjectionLeaf<SOURCE>,
    private val mapper: (Any?) -> TO,
) : YawnValueProjector<SOURCE, TO> {
    final override fun projection(): ProjectionNode.Value<SOURCE, TO> = ProjectionNode.Value(leaf, mapper)
}
