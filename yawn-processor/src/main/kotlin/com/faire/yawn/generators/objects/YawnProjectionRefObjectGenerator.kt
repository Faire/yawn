package com.faire.yawn.generators.objects

import com.faire.ksp.getAllPropertiesWithAllAnnotations
import com.faire.ksp.getEffectiveVisibility
import com.faire.yawn.generators.addGeneratedAnnotation
import com.faire.yawn.project.ProjectionNode
import com.faire.yawn.project.YawnProjectionRef
import com.faire.yawn.project.YawnProjector
import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnNamesGenerator.generateProjectionObjectName
import com.faire.yawn.util.isConstructorProperty
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

private val yawnProjector = YawnProjector::class.asClassName()
private val projectionNodeClass = ProjectionNode::class.asClassName()

internal object YawnProjectionRefObjectGenerator : YawnReferenceObjectGenerator {
    /**
     * Generate an object to facilitate creating the generated [com.faire.yawn.project.YawnProjectionDef].
     * This acts as a singleton for the projection and is referenced by the user to perform queries.
     *
     * The output code will look like:
     * object SimpleBookProjection: YawnProjectionRef<SimpleBook, SimpleBookProjectionDef<SimpleBook>>() {
     *  // see the definition of the create function below
     *  fun create(...): YawnProjector<SOURCE, YawnProjectionTest.SimpleBook> { ... }
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
            .returns(yawnProjector.parameterizedBy(source, f))

        data class Property(
            val index: Int,
            val name: String,
            val type: TypeName,
        )

        val properties = yawnContext.classDeclaration.getAllPropertiesWithAllAnnotations()
            .filter { yawnContext.classDeclaration.isConstructorProperty(it) }
            .mapIndexed { idx, property ->
                Property(
                    index = idx,
                    name = property.simpleName.asString(),
                    type = property.type.toTypeName(),
                )
            }

        var extraTypeParametersIdx = 0
        for (property in properties) {
            // if the type is nullable, we want to accept both nullable and non-nullable projections
            // so we add an extra type parameter `Tx : Type?`, so that YawnProjector<SOURCE, Tx> can be either
            // YawnProjector<SOURCE, Type> or YawnProjector<SOURCE, Type?>.
            val projectionType = if (property.type.isNullable) {
                val typeVariable = TypeVariableName("T$extraTypeParametersIdx", property.type)
                extraTypeParametersIdx++

                create.addTypeVariable(typeVariable)
                typeVariable
            } else {
                property.type
            }
            create.addParameter(property.name, yawnProjector.parameterizedBy(source, projectionType))
        }

        val propertyProjections = properties.joinToString(separator = ", ") { it.name }
        val propertyParameters = properties.joinToString(separator = ",\n") {
            "${it.name} = values[${it.index}] as ${it.type}"
        }
        create.addStatement(
            """
          @Suppress("UNCHECKED_CAST")
          return (
            %T {
                %T.composite(
                    listOf($propertyProjections)
                ) { values ->
                    %T(
                      $propertyParameters
                    )
                }
            }
          )
            """.trimIndent(),
            yawnProjector.parameterizedBy(source, f),
            projectionNodeClass,
            yawnContext.classDeclaration.toClassName(),
        )

        return create.build()
    }
}
