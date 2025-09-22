package com.faire.yawn.generators.type

import com.faire.yawn.util.YawnContext
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.TypeSpec

/**
 * These type generators are used to generate the code of a subclass inside the generated outer
 * [com.faire.yawn.YawnTableDef].
 *
 * For example, for fields tagged with `@Embedded` or `@EmbeddedId`, we need to generate a wrapper subclass that will
 * contain the properties of the embedded class.
 */
internal interface YawnEmbeddableTypeGenerator {
  fun generate(
      yawnContext: YawnContext,
      propertyDeclaration: KSPropertyDeclaration,
  ): TypeSpec?
}
