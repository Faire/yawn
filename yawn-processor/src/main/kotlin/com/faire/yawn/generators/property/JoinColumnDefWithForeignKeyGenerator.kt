package com.faire.yawn.generators.property

import com.faire.yawn.YawnTableDef
import com.faire.yawn.generators.makeNonNullable
import com.faire.yawn.processors.BaseYawnProcessor.Companion.PARENT_PARAMETER_NAME
import com.faire.yawn.util.ForeignKeyReference
import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnParameter
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * This generates a meta-property using the [com.faire.yawn.YawnTableDef.JoinColumnDefWithForeignKey] class.
 *
 * For example, for a given:
 *
 * ```
 *   @JoinColumn
 *   @ManyToOne(fetch = FetchType.LAZY)
 *   lateinit var foo: DbFoo
 * ```
 *
 * It will generate (note: I added the param names for legibility, they are not actually included):
 *
 * ```
 *   val foo: YawnTableDef<SOURCE, DbBarFooId>.JoinColumnDefWithForeignKey<
 *     T = DbFoo,
 *     DEF = FooTableDef<SOURCE>,
 *     ID = Id<DbFoo>,
 *   > = JoinColumnDefWithForeignKey(
 *     parentPath = path,
 *     name = "foo",
 *     foreignKeyName = "id",
 *     tableDefProvider = { FooTableDef(it) },
 *   )
 * ```
 */
internal object JoinColumnDefWithForeignKeyGenerator : YawnPropertyGenerator() {
    override val generatedType = YawnTableDef.JoinColumnDefWithForeignKey::class

    override fun generate(
        yawnContext: YawnContext,
        fieldName: String, // in this example, `foo`
        fieldType: KSType, // in this example,  `DbFoo`
        foreignKeyRef: ForeignKeyReference?, // pre-parsed info from the FK matching on the other side
    ): PropertySpec {
        checkNotNull(foreignKeyRef)

        val fieldTypeClassName: ClassName = fieldType
            .toClassName() // reference to DbFoo
            // TODO(yawn): we should make JoinColumnDefWithCompositeKey null-aware;
            //             check faire.link/yawn-nullability for more details.
            .makeNonNullable()

        // reference to FooTableDef
        val fieldYawnTableDef = tableDefForType(fieldTypeClassName)

        // These are the 3 type arguments that JoinColumnDefWithForeignKey takes:
        val typeArguments = listOf(
            // T = DbFoo
            fieldTypeClassName,
            // DEF = FooTableDef<SOURCE>
            fieldYawnTableDef
                .parameterizedBy(yawnContext.sourceTypeVariable),
            // ID = Id<DbFoo>
            foreignKeyRef.toTypeName(),
        )

        val parameters = listOf(
            // tableDefParent = parent
            YawnParameter.literal(PARENT_PARAMETER_NAME),
            // name = "foo"
            YawnParameter.string(fieldName),
            // foreignKeyName = "id"
            YawnParameter.string(foreignKeyRef.columnName),
            // tableDefProvider = { FooTableDef(it) }
            YawnParameter.simple("{ %T(it) }", fieldYawnTableDef),
        )

        return generatePropertySpec(yawnContext, fieldName, parameters, typeArguments)
    }
}
