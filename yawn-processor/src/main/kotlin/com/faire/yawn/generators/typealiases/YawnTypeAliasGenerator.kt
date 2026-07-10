package com.faire.yawn.generators.typealiases

import com.faire.yawn.util.YawnContext
import com.squareup.kotlinpoet.TypeAliasSpec

/**
 * These typealias generators are used to generate typealiases for generated content.
 *
 * For example,
 * ```
 * typealias DbBookEntityQueryScope = EntityYawnQueryScope<DbBook, DbBookTableDefType>
 * ```
 */
internal interface YawnTypeAliasGenerator {
    fun generate(yawnContext: YawnContext): TypeAliasSpec
}
