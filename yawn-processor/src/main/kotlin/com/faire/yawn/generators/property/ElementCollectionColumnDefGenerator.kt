package com.faire.yawn.generators.property

import com.faire.yawn.YawnTableDef
import com.faire.yawn.processors.BaseYawnProcessor.Companion.PARENT_PARAMETER_NAME
import com.faire.yawn.util.ForeignKeyReference
import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnParameter
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Leverages the [YawnTableDef.ElementCollectionDef] to create a field reference
 * for the element collection property of type [YawnTableDef.JoinColumnDef].
 *
 * For example, given the following column in `DbBook`:
 * ```
 *   @ElementCollection(targetClass = Genre::class)
 *   @CollectionTable(name = "book_genres", joinColumns = [JoinColumn(name = "book_id")])
 *   @Column(name = "genre")
 *   @Enumerated(EnumType.STRING)
 *   var genres = setOf<Genre>()
 * ```
 *
 * It will look something like:
 * ```
 *    val genres: YawnTableDef<SOURCE, DbBook>.JoinColumnDef<
 *      T = Genre,
 *      DEF = ElementCollectionDef<Genre>,
 *    > = JoinColumnDef(
 *      parentPath = path,
 *      name = "genres",
 *      tableDefProvider = { ElementCollectionDef(it) },
 *    )
 * ```
 */
internal object ElementCollectionColumnDefGenerator : YawnPropertyGenerator() {
    override val generatedType = YawnTableDef.JoinColumnDef::class
    private val elementCollectionDefName = YawnTableDef.ElementCollectionDef::class.asClassName().simpleName

    override fun generate(
        yawnContext: YawnContext,
        fieldName: String, // in this example, `genres`
        fieldType: KSType, // in this example, `Set<Genre>`
        foreignKeyRef: ForeignKeyReference?, // always null
    ): PropertySpec {
        check(foreignKeyRef == null)

        // in this example, `Genre`
        val elementType = fieldType.arguments.first().type!!.resolve()
        val elementTypeName = elementType.toTypeName()

        // These are the 2 type arguments that ElementCollectionColumnDef takes:
        val typeArguments = listOf(
            elementTypeName, // T = Genre
            // DEF = ElementCollectionDef<Genre>
            yawnContext.superClassName.nestedClass(
                elementCollectionDefName,
                listOf(elementTypeName),
            ),
        )

        val parameters = listOf(
            // parentPath = path
            YawnParameter.literal(PARENT_PARAMETER_NAME),
            // name = "genre"
            YawnParameter.string(fieldName),
            // tableDefProvider = { ElementCollectionDef(it) }
            YawnParameter("{ ElementCollectionDef(it) }"),
        )

        return generatePropertySpec(yawnContext, fieldName, parameters, typeArguments)
    }
}
