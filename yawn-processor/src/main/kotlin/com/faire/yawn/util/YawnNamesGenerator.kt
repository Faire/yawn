package com.faire.yawn.util

import com.faire.ksp.getUniqueSimpleName
import com.squareup.kotlinpoet.ClassName

/**
 * Centralized place to generate the names of the new types created by Yawn via KSP.
 *
 * The names of these types are always based on some variation on top of some original class name.
 * For nested classes, we prepend the simple names of the nested classes to the original class name to avoid
 * naming collisions (see [getUniqueSimpleName] for more details).
 */
internal object YawnNamesGenerator {
    /**
     * For entities annotated with @YawnEntity
     * For example: DbBook -> DbBookTableDef
     */
    fun generateTableDefClassName(originalClassName: ClassName): String {
        return "${originalClassName.getUniqueSimpleName()}TableDef"
    }

    /**
     * For entities annotated with @YawnEntity
     * For example: DbBook -> DbBookTable
     */
    fun generateTableObjectName(originalClassName: ClassName): String {
        return "${originalClassName.getUniqueSimpleName()}Table"
    }

    /**
     * For classes annotated with @Embedded or @EmbeddedId
     * For example: FooCompositeId -> FooCompositeIdDef
     */
    fun generateEmbeddedDefClassName(originalClassName: ClassName): String {
        return "${originalClassName.getUniqueSimpleName()}Def"
    }

    /**
     * For projections annotated with @YawnProjection
     * For example: YawnProjectionTest.SimpleBook -> YawnProjectionTest_SimpleBookProjectionDef
     */
    fun generateProjectionDefClassName(originalClassName: ClassName): String {
        return "${originalClassName.getUniqueSimpleName()}ProjectionDef"
    }

    /**
     * For projections annotated with @YawnProjection
     * For example: YawnProjectionTest.SimpleBook -> YawnProjectionTest_SimpleBookProjection
     */
    fun generateProjectionObjectName(originalClassName: ClassName): String {
        return "${originalClassName.getUniqueSimpleName()}Projection"
    }

    /**
     * For embedded properties, we need to create an internal field to store the current path, and we need it to
     * not clash with any user defined values.
     */
    fun generateInternalPathName(): String {
        return "_yawnPath"
    }
}
