package com.faire.yawn.util

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode

internal class YawnLogger(private val kspLogger: KSPLogger) {
    fun error(at: KSNode?, yawnProcessorException: YawnProcessorException) {
        val message = """
            ${yawnProcessorException.message}
            ${yawnProcessorException.stackTraceToString()}
        """.trimIndent()

        kspLogger.error("\n$message", at)
    }
}
