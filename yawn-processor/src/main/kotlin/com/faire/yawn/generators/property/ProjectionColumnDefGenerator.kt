package com.faire.yawn.generators.property

import com.faire.yawn.project.YawnProjectionDef
import com.faire.yawn.util.ForeignKeyReference
import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnParameter
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * This generates a meta-property for projections using the
 * [com.faire.yawn.project.YawnProjectionDef.ProjectionColumnDef] class.
 *
 * For example, for a given field:
 *
 * ```
 *       val string: String,
 * ```
 *
 * on a projection class:
 *
 * ```
 *   @YawnProjection
 *   data class SimpleProjection(
 *       val field: Type,
 *       // ...
 *   )
 * ```
 *
 * It will generate:
 *
 * ```
 *   val field: YawnProjectionDef<SOURCE, ProjectionClass>.ProjectionColumnDef<Type> = ProjectionColumnDef("field")
 * ```
 */
internal object ProjectionColumnDefGenerator : YawnPropertyGenerator() {
  override val generatedType = YawnProjectionDef.ProjectionColumnDef::class

  override fun generate(
      yawnContext: YawnContext,
      fieldName: String,
      fieldType: KSType,
      foreignKeyRef: ForeignKeyReference?, // always ignored
  ): PropertySpec {
    val parameters = listOf(
        YawnParameter.string(fieldName), // in the example, "field"
    )
    val typeArguments = listOf(
        fieldType.toTypeName(), // in the example, `Type`
    )
    return generatePropertySpec(
        yawnContext,
        fieldName,
        parameters,
        typeArguments,
    )
  }
}
