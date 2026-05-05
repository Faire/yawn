package com.faire.yawn.query

/**
 * Part of an [YawnQuery] representing each criterion in the WHERE clause.
 *
 * It is a thin wrapper over a constructed [YawnQueryRestriction].
 * Can be safely constructed by using the helpers on [YawnRestrictions].
 */
data class YawnQueryCriterion<SOURCE : Any>(
    val yawnRestriction: YawnQueryRestriction<SOURCE>,
)
