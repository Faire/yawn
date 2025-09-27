package com.faire.yawn.generators.properties

import com.faire.yawn.YawnTableDef
import com.faire.yawn.generators.makeNonNullable
import com.faire.yawn.processors.BaseYawnProcessor.Companion.PARENT_PARAMETER_NAME
import com.faire.yawn.util.ForeignKeyReference
import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnNamesGenerator.generateEmbeddedDefClassName
import com.faire.yawn.util.YawnParameter
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * This generates a meta-property using the [com.faire.yawn.YawnTableDef.JoinColumnDefWithCompositeKey] class.
 *
 * For example, for a given:
 *
 * ```
 *   @ManyToOne(fetch = FetchType.LAZY)
 *   @JoinColumn(name = "parent_foo_id")
 *   @JoinColumn(name = "foo_composite_id")
 *   lateinit var fooComposite: DbFooComposite
 * ```
 *
 * It will generate (note: I added the param names for legibility, they are not actually included):
 *
 * ```
 *   val fooComposite: YawnTableDef<SOURCE, DbFooCompositeRef>.JoinColumnDefWithCompositeKey<
 *     T = DbFooComposite,
 *     DEF = FooCompositeTableDef<SOURCE>,
 *     CID = FooCompositeTableDef<SOURCE>.FooCompositeIdDef,
 *   > = JoinColumnDefWithCompositeKey(
 *     parentPath = path,
 *     name = "fooComposite",
 *     foreignKeyProvider = { FooCompositeTableDef<SOURCE>().FooCompositeIdDef(it) },
 *     tableDefProvider = { FooCompositeTableDef(it) },
 *   )
 * ```
 */
internal object JoinColumnDefWithCompositeKeyGenerator : YawnPropertyGenerator() {
    override val generatedType = YawnTableDef.JoinColumnDefWithCompositeKey::class

    override fun generate(
        yawnContext: YawnContext,
        fieldName: String, // in this example, `fooComposite`
        fieldType: KSType, // in this example,  `DbFooComposite`
        foreignKeyRef: ForeignKeyReference?, // pre-parsed info from the FK matching on the other side
    ): PropertySpec {
        checkNotNull(foreignKeyRef)

        val fieldTypeClassName = fieldType
            .toClassName() // reference to DbFooComposite
            // TODO(yawn): we should make JoinColumnDefWithCompositeKey null-aware;
            //             check faire.link/yawn-nullability for more details.
            .makeNonNullable()

        // reference to FooCompositeTableDef
        val fieldYawnTableDef = tableDefForType(fieldTypeClassName)

        // reference to FooCompositeIdDef
        val compositeIdRef = generateEmbeddedDefClassName(foreignKeyRef.toClassName())

        // reference to FooCompositeTableDef<SOURCE>
        val tableDefWithSource = fieldYawnTableDef.parameterizedBy(yawnContext.sourceTypeVariable)

        // These are the 3 type arguments that JoinColumnDefWithCompositeKey takes:
        val typeArguments = listOf(
            // T = DbFooComposite
            fieldTypeClassName,
            // DEF = FooCompositeTableDef<SOURCE>
            tableDefWithSource,
            // CID = FooCompositeTableDef<SOURCE>.FooCompositeIdDef
            tableDefWithSource.nestedClass(compositeIdRef, typeArguments = listOf()),
        )

        // These are the parameters to the JoinColumnDefWithCompositeKey constructor:
        val parameters = listOf(
            // tableDefParent = parent
            YawnParameter.literal(PARENT_PARAMETER_NAME),
            // name = "fooComposite"
            YawnParameter.string(fieldName),
            // foreignKeyProvider = { FooCompositeTableDef<SOURCE>(parent).FooCompositeIdDef(it) }
            YawnParameter("{ %T(parent).%N(it) }", listOf(tableDefWithSource, compositeIdRef)),
            // tableDefProvider = { FooCompositeTableDef(it) }
            YawnParameter.simple("{ %T(it) }", fieldYawnTableDef),
        )

        return generatePropertySpec(yawnContext, fieldName, parameters, typeArguments)
    }
}
