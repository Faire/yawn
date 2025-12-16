package com.faire.yawn.util

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

internal object ReflectionUtils {
    /**
     * Attempts to unwrap the underlying value of a Kotlin value class using reflection.
     * If the underlying class cannot be construed as a value class, just returns the value itself.
     * Should only be called for value / inline classes in Kotlin.
     */
    fun tryUnwrapValueClass(instance: Any): Any {
        val kClass = instance::class
        val property = valueClassPropertyCache.getOrPut(kClass) {
            extractPropertyFromValueClass(kClass)
        }
        return property?.get(instance) ?: instance
    }

    // NOTE: This is a "permanent" cache - the class structure cannot change at runtime
    private val valueClassPropertyCache = mutableMapOf<KClass<*>, KProperty1<Any, *>?>()

    private fun extractPropertyFromValueClass(kClass: KClass<*>): KProperty1<Any, *>? {
        if (!kClass.isValue) {
            return null
        }

        val constructor = kClass.primaryConstructor ?: return null
        // use the first parameter of the primary constructor
        val parameter = constructor.parameters.firstOrNull() ?: return null

        @Suppress("UNCHECKED_CAST") // safe because we are getting the property from the same class
        val property = kClass.memberProperties
            .find { it.name == parameter.name }
            as? KProperty1<Any, *>
            ?: return null

        property.isAccessible = true
        return property
    }
}
