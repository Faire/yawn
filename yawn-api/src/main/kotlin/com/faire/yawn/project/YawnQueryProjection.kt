package com.faire.yawn.project

import com.faire.yawn.query.YawnCompilationContext
import org.hibernate.Criteria
import org.hibernate.criterion.Projection

/**
 * Return type of a call to a `project` method.
 * This is what the Query DSL expects to be returned from the lambda.
 *
 * @param SOURCE the type of the original table that the criteria is based off of.
 * @param TO the type being projected to
 */
interface YawnQueryProjection<SOURCE : Any, TO> {
    fun compile(context: YawnCompilationContext): Projection

    fun project(value: Any?): TO

    fun apply(context: YawnCompilationContext, criteria: Criteria) {
        criteria.setProjection(compile(context))
    }
}
