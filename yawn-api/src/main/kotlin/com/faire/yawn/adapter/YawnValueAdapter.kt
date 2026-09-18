package com.faire.yawn.adapter

/**
 * An optional adapter to be used when querying with the type of this column.
 * This allows Yawn to be smarter about the type-system than the underlying Hibernate is.
 *
 * For example, if you have a value class wrapping a primitive, the generated metamodel will automatically un-wrap it
 * so it works with Hibernate (see [ValueClassAdapter]).
 *
 * [T] is contravariant so that a single general adapter (e.g. a `YawnValueAdapter<Any?>`) can be used for any column.
 */
fun interface YawnValueAdapter<in T> {
    fun adapt(value: T): Any?

    companion object {
        /**
         * Passes the value through untouched; the default for columns that need no adaptation.
         */
        val IDENTITY: YawnValueAdapter<Any?> = YawnValueAdapter { it }
    }
}
