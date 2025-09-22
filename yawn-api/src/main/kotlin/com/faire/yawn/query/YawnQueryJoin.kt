package com.faire.yawn.query

import com.faire.yawn.YawnTableDef
import com.faire.yawn.YawnTableDefParent.AssociationTableDefParent
import org.hibernate.sql.JoinType

/**
 * A type-safe wrapper for a join in a [YawnQuery].
 */
data class YawnQueryJoin<SOURCE : Any>(
    val columnDef: YawnTableDef<*, *>.JoinColumnDef<*, *>,
    val parent: AssociationTableDefParent,
    val joinType: JoinType,
    val joinCriteria: MutableList<YawnQueryCriterion<SOURCE>> = mutableListOf(),
) {
  fun path(context: YawnCompilationContext): String = columnDef.path(context)
}
