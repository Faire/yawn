package com.faire.yawn

import com.faire.yawn.query.YawnCompilationContext

private const val ROOT_ALIAS = "root"

/**
 * Used by [YawnTableDef] to track parent relationships needed for compilation context.
 *
 * When the table def is the root of the query, the table def will not have a parent. Otherwise, it will have a
 * parent containing the column used to join to it. This allows us to build a tree structure for compilation.
 */
interface YawnTableDefParent {
  /**
   * Returns a base string to use for generating aliases for the table definition.
   */
  fun getAliasBaseString(context: YawnCompilationContext): String?

  /**
   * To be used for the root query being compiled
   */
  object RootTableDefParent : YawnTableDefParent {
    override fun getAliasBaseString(context: YawnCompilationContext): String? {
      /**
       * Hibernate can occasionally break with root aliases, but they're necessary for correlated subqueries, so
       * we only generate a root alias if we need to.
       */
      return if (context.withSubQuery) ROOT_ALIAS else null
    }
  }

  /**
   * To be used to generate aliases for sub queries
   */
  class SubqueryTableDefParent(val tableName: String) : YawnTableDefParent {
    override fun getAliasBaseString(context: YawnCompilationContext): String? = tableName
  }

  /**
   * To be used for association parents
   */
  class AssociationTableDefParent(
      val parentColumnDef: YawnTableDef<*, *>.JoinColumnDef<*, *>,
  ) : YawnTableDefParent {
    override fun getAliasBaseString(context: YawnCompilationContext): String? {
      return parentColumnDef.path(context)
    }
  }
}
