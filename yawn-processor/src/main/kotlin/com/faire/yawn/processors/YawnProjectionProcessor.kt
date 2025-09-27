package com.faire.yawn.processors

import com.faire.yawn.YawnDef
import com.faire.yawn.generators.objects.YawnProjectionRefObjectGenerator
import com.faire.yawn.generators.properties.ProjectionColumnDefGenerator
import com.faire.yawn.project.YawnProjection
import com.faire.yawn.project.YawnProjectionDef
import com.faire.yawn.util.YawnContext
import com.faire.yawn.util.YawnNamesGenerator.generateProjectionDefClassName
import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec
import kotlin.reflect.KClass

internal class YawnProjectionProcessor(codeGenerator: CodeGenerator) : BaseYawnProcessor(codeGenerator) {
    override val annotationClass: KClass<out Annotation> = YawnProjection::class
    override val yawnDefClass: KClass<out YawnDef<*, *>> = YawnProjectionDef::class

    override fun generateYawnDefClassName(originalClassName: ClassName): String {
        return generateProjectionDefClassName(originalClassName)
    }

    override val objectRefGenerator = YawnProjectionRefObjectGenerator

    override fun generateProperty(
        yawnContext: YawnContext,
        property: KSPropertyDeclaration,
    ): PropertySpec? {
        return ProjectionColumnDefGenerator.generate(
            yawnContext = yawnContext,
            property = property,
        )
    }
}

/**
 * Implementation of [BaseYawnProcessor] for [YawnProjection] annotated classes.
 */
@AutoService(SymbolProcessorProvider::class)
internal class YawnProjectionProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return YawnProjectionProcessor(
            codeGenerator = environment.codeGenerator,
        )
    }
}
