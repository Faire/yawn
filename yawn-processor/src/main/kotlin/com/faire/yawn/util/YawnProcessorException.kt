package com.faire.yawn.util

import com.google.devtools.ksp.symbol.KSNode

class YawnProcessorException(
    val ksNode: KSNode,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
