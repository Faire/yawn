package com.faire.yawn

import javax.persistence.Column
import javax.persistence.Id

/**
 * A value class whose backing property is not accessible from generated code, just like `kotlin.ULong.data`.
 */
@JvmInline
internal value class PrivateBackedId(private val raw: Long) {
    override fun toString(): String = raw.toString()
}

/**
 * A value class wrapping another value class; a single level of unwrapping would hand Hibernate a boxed [ULong].
 */
@JvmInline
internal value class WrappedULong(val value: ULong)

/**
 * Test entity for "opaque" value classes, i.e. ones that generated property access cannot unwrap:
 * the unsigned types (whose backing property is `internal`), value classes with a private backing property,
 * and nested value classes, both in their null and non-null counterparts.
 */
@YawnEntity
internal class EntityWithOpaqueValueClasses {
    @Id
    var id: Long = 0
        protected set

    @Column
    var isbn: ULong = 0u

    @Column
    var count: UInt? = null

    @Column
    var privateBacked: PrivateBackedId? = null

    @Column
    var wrapped: WrappedULong = WrappedULong(0u)
}
