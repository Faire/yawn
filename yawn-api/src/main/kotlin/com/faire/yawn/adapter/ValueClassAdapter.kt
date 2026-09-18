package com.faire.yawn.adapter

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

/**
 * A [YawnValueAdapter] that unwraps any Kotlin `@JvmInline value class` into the JVM value Hibernate actually stores.
 *
 * The generated metamodel attaches this adapter to every column whose type is a value class, so that query parameters
 * (e.g. `addEq(books.isbn, 42uL)`) are bound as the underlying primitive/reference rather than as the boxed wrapper.
 * Unwrapping happens at runtime through [UnboxHandles], looked up once per class and cached.
 */
object ValueClassAdapter : YawnValueAdapter<Any?> {
    /**
     * A single unbox yields the class's underlying representation, which is itself a value class when the wrapped type
     * is a nullable value class over a primitive (`value class A(val b: IntBox?)` is represented as a boxed `IntBox`).
     * Recursing until the result is no longer a value class covers that case; for everything else it stops after one step.
     */
    override tailrec fun adapt(value: Any?): Any? {
        if (value == null) return null
        val unboxHandle = UnboxHandles.get(value.javaClass) ?: return value
        return adapt(unboxHandle.invoke(value))
    }

    /**
     * Per-class cache of the `unbox-impl` [MethodHandle]; null means "not a value class".
     * Unlike a map keyed by [Class], entries are collected together with the class.
     */
    private object UnboxHandles : ClassValue<MethodHandle?>() {
        /**
         * The method every Kotlin/JVM value class exposes to convert its boxed form to its underlying representation,
         * defined in the [inline classes ABI](https://github.com/Kotlin/KEEP/blob/master/proposals/inline-classes.md#inline-classes-abi-jvm).
         * That representation is the JVM type of the backing field, so for a value class wrapping a non-null value class
         * it is already the innermost value (see [adapt] for the nullable exception).
         *
         * We call it instead of generating property access because the backing property is not always accessible from
         * generated code (`kotlin.ULong.data` is `internal`, users often declare `private val`).
         */
        private const val UNBOX_METHOD_NAME = "unbox-impl"

        override fun computeValue(type: Class<*>): MethodHandle? {
            if (!type.isAnnotationPresent(JvmInline::class.java)) return null
            val method = try {
                type.getMethod(UNBOX_METHOD_NAME)
            } catch (e: NoSuchMethodException) {
                throw IllegalStateException(
                    """
                        ${type.name} is annotated with @JvmInline but has no `$UNBOX_METHOD_NAME` method,
                        so Yawn cannot unwrap it for Hibernate. This is unexpected for a Kotlin/JVM value class.
                        Please open an issue on GitHub (https://github.com/faire/yawn/issues) with your schema definition.
                    """.trimIndent(),
                    e,
                )
            }
            return MethodHandles.lookup().unreflect(method)
        }
    }
}
