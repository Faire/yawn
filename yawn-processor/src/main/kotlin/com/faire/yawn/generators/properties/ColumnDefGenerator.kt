package com.faire.yawn.generators.properties

import com.faire.yawn.YawnTableDef
import com.faire.yawn.util.ForeignKeyReference
import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnParameter
import com.faire.yawn.util.YawnProcessorException
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * This generates a meta-property using the "raw" [com.faire.yawn.YawnTableDef.ColumnDef] class.
 * This is the simplest possible property definition, which is used for "normal" columns.
 *
 * For example, for a given:
 *
 * ```
 *   @Column
 *   var token: Token<FOO>
 * ```
 *
 * It will generate:
 *
 * ```
 *   val token: YawnTableDef<SOURCE, DbFoo>.ColumnDef<Token<FOO>> = ColumnDef("token")
 * ```
 */
internal object ColumnDefGenerator : YawnPropertyGenerator() {
    override val generatedType = YawnTableDef.ColumnDef::class

    override fun generate(
        yawnContext: YawnContext,
        fieldName: String,
        fieldType: KSType,
        foreignKeyRef: ForeignKeyReference?, // always ignored
    ): PropertySpec {
        return generate(yawnContext, fieldName, fieldType)
    }

    fun generate(
        yawnContext: YawnContext,
        propertyDeclaration: KSPropertyDeclaration,
        // the ColumnDef constructor can take a list of prefixes before the actual field name
        pathPrefixes: List<YawnParameter> = listOf(),
    ): PropertySpec {
        return generate(
            yawnContext = yawnContext,
            fieldName = propertyDeclaration.simpleName.asString(),
            fieldType = propertyDeclaration.type.resolve(),
            pathPrefixes = pathPrefixes,
        )
    }

    private fun generate(
        yawnContext: YawnContext,
        fieldName: String, // in this example, `token`
        fieldType: KSType, // in this example, `Token<FOO>`
        // the ColumnDef constructor can take a list of prefixes before the actual field name
        pathPrefixes: List<YawnParameter> = listOf(),
    ): PropertySpec {
        val typeArguments = try {
            listOf(fieldType.toTypeName()) // Token<FOO>
        } catch (e: IllegalArgumentException) {
            throw YawnProcessorException("Failed to get type name for ${yawnContext.superClassName}.$fieldName", e)
        }
        val parameters = pathPrefixes + listOf(
            YawnParameter.string(fieldName), // "token"
        )

        return generatePropertySpec(
            yawnContext,
            fieldName,
            parameters,
            typeArguments,
        )
    }
}
