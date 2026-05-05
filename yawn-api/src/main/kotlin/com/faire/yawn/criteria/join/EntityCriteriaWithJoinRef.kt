package com.faire.yawn.criteria.join

import com.faire.yawn.YawnTableDef
import com.faire.yawn.criteria.builder.EntityYawnQueryBuilder

/**
 * Wrapper class that holds an [EntityYawnQueryBuilder] along with a specific join reference.
 * This allows for a fluent API where both the criteria and join reference are returned together.
 *
 * @param T the type of the entity being queried.
 * @param DEF the table definition of the entity being queried.
 * @param F the type of the joined entity.
 * @param D the table definition of the joined entity.
 */
data class EntityCriteriaWithJoinRef<T : Any, DEF : YawnTableDef<T, T>, F : Any, D : YawnTableDef<T, F>>(
    val criteria: EntityYawnQueryBuilder<T, DEF>,
    val joinRef: EntityYawnQueryBuilder<T, DEF>.YawnJoinRef<F, D>,
)
