package com.faire.yawn.generators.properties

import com.faire.yawn.util.ForeignKeyReference
import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnNamesGenerator.generateTableDefClassName
import com.faire.yawn.util.YawnParameter
import com.faire.yawn.util.resolveTargetType
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import kotlin.reflect.KClass

/**
 * These properties generators are responsible for, given a property in the source class, generate the corresponding
 * meta-property in the generated table definition class.
 *
 * For example, for the simplest type, a property like:
 *
 * ```
 *   @Column
 *   lateinit var name: String
 * ```
 *
 * It would generate a property:
 *
 * ```
 *  val name: ColumnDef<String> = ColumnDef("name")
 * ```
 *
 * To be used when constructing the meta-definition classes.
 */
internal abstract class YawnPropertyGenerator {
    protected abstract val generatedType: KClass<*>

    private val generatedTypeName
        get() = generatedType.simpleName!!

    fun generate(
        yawnContext: YawnContext,
        property: KSPropertyDeclaration,
        foreignKeyRef: ForeignKeyReference? = null,
    ): PropertySpec? {
        return generate(
            yawnContext = yawnContext,
            fieldName = property.simpleName.asString(),
            fieldType = property.resolveTargetType(),
            foreignKeyRef = foreignKeyRef,
        )
    }

    abstract fun generate(
        yawnContext: YawnContext,
        fieldName: String,
        fieldType: KSType,
        foreignKeyRef: ForeignKeyReference?,
    ): PropertySpec?

    protected fun tableDefForType(type: ClassName): ClassName {
        return ClassName(
            type.packageName,
            generateTableDefClassName(type),
        )
    }

    /**
     * Builds a property following the template:
     *
     * ```
     *    val fieldName: Type<typeArguments...> = Type(parameters...)
     * ```
     *
     * The type is controlled by the `generatedType` property.
     * This makes sure that types are properly imported and correctly referenced.
     */
    protected fun generatePropertySpec(
        yawnContext: YawnContext,
        fieldName: String,
        parameters: List<YawnParameter>,
        typeArguments: List<TypeName> = listOf(),
    ): PropertySpec {
        val fieldType = yawnContext.superClassName.nestedClass(generatedTypeName, typeArguments)

        val parameterFormats = parameters.joinToString(", ") { it.format }
        val parameterValues = parameters.flatMap { it.arguments }

        return PropertySpec.builder(
            fieldName,
            fieldType,
        ).initializer(
            "$generatedTypeName($parameterFormats)",
            *parameterValues.toTypedArray(),
        ).build()
    }
}
