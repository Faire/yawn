package com.faire.ksp

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import kotlin.reflect.KClass

/**
 * Extension functions for KSP symbols to provide compatibility with Faire's internal KSP utilities.
 * These functions replicate the functionality that was previously available in Faire's build system.
 */

/**
 * Gets the effective visibility modifier for a KS declaration.
 */
fun KSDeclaration.getEffectiveVisibility(): KModifier {
    return when {
        modifiers.contains(Modifier.PUBLIC) -> KModifier.PUBLIC
        modifiers.contains(Modifier.PRIVATE) -> KModifier.PRIVATE
        modifiers.contains(Modifier.PROTECTED) -> KModifier.PROTECTED
        modifiers.contains(Modifier.INTERNAL) -> KModifier.INTERNAL
        else -> {
            // For classes without explicit visibility, check if they're in a test context
            // or nested in an internal class, and make them internal to avoid exposure issues
            val isInTestContext = qualifiedName?.asString()?.contains("Test") == true
            val parentDeclaration = parentDeclaration
            val isNestedInInternal = parentDeclaration?.modifiers?.contains(Modifier.INTERNAL) == true
            
            if (isInTestContext || isNestedInInternal) {
                KModifier.INTERNAL
            } else {
                KModifier.PUBLIC // Default Kotlin visibility
            }
        }
    }
}

/**
 * Checks if a KS declaration has a specific annotation.
 */
inline fun <reified T : Annotation> KSDeclaration.isAnnotationPresent(): Boolean {
    return isAnnotationPresent(T::class)
}

/**
 * Checks if a KS declaration has a specific annotation by class.
 */
fun KSDeclaration.isAnnotationPresent(annotationClass: KClass<out Annotation>): Boolean {
    val annotationName = annotationClass.qualifiedName ?: return false
    return annotations.any { annotation ->
        annotation.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName
    }
}

/**
 * Gets annotations of a specific type from a KS declaration.
 */
inline fun <reified T : Annotation> KSDeclaration.getAnnotationsByType(): Sequence<KSAnnotation> {
    return getAnnotationsByType(T::class)
}

/**
 * Gets annotations of a specific type from a KS declaration by class.
 */
fun KSDeclaration.getAnnotationsByType(annotationClass: KClass<out Annotation>): Sequence<KSAnnotation> {
    val annotationName = annotationClass.qualifiedName ?: return emptySequence()
    return annotations.filter { annotation ->
        annotation.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName
    }
}

/**
 * Gets a unique simple name for a KS class declaration, handling potential conflicts.
 */
fun KSClassDeclaration.getUniqueSimpleName(): String {
    // For now, just return the simple name. In a more complex scenario,
    // this might need to handle name conflicts by adding suffixes.
    return simpleName.asString()
}

/**
 * Gets a unique simple name for a KotlinPoet ClassName, handling potential conflicts.
 * For nested classes, this creates a flattened name using underscores.
 * For example: YawnProjectionTest.SimpleBook -> YawnProjectionTest_SimpleBook
 */
fun ClassName.getUniqueSimpleName(): String {
    // If this is a nested class, flatten the names with underscores
    return if (enclosingClassName() != null) {
        val enclosingName = enclosingClassName()!!.getUniqueSimpleName()
        "${enclosingName}_${simpleName}"
    } else {
        simpleName
    }
}
