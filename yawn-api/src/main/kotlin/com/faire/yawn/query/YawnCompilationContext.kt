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

    private var resultAliasCounter: Int = 0

    fun generateAlias(tableDef: YawnTableDef<*, *>): String? {
        return generateAlias(tableDef.parent)
    }

    fun generateAlias(parent: YawnTableDefParent): String? {
        return aliasManager.generate(parent, this)
    }

    /**
     * Generates an alias for a projected SQL value, unique within this compilation.
     *
     * A projection that selects a raw SQL value must name it, so that the ORM can read that value back out of the
     * result set. Two projections in the same projection list sharing one name are indistinguishable at read time,
     * and both end up reading whichever column the name resolves to first.
     *
     * Note that this is unrelated to [generateAlias]: those name the *tables* a query selects from, whereas this
     * names a single *column* the query selects.
     */
    internal fun generateResultAlias(): String = "$RESULT_ALIAS_PREFIX${resultAliasCounter++}"

    companion object {
        fun fromQuery(query: YawnQuery<*, *>): YawnCompilationContext {
            return YawnCompilationContext(withSubQuery = query.hasSubQuery())
        }
    }
}

private const val RESULT_ALIAS_PREFIX = "_yawn_ct"
