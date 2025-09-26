package com.faire.yawn.generators.type

import com.faire.yawn.YawnTableDef
import com.faire.yawn.generators.addGeneratedAnnotation
import com.faire.yawn.generators.property.ColumnDefGenerator
import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnNamesGenerator.generateEmbeddedDefClassName
import com.faire.yawn.util.YawnNamesGenerator.generateInternalPathName
import com.faire.yawn.util.YawnParameter
import com.faire.yawn.util.typeAsClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * This will generate an [com.faire.yawn.YawnTableDef.EmbeddedDef] subclass for a field
 * that is tagged with `@Embedded`
 *
 * E.g. for a given embeddable data class
 * ```
 * @Embeddable
 * data class Foo(
 *    @Column(name = "bar")
 *    var bar: String,
 *
 *    @Column(name = "baz")
 *    var baz: Int,
 * )
 * ```
 *
 * Used in an entity like so
 * ```
 *   @Embedded
 *   override lateinit var foo: Foo
 *     protected set
 * ```
 *
 * It will look like this:
 * ```
 *  inner class FooDef(
 *    private val _yawnPath: String? = null
 *  ): YawnTableDef<SOURCE, DbFoo>.EmbeddedDef<Foo>() {
 *    val bar: YawnTableDef<SOURCE, DbFoo>.ColumnDef<String> = ColumnDef(_yawnPath, "foo", "bar")
 *    val baz: YawnTableDef<SOURCE, DbFoo>.ColumnDef<Int> = ColumnDef(_yawnPath, "foo", "baz")
 *  }
 * ```
 *
 * Note that the class takes in an optional `_yawnPath` parameter. This will not be set by the main reference on
 * this class, but rather to allow for queries to the object with the embedded property to be able to access
 * the columns on the embedded def properly.
 *
 * A column definition will be generated using [com.faire.yawn.generators.property.EmbeddedDefGenerator] using this
 * type.
 */
internal object EmbeddedTypeGenerator : YawnEmbeddableTypeGenerator {
    private val superClassType = YawnTableDef.EmbeddedDef::class
    private val superClassTypeName = superClassType.simpleName!!

    override fun generate(
        yawnContext: YawnContext,
        /** This will be the property `var foo: Foo` from the example above */
        propertyDeclaration: KSPropertyDeclaration,
    ): TypeSpec {
        return generate(yawnContext, propertyDeclaration, superClassTypeName)
    }

    fun generate(
        yawnContext: YawnContext,
        propertyDeclaration: KSPropertyDeclaration,
        superClassTypeName: String,
    ): TypeSpec {
        val originalType = propertyDeclaration.type.resolve() // Foo
        val generatedTypeName = generateEmbeddedDefClassName(originalType.toClassName()) // FooDef

        val yawnPathName = generateInternalPathName()
        val yawnPathType = String::class.asTypeName().copy(nullable = true)

        val pathPrefix = propertyDeclaration.simpleName.asString()
        val pathPrefixes = listOf(
            YawnParameter.literal(yawnPathName),
            YawnParameter.string(pathPrefix),
        )

        val typeSpec = TypeSpec.classBuilder(generatedTypeName)
            .addGeneratedAnnotation(EmbeddedTypeGenerator::class)
            .addModifiers(KModifier.INNER)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        ParameterSpec.builder(yawnPathName, yawnPathType)
                            .defaultValue("null")
                            .build(),
                    )
                    .build(),
            )
            .addProperty(
                PropertySpec.builder(yawnPathName, yawnPathType)
                    .addModifiers(KModifier.PRIVATE)
                    .initializer(yawnPathName)
                    .build(),
            )
            .superclass(
                yawnContext.superClassName.nestedClass(
                    name = superClassTypeName,
                    typeArguments = listOf(originalType.toClassName()),
                ),
            )
            .addSuperclassConstructorParameter("%N", yawnPathName)
            .addSuperclassConstructorParameter("%S", pathPrefix)

        propertyDeclaration.typeAsClassDeclaration()
            ?.getAllProperties()
            ?.forEach { property ->
                val propertySpec = ColumnDefGenerator.generate(yawnContext, property, pathPrefixes)
                typeSpec.addProperty(propertySpec)
            }

        return typeSpec.build()
    }
}
