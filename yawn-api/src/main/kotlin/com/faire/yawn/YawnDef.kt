package com.faire.yawn

import com.faire.yawn.project.ProjectionNode
import com.faire.yawn.project.YawnPathProvider
import com.faire.yawn.project.YawnValueProjector
import com.faire.yawn.query.YawnCompilationContext

/**
 * A Yawn definition that can be queried, i.e. either a [YawnTableDef] or a [com.faire.yawn.project.YawnProjectionDef].
 * It will contain a collection of columns that allows for Yawn queries to be type safe.
 *
 * @param SOURCE the type of the original table that the criteria is based off of.
 * @param D the type of the entity or projection.
 */
abstract class YawnDef<SOURCE : Any, D : Any> {
    /**
     * Base class for all Yawn Column-like definitions.
     * This can be either a column from a table or a projection.
     *
     * Implements [YawnValueProjector] so that columns can be used directly as projections
     * in both the v2 [ProjectionNode] tree and as arguments to [com.faire.yawn.project.YawnProjections] methods.
     *
     * @param F the type of the column.
     */
    abstract inner class YawnColumnDef<F> : YawnValueProjector<SOURCE, F>, YawnPathProvider<SOURCE> {
        open fun adaptValue(value: F): Any? {
            return value
        }

        override fun projection(): ProjectionNode.Value<SOURCE, F> = ProjectionNode.property(this)

        /**
         * Backs the [raw] extension, which is where the [YawnStringifiable] bound is applied.
         */
        internal fun rawView(): RawStringColumnDef = RawStringColumnDef(this)
    }

    /**
     * A text view of a column, see [raw].
     *
     * This is a `String` column only for the purpose of pattern matching: the underlying column may well be mapped as
     * something else, so reading it back as a `String` would be wrong, and projecting it is rejected.
     */
    inner class RawStringColumnDef(
        private val delegate: YawnColumnDef<*>,
    ) : YawnColumnDef<String>() {
        override fun generatePath(context: YawnCompilationContext): String = delegate.generatePath(context)

        override fun projection(): ProjectionNode.Value<SOURCE, String> {
            throw UnsupportedOperationException(
                """
                    A text view of $delegate cannot be projected, only pattern-matched.
                    Project the column itself instead, and it will be read back as its own type.
                """.trimIndent(),
            )
        }

        override fun toString(): String = "$delegate (as text)"
    }
}

/**
 * A text view of this column, for pattern matching against a `String` pattern rather than a value of the column's own
 * type.
 *
 * Only available on a [YawnStringifiable] column, so a column that does not hold text cannot be matched as text:
 *
 * ```
 * addLike(people.email.raw, "@example.com", MatchMode.END)
 * ```
 *
 * The pattern is bound as a `String` against the underlying column, so `MatchMode` and the case-insensitive variants
 * work even when Hibernate maps the property through an `AttributeConverter`. Reach for this when the wildcards cannot
 * be part of the value itself, either because the type validates its own format or because the pattern is partial.
 *
 * The column has to map to a single column, and this view cannot be projected.
 */
val <SOURCE : Any, F : YawnStringifiable?> YawnDef<SOURCE, *>.YawnColumnDef<F>.raw: YawnDef<SOURCE, *>.RawStringColumnDef
    get() = rawView()
