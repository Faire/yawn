package com.faire.yawn.query

import com.faire.yawn.YawnTableDef
import com.faire.yawn.YawnTableDefParent
import com.faire.yawn.criteria.query.YawnAliasManager

/**
 * Used to pass context information during query compilation.
 */
data class YawnCompilationContext(
    val withSubQuery: Boolean = false,
) {
  private val aliasManager: YawnAliasManager = YawnAliasManager()

  fun generateAlias(tableDef: YawnTableDef<*, *>): String? {
    return generateAlias(tableDef.parent)
  }

  fun generateAlias(parent: YawnTableDefParent): String? {
    return aliasManager.generate(parent, this)
  }

  companion object {
    fun fromQuery(query: YawnQuery<*, *>): YawnCompilationContext {
      return YawnCompilationContext(withSubQuery = query.hasSubQuery())
    }
  }
}
