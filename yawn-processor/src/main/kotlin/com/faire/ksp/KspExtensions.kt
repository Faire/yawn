package com.faire.ksp

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toClassName

internal inline fun <reified T : Annotation> KSAnnotation.isExactlyType(): Boolean {
    return annotationType.resolve().toClassName().toString() == T::class.qualifiedName
}

internal inline fun <reified T : Annotation> KSAnnotated.isAnnotationPresent(): Boolean {
    return annotations.any { it.isExactlyType<T>() }
}

internal inline fun <reified T : Annotation> KSAnnotated.getAnnotationsByType(): Sequence<KSAnnotation> {
    return annotations.filter { it.isExactlyType<T>() }
}

internal fun KSClassDeclaration.getVisibilityModifier(): KModifier {
    val visibilityModifier = modifiers.firstOrNull {
        it in setOf(Modifier.PUBLIC, Modifier.PRIVATE, Modifier.INTERNAL, Modifier.PROTECTED)
    } ?: Modifier.PUBLIC // Default to public if no explicit visibility modifier

    return when (visibilityModifier) {
        Modifier.PUBLIC -> KModifier.PUBLIC
        Modifier.PRIVATE -> KModifier.PRIVATE
        Modifier.INTERNAL -> KModifier.INTERNAL
        Modifier.PROTECTED -> KModifier.PROTECTED
        else -> error("Unknown visibility modifier: $visibilityModifier for $this")
    }
}

internal fun KSClassDeclaration.getEffectiveVisibility(): KModifier {
    return getClassDeclarationNestedChain()
        .map { it.getVisibilityModifier() }
        .minBy { modifier ->
            @Suppress("ElseCaseInsteadOfExhaustiveWhen") // Too many non-visibility modifiers to enumerate
            when (modifier) {
                KModifier.PUBLIC -> 3
                KModifier.PROTECTED -> 2
                KModifier.INTERNAL -> 1
                KModifier.PRIVATE -> 0
                else -> error("Unknown visibility $modifier")
            }
        }
}

private fun KSClassDeclaration.getClassDeclarationNestedChain(): Sequence<KSClassDeclaration> {
    return generateSequence(this) { it.parentDeclaration as? KSClassDeclaration }
}

/**
 * Extension function to get a unique simple name for each class under the same package.
 * This is useful when processing multiple inner classes with the same name.
 *
 * @return A standardized unique simple name combined by joining the simple names of the nested class chain,
 *  always using `_` as a separator.
 */
internal fun ClassName.getUniqueSimpleName(): String {
    return simpleNames.joinToString("_")
}
