package com.faire.yawn.util

import com.faire.ksp.getAnnotationsByType
import com.faire.ksp.isAnnotationPresent
import com.faire.yawn.YawnEntity
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import org.hibernate.annotations.Formula
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Embedded
import javax.persistence.EmbeddedId
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Transient

internal fun KSPropertyDeclaration.isTransient(): Boolean {
  return isAnnotationPresent<Transient>()
}

internal fun KSPropertyDeclaration.isEmbeddedId(): Boolean {
  return isAnnotationPresent<EmbeddedId>()
}

internal fun KSPropertyDeclaration.isEmbedded(): Boolean {
  return isAnnotationPresent<Embedded>()
}

internal fun KSPropertyDeclaration.isElementCollection(): Boolean {
  return isAnnotationPresent<ElementCollection>()
}

internal fun KSPropertyDeclaration.isColumn(): Boolean {
  return isAnnotationPresent<Column>()
}

internal fun KSPropertyDeclaration.isId(): Boolean {
  return isAnnotationPresent<Id>()
}

internal fun KSPropertyDeclaration.isFormula(): Boolean {
  return isAnnotationPresent<Formula>()
}

internal fun KSPropertyDeclaration.isOneToOneJoin(): Boolean {
  return isAnnotationPresent<OneToOne>()
}

internal fun KSPropertyDeclaration.isManyToOneJoin(): Boolean {
  return isAnnotationPresent<ManyToOne>()
}

internal fun KSPropertyDeclaration.isManyToManyJoin(): Boolean {
  return isAnnotationPresent<ManyToMany>()
}

internal fun KSPropertyDeclaration.isOneToManyJoin(): Boolean {
  return isAnnotationPresent<OneToMany>()
}

internal fun KSPropertyDeclaration.resolveTargetType(): KSType {
  val targetAnnotation = getAnnotationsByType<OneToOne>().singleOrNull()
      ?: getAnnotationsByType<ManyToOne>().singleOrNull()
  val targetEntity = targetAnnotation?.arguments?.firstOrNull { it.name?.asString() == "targetEntity" }?.value
  val typeReference = targetEntity as? KSType ?: (targetEntity as? KSClassDeclaration)?.asType(listOf())
  if (typeReference != null && typeReference.declaration.qualifiedName?.asString() != "kotlin.Unit") {
    return typeReference
  }
  return type.resolve()
}

internal fun KSPropertyDeclaration.isYawnEntity(): Boolean {
  return resolveTargetType().declaration.isYawnEntity()
}

internal data class ForeignKeyReference(
    val columnName: String,
    val typeReference: KSTypeReference,
    val isCompositeKey: Boolean,
) {
  private val type = typeReference.resolve()

  fun toTypeName(): TypeName {
    return type.toTypeName()
  }

  fun toClassName(): ClassName {
    return type.toClassName()
  }
}

private fun KSAnnotation.getStringValue(name: String): String? {
  return arguments
      .firstOrNull { it.name?.asString() == name }
      ?.value
      ?.toString()
      ?.takeUnless { it.isEmpty() }
}

private fun KSPropertyDeclaration.maybeGetAnnotatedColumnName(): String? {
  return getAnnotationsByType<Column>().singleOrNull()?.getStringValue("name")
}

internal fun KSPropertyDeclaration.typeAsClassDeclaration(): KSClassDeclaration? {
  return resolveTargetType().declaration as? KSClassDeclaration
}

internal fun KSPropertyDeclaration.getHibernateForeignKeyReference(): ForeignKeyReference? {
  val declaration = typeAsClassDeclaration() ?: return null

  // first, let's see if there are @JoinColumn annotation with a referencedColumnName attribute
  val joinColumns = getAnnotationsByType<JoinColumn>()
      .map { it.getStringValue("name") to it.getStringValue("referencedColumnName") }
      .toList()
  val referencedColumnName = joinColumns.map { it.second }.singleOrNull()

  // if it is a composite key we just assume it is the PK on the other end
  val isCompositeKey = joinColumns.size > 1

  return declaration.getAllProperties()
      .filter { property ->
        when {
          isCompositeKey -> property.isAnnotationPresent<EmbeddedId>()
          referencedColumnName != null -> {
            val name = property.maybeGetAnnotatedColumnName() ?: property.simpleName.asString()
            name == referencedColumnName
          }
          else -> property.isAnnotationPresent<Id>()
        }
      }
      .map { property ->
        ForeignKeyReference(
            columnName = property.simpleName.asString(),
            typeReference = property.type,
            isCompositeKey = isCompositeKey,
        )
      }
      .singleOrNull()
}

internal fun KSDeclaration.isYawnEntity(): Boolean {
  return isAnnotationPresent<YawnEntity>()
}
