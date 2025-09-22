package com.faire.yawn.query

/**
 * A type-safe wrapper around a query hint.
 *
 * Since indexes are not yet supported by Yawn, this is just a String.
 */
@JvmInline
value class YawnQueryHint<SOURCE : Any>(
    val hint: String,
)
