package com.faire.yawn.generators.adapters

import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnParameter
import com.faire.yawn.util.YawnProcessorException
import com.faire.yawn.util.isValueClass
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

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
        val declaration = fieldType.declaration as? KSClassDeclaration
            ?: fail("Expected a class declaration for value class, but found ${fieldType.declaration}")
        val primaryConstructor = declaration.primaryConstructor
            ?: fail("Value class ${declaration.qualifiedName?.asString()} must have a primary constructor")
        val valueClassProperty = primaryConstructor.parameters.singleOrNull()
            ?: fail("Value class ${declaration.qualifiedName?.asString()} must have a single property in its primary constructor")
        val valueClassPropertyName = valueClassProperty.name?.asString()
            ?: fail("Value class property must have a name")
        return YawnParameter("adapter = { it?.%N }", listOf(valueClassPropertyName))
    }
}

private fun fail(message: String): Nothing {
    throw YawnProcessorException(message)
}
