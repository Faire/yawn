package com.faire.yawn.generators.type

import com.faire.yawn.YawnTableDef
import com.faire.yawn.util.YawnContext
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.TypeSpec

/**
 * This will generate an [com.faire.yawn.YawnTableDef.EmbeddedDef] subclass for a field that is tagged with
 * `@EmbeddedId`.
 *
 * For a given composite key data class:
 *
 * ```
 *   @Embeddable
 *   data class FooCompositeId(
 *       @Column(updatable = false, nullable = false, name = "parent_foo_id")
 *       var fooId: Id<DbFoo>,
 *
 *       @Column(updatable = false, nullable = false, name = "id")
 *       override var id: Id<DbFooComposite>,
 *   ) : CompositeId<DbFooComposite>
 * ```
 *
 * Used in an entity like so:
 *
 * ```
 *   @EmbeddedId
 *   override lateinit var cid: FooCompositeId
 *     protected set
 * ```
 *
 * It will look like this:
 *
 * ```
 *   inner class FooCompositeIdDef(
 *     private val _yawnPath: String? = null,
 *   ) : YawnTableDef<SOURCE, DbFooComposite>.EmbeddedDef<FooCompositeId>(_yawnPath, "cid") {
 *     val fooId: YawnTableDef<SOURCE, DbFooComposite>.ColumnDef<Id<DbFoo>> =
 *         ColumnDef(_yawnPath, "cid", "fooId")
 *
 *     val id: YawnTableDef<SOURCE, DbFooComposite>.ColumnDef<Id<DbFooComposite>> =
 *         ColumnDef(_yawnPath, "cid", "id")
 *   }
 * ```
 *
 * Note that the class takes in an optional `_yawnPath` parameter. This will not be set by the main reference on
 * this class, but rather to allow for foreign keys using this composite id on other classes.
 *
 * A column definition will be generated using [com.faire.yawn.generators.property.EmbeddedIdDefGenerator] using this
 * type.
 */
internal object EmbeddedIdTypeGenerator : YawnEmbeddableTypeGenerator {
    private val superClassType = YawnTableDef.EmbeddedDef::class
    private val superClassTypeName = superClassType.simpleName!!

    override fun generate(
        yawnContext: YawnContext,
        /** This will be the property `var cid: FooCompositeId` from the example above */
        propertyDeclaration: KSPropertyDeclaration,
    ): TypeSpec {
        return EmbeddedTypeGenerator.generate(yawnContext, propertyDeclaration, superClassTypeName)
    }
}
