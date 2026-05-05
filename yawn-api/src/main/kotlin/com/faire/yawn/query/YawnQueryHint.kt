package com.faire.yawn.query

/**
 * Part of an [YawnQuery] representing a query hint.
 *
 * Since indexes are not yet supported by Yawn in a type-safe fashion, this is just a String,
 * tied to an originating [SOURCE].
 */
@JvmInline
value class YawnQueryHint<SOURCE : Any>(
    val hint: String,
)
