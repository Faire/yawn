package com.faire.yawn.generators.`object`

import com.faire.yawn.util.YawnContext
import com.squareup.kotlinpoet.TypeSpec

/**
 * These type generators are used to generate the code of the singleton objects inside Yawn definitions.
 * These reference objects are accessed by the user to perform queries.
 */
internal interface YawnReferenceObjectGenerator {
    fun generate(yawnContext: YawnContext): TypeSpec
}
