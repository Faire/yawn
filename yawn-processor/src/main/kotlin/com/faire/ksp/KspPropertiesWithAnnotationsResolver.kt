package com.faire.ksp

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

/**
 * Resolves all properties from a [KSClassDeclaration] by walking the full inheritance chain
 * using [KSClassDeclaration.getDeclaredProperties] at each level, then merging annotations
 * from multiple sources when the same property name appears at more than one level.
 *
 * This is entirely to bypass [this bug](https://github.com/google/ksp/issues/2833) with the
 * [KSClassDeclaration.getAllProperties] API, which was introduced in KSP2 and is impacting Yawn.
 * We can remove this once that is fixed.
 */
internal class KspPropertiesWithAnnotationsResolver(
    private val root: KSClassDeclaration,
) {
    fun resolve(): Sequence<KSPropertyDeclaration> {
        val propertiesByName = mutableMapOf<String, MutableList<KSPropertyDeclaration>>()

        val visited = mutableSetOf<String>()
        val queue = ArrayDeque<KSClassDeclaration>()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val qualifiedName = current.qualifiedName?.asString() ?: continue
            if (!visited.add(qualifiedName)) {
                continue
            }

            for (property in current.getDeclaredProperties()) {
                val name = property.simpleName.asString()
                propertiesByName.getOrPut(name) { mutableListOf() }.add(property)
            }

            for (superTypeRef in current.superTypes) {
                val parent = superTypeRef.resolve().declaration as? KSClassDeclaration ?: continue
                queue.add(parent)
            }
        }

        return propertiesByName.values.asSequence().map { declarations ->
            declarations.singleOrNull() ?: MergedKSPropertyDeclaration(declarations)
        }
    }
}

/**
 * A [KSPropertyDeclaration] wrapper that merges annotations from multiple declarations
 * of the same property across the inheritance chain.
 *
 * Delegates everything to the most-derived (first) declaration, but returns the union
 * of all annotations from every declaration in the chain, deduplicated by fully-qualified
 * annotation type name (keeping the most-derived one when duplicates exist).
 */
private class MergedKSPropertyDeclaration(
    private val declarations: List<KSPropertyDeclaration>,
) : KSPropertyDeclaration by declarations.first() {

    override val annotations: Sequence<KSAnnotation>
        get() {
            val seen = mutableSetOf<String>()
            return declarations.asSequence()
                .flatMap { it.annotations }
                .filter { annotation ->
                    val type = annotation.annotationType.resolve()
                    val name = type.declaration.qualifiedName?.asString()
                    name == null || seen.add(name)
                }
        }
}
