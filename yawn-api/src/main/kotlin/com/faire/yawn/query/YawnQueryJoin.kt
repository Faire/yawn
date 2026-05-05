package com.faire.yawn.query

import com.faire.yawn.YawnTableDef
import com.faire.yawn.YawnTableDefParent.AssociationTableDefParent
import org.hibernate.sql.JoinType

/**
 * Part of an [YawnQuery] representing each join in the JOIN clause.
 *
 * Can be safely constructed using the `join` function on appropriate DSL query lambdas.
 */
data class YawnQueryJoin<SOURCE : Any>(
    val columnDef: YawnTableDef<*, *>.JoinColumnDef<*, *>,
    val parent: AssociationTableDefParent,
    val joinType: JoinType,
    val joinCriteria: MutableList<YawnQueryCriterion<SOURCE>> = mutableListOf(),
) {
    fun generatePath(context: YawnCompilationContext): String = columnDef.generatePath(context)
}
