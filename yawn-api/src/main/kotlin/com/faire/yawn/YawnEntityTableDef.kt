package com.faire.yawn

/**
 * Base class for the table definition of an entity, which is what KSP extends for each [YawnEntity].
 *
 * Every [YawnTableDef] can be joined to, but only an entity can be the root of a query or of a sub-query. A table of
 * plain values, such as [YawnTableDef.ElementCollectionDef], is not mapped as an entity and so is not one of these.
 *
 * @param SOURCE the type of the original table that the criteria is based off of.
 * @param D the type of the entity.
 * @param entityClass the runtime reference to the class [D], provided by the generated definition.
 */
abstract class YawnEntityTableDef<SOURCE : Any, D : Any>(
    parent: YawnTableDefParent,
    internal val entityClass: Class<D>,
) : YawnTableDef<SOURCE, D>(parent)
