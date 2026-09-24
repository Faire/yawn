package com.faire.yawn.generators.objects

import com.faire.ksp.getAllPropertiesWithAllAnnotations
import com.faire.ksp.getEffectiveVisibility
import com.faire.yawn.generators.addGeneratedAnnotation
import com.faire.yawn.project.ProjectionNode
import com.faire.yawn.project.ProjectionSlot
import com.faire.yawn.project.YawnProjectionRef
import com.faire.yawn.project.YawnProjections
import com.faire.yawn.project.YawnProjector
import com.faire.yawn.project.YawnValueProjector
import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnNamesGenerator.generateOrderableProjectionClassName
import com.faire.yawn.util.YawnNamesGenerator.generateProjectionObjectName
import com.faire.yawn.util.isConstructorProperty
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

private val yawnProjector = YawnProjector::class.asClassName()
private val yawnValueProjector = YawnValueProjector::class.asClassName()
private val projectionNodeClass = ProjectionNode::class.asClassName()
private val projectionSlotClass = ProjectionSlot::class.asClassName()
private val yawnProjectionsObject = YawnProjections::class.asClassName()

private data class Property(
    val index: Int,
    val name: String,
    val type: TypeName,
)

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
            .addFunction(
                generateCreateOrderableFunction(
                    yawnContext = yawnContext,
                    orderableClassName = orderableClassName(originalClassName),
                ),
            )
            .build()
    }

    /**
     * Generates the orderable counterpart of [generate]'s object: a class exposing each field as a [ProjectionSlot],
     * so [com.faire.yawn.criteria.query.orderAscBy]/[com.faire.yawn.criteria.query.orderDescBy] can order by one of
     * them after the fact, the same way [com.faire.yawn.project.YawnProjectionPair] does for [YawnProjections.pair].
     * Returned by [generate]'s object's `createOrderable` function.
     */
    fun generateOrderableRefType(yawnContext: YawnContext): TypeSpec {
        val classDeclaration = yawnContext.classDeclaration
        val f = classDeclaration.toClassName()
        val source = TypeVariableName("SOURCE", Any::class.asTypeName())
        val properties = computeProperties(yawnContext)

        val constructor = FunSpec.constructorBuilder().addModifiers(KModifier.INTERNAL)
        val classBuilder = TypeSpec.classBuilder(orderableClassName(f))
            .addGeneratedAnnotation(YawnProjectionRefObjectGenerator::class)
            .addModifiers(classDeclaration.getEffectiveVisibility())
            .addTypeVariable(source)
            .addSuperinterface(yawnProjector.parameterizedBy(source, f))

        for (property in properties) {
            val slotType = projectionSlotClass.parameterizedBy(source, property.type)
            constructor.addParameter(property.name, slotType)
            classBuilder.addProperty(
                PropertySpec.builder(property.name, slotType)
                    .initializer(property.name)
                    .build(),
            )
        }
        classBuilder.primaryConstructor(constructor.build())

        classBuilder.addFunction(
            FunSpec.builder("projection")
                .addModifiers(KModifier.OVERRIDE)
                .returns(projectionNodeClass.parameterizedBy(source, f))
                .addStatement(
                    """
                  @Suppress("UNCHECKED_CAST")
                  return %T.composite(
                      listOf(${properties.joinToString(", ") { it.name }})
                  ) { values ->
                      %T(
                        ${properties.joinToString(",\n") { "${it.name} = values[${it.index}] as ${it.type}" }}
                      )
                  }
                    """.trimIndent(),
                    projectionNodeClass,
                    f,
                )
                .build(),
        )

        return classBuilder.build()
    }

    private fun orderableClassName(originalClassName: ClassName): ClassName {
        return ClassName(originalClassName.packageName, generateOrderableProjectionClassName(originalClassName))
    }

    private fun computeProperties(yawnContext: YawnContext): List<Property> {
        return yawnContext.classDeclaration.getAllPropertiesWithAllAnnotations()
            .filter { yawnContext.classDeclaration.isConstructorProperty(it) }
            .mapIndexed { idx, property ->
                Property(
                    index = idx,
                    name = property.simpleName.asString(),
                    type = property.type.toTypeName(),
                )
            }
            .toList()
    }

    /**
     * Adds one parameter per [properties] entry to [functionBuilder], typed `projectorClass<SOURCE, Type>` - adding
     * an extra type parameter `Tx : Type?` per nullable field so that a projector of either `Type` or `Type?` is
     * accepted (see [generateCreateFunction]/[generateCreateOrderableFunction]).
     */
    private fun addProjectorParameters(
        functionBuilder: FunSpec.Builder,
        source: TypeVariableName,
        properties: List<Property>,
        projectorClass: ClassName,
    ) {
        var extraTypeParametersIdx = 0
        for (property in properties) {
            val projectionType = if (property.type.isNullable) {
                val typeVariable = TypeVariableName("T$extraTypeParametersIdx", property.type)
                extraTypeParametersIdx++

                functionBuilder.addTypeVariable(typeVariable)
                typeVariable
            } else {
                property.type
            }
            functionBuilder.addParameter(property.name, projectorClass.parameterizedBy(source, projectionType))
        }
    }

    private fun generateCreateFunction(
        yawnContext: YawnContext,
    ): FunSpec {
        val source = TypeVariableName("SOURCE", Any::class.asTypeName())
        val f = yawnContext.classDeclaration.toClassName()
        val properties = computeProperties(yawnContext)

        val create = FunSpec.builder("create")
            .addTypeVariable(source)
            .returns(yawnProjector.parameterizedBy(source, f))

        addProjectorParameters(create, source, properties, yawnProjector)

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

    /**
     * Generates `createOrderable`: like `create`, but accepts single-value ([YawnValueProjector]) projections only,
     * and returns [generateOrderableRefType]'s class instead of a bare [YawnProjector] - see its documentation.
     *
     * Unlike `create`, parameters are typed exactly `YawnValueProjector<SOURCE, Type>` (no widening for nullable
     * fields): each field's [ProjectionSlot] is invariant in its type, so a projector accepted for e.g. a `Long?`
     * field must itself already be typed `YawnValueProjector<SOURCE, Long?>`, not `YawnValueProjector<SOURCE, Long>`.
     */
    private fun generateCreateOrderableFunction(
        yawnContext: YawnContext,
        orderableClassName: ClassName,
    ): FunSpec {
        val source = TypeVariableName("SOURCE", Any::class.asTypeName())
        val properties = computeProperties(yawnContext)

        val createOrderable = FunSpec.builder("createOrderable")
            .addTypeVariable(source)
            .returns(orderableClassName.parameterizedBy(source))

        for (property in properties) {
            createOrderable.addParameter(property.name, yawnValueProjector.parameterizedBy(source, property.type))
        }

        val slotArguments = properties.joinToString(separator = ",\n") {
            "${it.name} = %T.orderableSlot(${it.name})"
        }
        createOrderable.addStatement(
            "return %T(\n$slotArguments\n)",
            orderableClassName,
            *Array(properties.size) { yawnProjectionsObject },
        )

        return createOrderable.build()
    }
}
