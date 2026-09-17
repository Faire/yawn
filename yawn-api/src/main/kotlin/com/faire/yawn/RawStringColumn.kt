package com.faire.yawn

import com.faire.yawn.project.YawnPathProvider
import com.faire.yawn.query.YawnCompilationContext

/**
 * A text view of a column, for pattern matching against a `String` pattern rather than a value of the column's own
 * type.
 *
 * Deliberately not a [YawnDef.YawnColumnDef]: a pattern is not a value of the column's type, and reading the column
 * back as a `String` would be wrong for any custom type. Only the pattern-matching restrictions accept this, so it
 * cannot be projected or compared.
 *
 * Obtained from [YawnDef.YawnColumnDef.raw].
 */
class RawStringColumn<SOURCE : Any> internal constructor(
    private val delegate: YawnPathProvider<SOURCE>,
) : YawnPathProvider<SOURCE> {
    override fun generatePath(context: YawnCompilationContext): String = delegate.generatePath(context)

    override fun toString(): String = "$delegate (as text)"
}
