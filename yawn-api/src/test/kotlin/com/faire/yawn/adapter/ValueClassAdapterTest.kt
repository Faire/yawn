package com.faire.yawn.adapter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ValueClassAdapterTest {
    private fun adapt(value: Any?): Any? = ValueClassAdapter.adapt(value)

    @Test
    fun `unwraps unsigned types to their signed JVM primitive`() {
        assertThat(adapt(42uL)).isEqualTo(42L)
        assertThat(adapt(7u)).isEqualTo(7)
        assertThat(adapt(3.toUShort())).isEqualTo(3.toShort())
        assertThat(adapt(9.toUByte())).isEqualTo(9.toByte())
    }

    @Test
    fun `unsigned values above the signed range keep the same bits`() {
        // this matches what Hibernate stores, since the entity field itself is erased to a signed primitive
        assertThat(adapt(ULong.MAX_VALUE)).isEqualTo(-1L)
        assertThat(adapt(UInt.MAX_VALUE)).isEqualTo(-1)
    }

    @Test
    fun `unwraps a value class over a reference type`() {
        assertThat(adapt(StringBacked("hello"))).isEqualTo("hello")
    }

    @Test
    fun `unwraps a value class whose backing property is private`() {
        assertThat(adapt(PrivateBacked("secret"))).isEqualTo("secret")
    }

    @Test
    fun `unwraps a value class whose backing property is internal`() {
        assertThat(adapt(InternalBacked(99L))).isEqualTo(99L)
    }

    @Test
    fun `unwraps a value class over an unsigned type all the way down`() {
        assertThat(adapt(OverULong(ULong.MAX_VALUE))).isEqualTo(-1L)
    }

    @Test
    fun `unwraps nested value classes to the innermost representation`() {
        assertThat(adapt(Nested(Inner("deep")))).isEqualTo("deep")
    }

    @Test
    fun `unwraps a nullable inner value class over a primitive`() {
        assertThat(adapt(NullableInnerPrimitive(IntBox(5)))).isEqualTo(5)
        assertThat(adapt(NullableInnerPrimitive(null))).isNull()
    }

    @Test
    fun `passes through null`() {
        assertThat(adapt(null)).isNull()
    }

    @Test
    fun `passes through values that are not value classes`() {
        assertThat(adapt("plain")).isEqualTo("plain")
        assertThat(adapt(5L)).isEqualTo(5L)
        val notAValueClass = NotAValueClass("x")
        assertThat(adapt(notAValueClass)).isSameAs(notAValueClass)
    }
}

@JvmInline
private value class StringBacked(val value: String)

@JvmInline
private value class PrivateBacked(private val raw: String) {
    override fun toString(): String = raw
}

@JvmInline
internal value class InternalBacked(internal val raw: Long)

@JvmInline
private value class OverULong(val value: ULong)

@JvmInline
private value class Inner(val s: String)

@JvmInline
private value class Nested(val inner: Inner)

@JvmInline
private value class IntBox(val i: Int)

@JvmInline
private value class NullableInnerPrimitive(val inner: IntBox?)

private class NotAValueClass(val value: String)
