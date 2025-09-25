package com.faire.yawn.generators

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import kotlin.reflect.KClass

internal fun TypeSpec.Builder.addGeneratedAnnotation(generator: KClass<*>): TypeSpec.Builder {
    val generatedAnnotationName = ClassName("javax.annotation.processing", "Generated")

    val annotationSpec = AnnotationSpec.builder(generatedAnnotationName)
        // Do **not** specify `dateTime` property. This causes build cache misses as the time changes.
        .addMember("%S", generator.qualifiedName!!)
        .build()

    return addAnnotation(annotationSpec)
}

internal fun ClassName.makeNonNullable(): ClassName {
    // NOTE: due to a lack of default parameter values on the ClassName override for copy,
    //       we need to specify all args to avoid falling back to the TypeName super version,
    //       which returns TypeName
    return copy(
        nullable = false,
        annotations = annotations,
        tags = tags,
    )
}
