package com.faire.yawn.generators.`object`

import com.faire.ksp.getEffectiveVisibility
import com.faire.yawn.generators.addGeneratedAnnotation
import com.faire.yawn.project.YawnCompositeQueryProjection
import com.faire.yawn.project.YawnProjectionRef
import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnNamesGenerator.generateProjectionObjectName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

private val yawnQueryProjection = YawnQueryProjection::class.asClassName()
private val typedProjectionImpl = YawnCompositeQueryProjection::class.asClassName()

internal object YawnProjectionRefObjectGenerator : YawnReferenceObjectGenerator {
  /**
   * Generate an object to facilitate creating the generated [com.faire.yawn.project.YawnProjectionDef].
   * This acts as a singleton for the projection and is referenced by the user to perform queries.
   *
   * The output code will look like:
   * object SimpleBookProjection: YawnProjectionRef<SimpleBook, SimpleBookProjectionDef<SimpleBook>>() {
   *  // see the definition of the create function below
   *  fun create(...): TypedProjection<SOURCE, YawnProjectionTest.SimpleBook> { ... }
   * }
   */
  override fun generate(
      yawnContext: YawnContext,
  ): TypeSpec {
    val classDeclaration = yawnContext.classDeclaration
    val newClassName = yawnContext.newClassName.simpleName

    val originalClassName = classDeclaration.toClassName()
    val objectName = generateProjectionObjectName(originalClassName)

    val typeParameter = ClassName(originalClassName.packageName, newClassName).parameterizedBy(originalClassName)
    val superInterface = YawnProjectionRef::class.asClassName().parameterizedBy(originalClassName, typeParameter)

    return TypeSpec.objectBuilder(objectName)
        .addGeneratedAnnotation(YawnProjectionRefObjectGenerator::class)
        .addSuperinterface(superInterface)
        .addModifiers(classDeclaration.getEffectiveVisibility())
        .addFunction(generateCreateFunction(yawnContext = yawnContext))
        .build()
  }

  private fun generateCreateFunction(
      yawnContext: YawnContext,
  ): FunSpec {
    val source = TypeVariableName("SOURCE", Any::class.asTypeName())
    val f = yawnContext.classDeclaration.toClassName()

    val create = FunSpec.builder("create")
        .addTypeVariable(source)
        .returns(yawnQueryProjection.parameterizedBy(source, f))

    data class Property(
        val index: Int,
        val name: String,
        val type: TypeName,
    )

    val properties = yawnContext.classDeclaration.getAllProperties().mapIndexed { idx, property ->
      Property(
          index = idx,
          name = property.simpleName.asString(),
          type = property.type.toTypeName(),
      )
    }

    var extraTypeParametersIdx = 0
    for (property in properties) {
      // if the type is nullable, we want to accept both nullable and non-nullable projections
      // so we add an extra type parameter `Tx : Type?`, so that Projection<SOURCE, Tx> can be either
      // Projection<SOURCE, Type> or Projection<SOURCE, Type?>.
      val projectionType = if (property.type.isNullable) {
        val typeVariable = TypeVariableName("T$extraTypeParametersIdx", property.type)
        extraTypeParametersIdx++

        create.addTypeVariable(typeVariable)
        typeVariable
      } else {
        property.type
      }
      create.addParameter(property.name, yawnQueryProjection.parameterizedBy(source, projectionType))
    }

    val propertyProjections = properties.joinToString(separator = ",\n") { it.name }
    val propertyParameters = properties.joinToString(separator = ",\n") {
      "${it.name} = ${it.name}.project(queryResult[${it.index}])"
    }
    create.addStatement(
        """
          return (
            %T(
                $propertyProjections
            ) { row ->
                val queryResult = row as Array<*>
                %T(
                  $propertyParameters
                )
            }
          )
        """.trimIndent(),
        typedProjectionImpl.parameterizedBy(source, f),
        yawnContext.classDeclaration.toClassName(),
    )

    return create.build()
  }
}
