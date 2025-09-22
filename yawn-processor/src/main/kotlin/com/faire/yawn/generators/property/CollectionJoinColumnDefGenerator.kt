package com.faire.yawn.generators.property

import com.faire.yawn.YawnTableDef
import com.faire.yawn.processors.BaseYawnProcessor.Companion.PARENT_PARAMETER_NAME
import com.faire.yawn.util.ForeignKeyReference
import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnParameter
import com.faire.yawn.util.isYawnEntity
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * This generates a meta-property using the [com.faire.yawn.YawnTableDef.CollectionJoinColumnDef] class.
 *
 * For example, for a given property in the source class like this:
 *
 * ```
 *   @OneToMany(fetch = FetchType.LAZY)
 *   @JoinColumn(name = "name", referencedColumnName = "reference")
 *   var oneToManyYawn: List<YawnEntityInAnotherPackage> = listOf()
 * ```
 *
 * It will generate (note: I added the param names for legibility, they are not actually included):
 *
 * ```
 *   val oneToManyYawn: YawnTableDef<SOURCE, EntityWithXtoManyRelations>.CollectionJoinColumnDef<
 *      T = YawnEntityInAnotherPackage,
 *      DEF = YawnEntityInAnotherPackageTableDef<SOURCE>,
 *   > = CollectionJoinColumnDef(
 *      parentPath = path,
 *      name = "oneToManyYawn",
 *      tableDefProvider = { YawnEntityInAnotherPackageTableDef(it) },
 *   )
 * ```
 */
internal object CollectionJoinColumnDefGenerator : YawnPropertyGenerator() {
  override val generatedType = YawnTableDef.CollectionJoinColumnDef::class

  override fun generate(
      yawnContext: YawnContext,
      fieldName: String, // in this example, `oneToManyYawn`
      fieldType: KSType, // in this example, `List<YawnEntityInAnotherPackage>`
      foreignKeyRef: ForeignKeyReference?, // null
  ): PropertySpec? {
    check(foreignKeyRef == null)

    val genericType = fieldType.arguments.first().type!!.resolve() // reference to YawnEntityInAnotherPackage
    if (!genericType.declaration.isYawnEntity()) {
      return null
    }

    // reference to YawnEntityInAnotherPackageTableDef
    val fieldYawnTableDef = tableDefForType(genericType.toClassName())

    // These are the 2 type arguments that CollectionJoinColumnDef takes:
    val typeArguments = listOf(
        // T = YawnEntityInAnotherPackage
        genericType.toTypeName(),
        // DEF = YawnEntityInAnotherPackageTableDef<SOURCE>
        fieldYawnTableDef.parameterizedBy(yawnContext.sourceTypeVariable),
    )

    val parameters = listOf(
        // tableDefParent = parent
        YawnParameter.literal(PARENT_PARAMETER_NAME),
        // name = "oneToManyYawn"
        YawnParameter.string(fieldName),
        // tableDefProvider = { YawnEntityInAnotherPackageTableDef(it) }
        YawnParameter.simple("{ %T(it) }", fieldYawnTableDef),
    )

    return generatePropertySpec(yawnContext, fieldName, parameters, typeArguments)
  }
}
