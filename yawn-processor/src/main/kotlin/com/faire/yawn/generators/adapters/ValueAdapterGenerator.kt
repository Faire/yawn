package com.faire.yawn.generators.adapters

import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnParameter
import com.google.devtools.ksp.symbol.KSType

internal interface ValueAdapterGenerator {
    fun qualifies(yawnContext: YawnContext, fieldType: KSType): Boolean
    fun generate(yawnContext: YawnContext, fieldType: KSType): YawnParameter
}
