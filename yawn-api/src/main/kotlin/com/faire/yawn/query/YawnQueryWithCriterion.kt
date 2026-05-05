package com.faire.yawn.query

/**
 * Defines any [YawnQuery]-like object that can support [YawnQueryCriterion].
 * Used for registering sub-criteria for joined or detached queries.
 */
interface YawnQueryWithCriterion<SOURCE : Any, T : Any> {
    fun addCriterion(criterion: YawnQueryCriterion<SOURCE>)
}
