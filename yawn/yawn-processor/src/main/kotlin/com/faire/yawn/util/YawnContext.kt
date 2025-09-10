package com.faire.yawn.util

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeVariableName

/**
 * Represents the "global" context of a class being processed by Yawn.
 * This is agnostic of the current property being handled.
 */
internal data class YawnContext(
    val classDeclaration: KSClassDeclaration,
    val superClassName: ParameterizedTypeName,
    val sourceTypeVariable: TypeVariableName,
    val newClassName: ClassName,
)
