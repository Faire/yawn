package com.faire.yawn.project

import com.faire.yawn.query.YawnCompilationContext
import org.hibernate.criterion.Projection

/**
 * A type-safe projection descriptor that can be resolved into a flat list of
 * [ProjectionNode.Value] nodes for query compilation.
 *
 * Implementations describe the shape of a projection by returning a [ProjectionNode]
 * from [projection]. The [ProjectorResolver] resolution engine then walks this tree,
 * flattening composites, eliminating constants and mapped transforms, deduplicating
 * identical leaves, and producing a [ResolvedProjection] that the query factory can compile.
 *
 * Extends [YawnQueryProjection] so that projectors can be used anywhere a legacy projection is
 * expected. The default [compile] and [project] implementations resolve through the v2 pipeline.
 * In the query pipeline, [ProjectedTypeSafeCriteriaQuery.project] detects projectors and wraps
 * them in a [ResolvedProjectionAdapter] once, avoiding repeated resolution.
 *
 * Note: this is a `fun interface` for ergonomic lambda syntax, but JVM SAM lambdas do NOT
 * inherit default method implementations from super-interfaces. Production code that needs
 * `compile()` or `project()` to work must use the [YawnValueProjector] or [YawnProjector]
 * factory functions instead of SAM conversion.
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
 * This subtype exists so that modifiers like distinct can enforce at compile time
 * that they only wrap single-value projections, not composites.
 */
fun interface YawnValueProjector<SOURCE : Any, TO> : YawnProjector<SOURCE, TO> {
    override fun projection(): ProjectionNode.Value<SOURCE, TO>
}

/**
 * Factory function for [YawnProjector] that creates a concrete anonymous object
 * instead of a SAM lambda. Avoids the JVM SAM default-method inheritance limitation.
 */
fun <SOURCE : Any, TO> YawnProjector(
    projection: () -> ProjectionNode<SOURCE, TO>,
): YawnProjector<SOURCE, TO> {
    return object : YawnProjector<SOURCE, TO> {
        override fun projection() = projection()
    }
}

/**
 * Factory function for [YawnValueProjector] that creates a concrete anonymous object
 * instead of a SAM lambda. Avoids the JVM SAM default-method inheritance limitation.
 */
fun <SOURCE : Any, TO> YawnValueProjector(
    projection: () -> ProjectionNode.Value<SOURCE, TO>,
): YawnValueProjector<SOURCE, TO> {
    return object : YawnValueProjector<SOURCE, TO> {
        override fun projection() = projection()
    }
}
