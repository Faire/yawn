package com.faire.yawn

import com.faire.yawn.adapter.YawnValueAdapter
import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.query.YawnCompilationContext
import org.hibernate.criterion.Projection
import org.hibernate.criterion.Projections

/**
 * Base class for all Yawn Table Definitions.
 *
 * This class is automatically extended by KSP to generate the table definitions for you.
 * Just add [YawnEntity] to your entity and the rest is done for you.
 *
 * @param SOURCE the type of the original table that the criteria is based off of.
 * @param D the type of the entity.
 */
abstract class YawnTableDef<SOURCE : Any, D : Any>(
    internal val parent: YawnTableDefParent,
) : YawnDef<SOURCE, D>() {

    /**
     * Base class for all Yawn embedded definitions.
     * This class is automatically extended by KSP to generate the embedded definitions for you for all Yawn entities.
     * Note that it can be treated as a column definition, as well as a container for sub-columns.
     * Accessing this requires no joins.
     *
     * @param F the type of the embedded entity.
     */
    abstract inner class EmbeddedDef<F>(
        private val rootPath: String?,
        private val path: String,
    ) : YawnColumnDef<F>() {
        override fun generatePath(context: YawnCompilationContext): String {
            return listOfNotNull(context.generateAlias(parent), rootPath, path).joinToString(".")
        }
    }

    /**
     * This is used by Yawn to represent the implicit join reference created by the usage of the @ElementCollection
     * annotation.
     * Note that the synthetic column name "elements" is employed by Hibernate to reference the relationship column.
     */
    inner class ElementCollectionDef<D : Any>(
        parent: YawnTableDefParent,
    ) : YawnTableDef<SOURCE, D>(parent) {
        val elements: ColumnDef<D> = ColumnDef("elements")
    }

    /**
     * Base class for all Yawn Column Definitions.
     * This class is automatically extended by KSP to generate the column definitions for you for all Yawn entities.
     *
     * @param F the type of the column.
     */
    inner class ColumnDef<F>(
        private vararg val path: String?,
        private val adapter: YawnValueAdapter<F>? = null,
    ) : YawnColumnDef<F>() {
        override fun generatePath(context: YawnCompilationContext): String {
            return listOfNotNull(context.generateAlias(parent), *path).joinToString(".")
        }

        override fun adaptValue(value: F): Any? {
            return adapter?.adapt(value) ?: super.adaptValue(value)
        }
    }

    /**
     * Base class for all Yawn Join Column Definitions.
     *
     * @param T the type of the entity class of the join table
     * @param DEF the type definition (YawnTableDef) for T
     */
    open inner class JoinColumnDef<T : Any, DEF : YawnTableDef<SOURCE, T>>(
        private val parent: YawnTableDefParent,
        protected val name: String,
        private val tableDefProvider: (YawnTableDefParent) -> DEF,
    ) {
        fun path(context: YawnCompilationContext): String {
            return listOfNotNull(context.generateAlias(parent), name).joinToString(".")
        }

        fun joinTableDef(parent: YawnTableDefParent): DEF {
            return tableDefProvider(parent)
        }
    }

    /**
     * [JoinColumnDef] but for when there is a trackable foreign key reference.
     * This is a base class that should not be used directly; use instead:
     *
     * * [JoinColumnDefWithForeignKey] for when the reference can be backed by an @Id.
     * * [JoinColumnDefWithCompositeKey] for when the reference can be backed by an @EmbeddedId.
     *
     * @param T the type of the entity class of the join table
     * @param DEF the type definition (YawnTableDef) for T
     * @param REF the type of the reference
     */
    abstract inner class JoinColumnDefWithReference<T : Any, DEF : YawnTableDef<SOURCE, T>, REF>(
        parent: YawnTableDefParent,
        name: String,
        private val foreignKeyProvider: (String) -> REF,
        tableDefProvider: (YawnTableDefParent) -> DEF,
    ) : JoinColumnDef<T, DEF>(
        parent,
        name,
        tableDefProvider,
    ),
        YawnQueryProjection<SOURCE, T> {
        val foreignKey: REF
            get() = foreignKeyProvider(name)

        override fun compile(context: YawnCompilationContext): Projection {
            return Projections.property(path(context))
        }

        override fun project(value: Any?): T {
            @Suppress("UNCHECKED_CAST")
            return value as T
        }
    }

    /**
     * [JoinColumnDef] but for when the reference can be backed by an @Id.
     *
     * @param T the type of the entity class of the join table
     * @param DEF the type definition (YawnTableDef) for T
     * @param ID the type of the id of the joined entity class
     */
    inner class JoinColumnDefWithForeignKey<T : Any, DEF : YawnTableDef<SOURCE, T>, ID>(
        parent: YawnTableDefParent,
        name: String,
        foreignKeyName: String,
        tableDefProvider: (YawnTableDefParent) -> DEF,
    ) : JoinColumnDefWithReference<T, DEF, ColumnDef<ID>>(
        parent,
        name,
        { ColumnDef(it, foreignKeyName) },
        tableDefProvider,
    )

    /**
     * [JoinColumnDef] but for when the reference can be backed by an @EmbeddedId.
     *
     * @param T the type of the entity class of the join table
     * @param DEF the type definition (YawnTableDef) for T
     * @param CID the type of the composite key id of the joined entity class
     */
    inner class JoinColumnDefWithCompositeKey<
        T : Any,
        DEF : YawnTableDef<SOURCE, T>,
        CID : YawnTableDef<SOURCE, T>.EmbeddedDef<*>,
        >(
        parent: YawnTableDefParent,
        name: String,
        foreignKeyProvider: (String) -> CID,
        tableDefProvider: (YawnTableDefParent) -> DEF,
    ) : JoinColumnDefWithReference<T, DEF, CID>(
        parent,
        name,
        foreignKeyProvider,
        tableDefProvider,
    )

    /**
     * Class to represent OneToMany and ManyToMany relations.
     * Extends [JoinColumnDef]
     *
     * @param T the type of the entity class of the join table which is the type of the generic of the collection.
     * @param DEF the type definition (YawnTableDef) for T
     */
    inner class CollectionJoinColumnDef<T : Any, DEF : YawnTableDef<SOURCE, T>>(
        parent: YawnTableDefParent,
        name: String,
        tableDefProvider: (YawnTableDefParent) -> DEF,
    ) : JoinColumnDef<T, DEF>(parent, name, tableDefProvider)
}
