package com.faire.yawn.generators.adapters

import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnParameter
import com.google.devtools.ksp.symbol.KSType

internal class TmpValueAdapterGenerator : ValueAdapterGenerator {
    override fun qualifies(yawnContext: YawnContext, fieldType: KSType): Boolean {
        return true
    }

    override fun generate(yawnContext: YawnContext, fieldType: KSType): YawnParameter {
        return YawnParameter("adapter = { it?.tmp }")
    }
}
