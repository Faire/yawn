package com.faire.yawn.project

import com.faire.yawn.query.YawnCompilationContext
import org.hibernate.criterion.Projection
import org.hibernate.criterion.Projections

/**
 * A composite implementation of [YawnQueryProjection] that allows you to specify both a list of projections
 * and the mapper function.
 */
class YawnCompositeQueryProjection<SOURCE : Any, TO>(
    private val projections: List<YawnQueryProjection<SOURCE, *>>,
    private val mapper: (Any?) -> TO,
) : YawnQueryProjection<SOURCE, TO> {

    constructor(vararg projections: YawnQueryProjection<SOURCE, *>, mapper: (Any?) -> TO) : this(
        projections = projections.toList(),
        mapper = mapper,
    )

    override fun compile(context: YawnCompilationContext): Projection = Projections.projectionList().apply {
        for (projection in projections) {
            add(projection.compile(context))
        }
    }

    override fun project(value: Any?): TO {
        return mapper(value)
    }
}
