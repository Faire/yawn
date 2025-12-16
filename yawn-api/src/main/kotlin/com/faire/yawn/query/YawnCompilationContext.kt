package com.faire.yawn.query

import com.faire.yawn.YawnTableDef
import com.faire.yawn.YawnTableDefParent
import com.faire.yawn.criteria.query.YawnAliasManager
import com.faire.yawn.util.ReflectionUtils

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

    /**
     * Adapts the given value for use in queries, unwrapping value classes if necessary.
     * Added to the context in case we need to support more complex conversions in the future, or consult
     * context options or configurations.
     */
    fun adaptValue(value: Any?): Any? {
        return value?.let { ReflectionUtils.tryUnwrapValueClass(value) }
    }

    companion object {
        fun fromQuery(query: YawnQuery<*, *>): YawnCompilationContext {
            return YawnCompilationContext(withSubQuery = query.hasSubQuery())
        }
    }
}
