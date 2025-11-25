package com.faire.yawn.generators.properties

import com.faire.yawn.YawnTableDef
import com.faire.yawn.generators.makeNonNullable
import com.faire.yawn.processors.BaseYawnProcessor.Companion.PARENT_PARAMETER_NAME
import com.faire.yawn.util.ForeignKeyReference
import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnParameter
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * This generates a meta-property using the "raw" [com.faire.yawn.YawnTableDef.JoinColumnDef] class.
 * Note that this class is ONLY used when a matching foreign key can not be found on the other side; this either
 * means a Hibernate issue or a Yawn issue. As examples, we have entities without `@Id` columns, which would not
 * be allowed by Hibernate.
 *
 * For example, for a given:
 *
 * ```
 *   @OneToOne(fetch = FetchType.LAZY)
 *   @JoinColumn
 *   var nullableOneToOneYawn: YawnEntityInAnotherPackage? = null
 * ```
 *
 * It will generate (note: I added the param names for legibility, they are not actually included):
 *
 * ```
 *   val nullableOneToOneYawn: YawnTableDef<SOURCE, EntityWithSimpleRelations>.JoinColumnDef<
 *     T = YawnEntityInAnotherPackage,
 *     DEF = YawnEntityInAnotherPackageTableDef<SOURCE>?,
 *   > = JoinColumnDef(
 *     parentPath = path,
 *     name = "nullableOneToOneYawn",
 *     tableDefProvider = { YawnEntityInAnotherPackageTableDef(it) }
 *   )
 * ```
 *
 * Note that this only happens because `YawnEntityInAnotherPackage` does not have an `@Id`, which would not be allowed
 * by Hibernate.
 */
internal object JoinColumnDefGenerator : YawnPropertyGenerator() {
    override val generatedType = YawnTableDef.JoinColumnDef::class

    override fun generate(
        yawnContext: YawnContext,
        fieldName: String, // in this example, `nullableOneToOneYawn`
        fieldType: KSType, // in this example, `YawnEntityInAnotherPackage?`
        foreignKeyRef: ForeignKeyReference?,
    ): PropertySpec {
        check(foreignKeyRef == null)

        val fieldTypeClassName = fieldType
            .toClassName() // reference to YawnEntityInAnotherPackage
            // TODO(yawn): we should make JoinColumnDefGenerator null-aware;
            //             check https://github.com/faire/yawn/blob/main/docs/nullability.md for more details.
            .makeNonNullable()

        // reference to YawnEntityInAnotherPackageTableDef
        val fieldYawnTableDef = tableDefForType(fieldTypeClassName)

        // These are the 2 type arguments that JoinColumnDef takes:
        val typeArguments = listOf(
            // T = YawnEntityInAnotherPackage
            fieldTypeClassName,
            // DEF = YawnEntityInAnotherPackageTableDef<SOURCE>
            fieldYawnTableDef.parameterizedBy(yawnContext.sourceTypeVariable),
        )

        val parameters = listOf(
            // tableDefParent = parent
            YawnParameter.literal(PARENT_PARAMETER_NAME),
            // name = "nullableOneToOneYawn"
            YawnParameter.string(fieldName),
            // tableDefProvider = { YawnEntityInAnotherPackageTableDef(it) }
            YawnParameter.simple("{ %T(it) }", fieldYawnTableDef),
        )

        return generatePropertySpec(yawnContext, fieldName, parameters, typeArguments)
    }
}
