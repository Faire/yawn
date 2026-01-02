package com.faire.yawn.adapter

/**
 * An optional adapter to be used when querying with the type of this column.
 * This allows Yawn to be smarter about the type-system than the underlying Hibernate is.
 *
 * For example, if you have a value class wrapping a primitive, the generate metamodel will automatically un-wrap it
 * so it works with Hibernate.
 */
fun interface YawnValueAdapter<T> {
    fun adapt(value: T): Any?
}
