package com.faire.yawn.generators.adapters

import com.faire.yawn.adapter.ValueClassAdapter
import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnParameter
import com.faire.yawn.util.isValueClass
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.asClassName

/**
 * Attaches [ValueClassAdapter] to every column whose type is a Kotlin value class, so that query parameters are
 * unwrapped to the representation Hibernate stores. See [ValueClassAdapter] for why the unwrapping happens at runtime.
 */
internal class ValueClassAdapterGenerator : ValueAdapterGenerator {
    override fun qualifies(
        yawnContext: YawnContext,
        fieldType: KSType,
    ): Boolean {
        return fieldType.isValueClass()
    }

    override fun generate(
        yawnContext: YawnContext,
        fieldType: KSType,
    ): YawnParameter {
        return YawnParameter.simple("adapter = %T", ValueClassAdapter::class.asClassName())
    }
}
