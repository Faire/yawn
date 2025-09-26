package com.faire.yawn.query

/**
 * Defines a Yawn query that can support criteria
 */
interface YawnCriteriaQuery<SOURCE : Any, T : Any> {
    fun addCriterion(criterion: YawnQueryCriterion<SOURCE>)
}
