package com.faire.yawn.query

/**
 * A type safe criterion.
 *
 * It is type safe by restricting construction of this class by TypeSafeRestrictions which is type safe.
 */
data class YawnQueryCriterion<SOURCE : Any>(
    val yawnRestriction: YawnQueryRestriction<SOURCE>,
)
