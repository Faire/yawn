package com.faire.yawn.processors

import com.faire.yawn.YawnDef
import com.faire.yawn.YawnEntity
import com.faire.yawn.YawnTableDef
import com.faire.yawn.generators.`object`.YawnTableRefObjectGenerator
import com.faire.yawn.generators.property.CollectionJoinColumnDefGenerator
import com.faire.yawn.generators.property.ColumnDefGenerator
import com.faire.yawn.generators.property.ElementCollectionColumnDefGenerator
import com.faire.yawn.generators.property.EmbeddedDefGenerator
import com.faire.yawn.generators.property.EmbeddedIdDefGenerator
import com.faire.yawn.generators.property.JoinColumnDefGenerator
import com.faire.yawn.generators.property.JoinColumnDefWithCompositeKeyGenerator
import com.faire.yawn.generators.property.JoinColumnDefWithForeignKeyGenerator
import com.faire.yawn.generators.type.EmbeddedIdTypeGenerator
import com.faire.yawn.generators.type.EmbeddedTypeGenerator
import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnNamesGenerator.generateTableDefClassName
import com.faire.yawn.util.getHibernateForeignKeyReference
import com.faire.yawn.util.isColumn
import com.faire.yawn.util.isElementCollection
import com.faire.yawn.util.isEmbedded
import com.faire.yawn.util.isEmbeddedId
import com.faire.yawn.util.isFormula
import com.faire.yawn.util.isId
import com.faire.yawn.util.isManyToManyJoin
import com.faire.yawn.util.isManyToOneJoin
import com.faire.yawn.util.isOneToManyJoin
import com.faire.yawn.util.isOneToOneJoin
import com.faire.yawn.util.isTransient
import com.faire.yawn.util.isYawnEntity
import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import kotlin.reflect.KClass

/**
 * Implementation of [BaseYawnProcessor] for [YawnEntity] annotated classes.
 */
internal class YawnEntityProcessor(codeGenerator: CodeGenerator) : BaseYawnProcessor(codeGenerator) {
    override val annotationClass: KClass<out Annotation> = YawnEntity::class
    override val yawnDefClass: KClass<out YawnDef<*, *>> = YawnTableDef::class

    override fun generateYawnDefClassName(originalClassName: ClassName): String {
        return generateTableDefClassName(originalClassName)
    }

    override val objectRefGenerator = YawnTableRefObjectGenerator

    override fun generateProperty(
        yawnContext: YawnContext,
        property: KSPropertyDeclaration,
    ): PropertySpec? {
        val foreignKeyRef = property.getHibernateForeignKeyReference()
        val generator = when {
            property.isTransient() -> null

            (property.isOneToOneJoin() || property.isManyToOneJoin()) -> {
                when {
                    !property.isYawnEntity() -> ColumnDefGenerator
                    foreignKeyRef == null -> JoinColumnDefGenerator
                    foreignKeyRef.isCompositeKey -> JoinColumnDefWithCompositeKeyGenerator
                    else -> JoinColumnDefWithForeignKeyGenerator
                }
            }

            (property.isOneToManyJoin() || property.isManyToManyJoin()) -> CollectionJoinColumnDefGenerator
            property.isEmbeddedId() -> EmbeddedIdDefGenerator
            property.isEmbedded() -> EmbeddedDefGenerator
            property.isElementCollection() -> ElementCollectionColumnDefGenerator

            property.isColumn() || property.isId() || property.isFormula() -> ColumnDefGenerator
            else -> null
        }

        return generator?.generate(
            yawnContext = yawnContext,
            property = property,
            foreignKeyRef = foreignKeyRef,
        )
    }

    override fun additionalClassBuilder(
        yawnContext: YawnContext,
        classBuilder: TypeSpec.Builder,
    ): TypeSpec.Builder {
        return classBuilder
            .addSuperclassConstructorParameter(PARENT_PARAMETER_NAME)
            .addTypes(generateEmbeddedDefinitions(yawnContext))
    }

    /**
     * Within the generated table definition, we might need to define subclasses to represent embedded definitions.
     * This will be either fields tagged with @Embedded or composite primary keys tagged with @EmbeddedId.
     */
    private fun generateEmbeddedDefinitions(
        yawnContext: YawnContext,
    ): List<TypeSpec> {
        return yawnContext.classDeclaration.getAllProperties()
            .mapNotNull { property ->
                val generator = when {
                    property.isEmbeddedId() -> EmbeddedIdTypeGenerator
                    property.isEmbedded() -> EmbeddedTypeGenerator
                    else -> null
                }
                generator?.generate(yawnContext, property)
            }
            .toList()
    }
}

/**
 * The Yawn Processor Provider wires up the Yawn Processor bound via the META-INF file using Google's @AutoService.
 */
@AutoService(SymbolProcessorProvider::class)
internal class YawnEntityProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return YawnEntityProcessor(
            codeGenerator = environment.codeGenerator,
        )
    }
}
