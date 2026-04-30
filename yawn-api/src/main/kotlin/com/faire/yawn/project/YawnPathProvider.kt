package com.faire.yawn.project

import com.faire.yawn.query.YawnCompilationContext

/**
 * A column reference that can be converted to a dotted path.
 *
 * * [com.faire.yawn.YawnDef.YawnColumnDef] — the path of a simple column, e.g. `"name"` or `"a.name"`.
 * * [com.faire.yawn.YawnTableDef.JoinColumnDef] — the path of a join association, e.g. `"publisher"` or `"a.publisher"`.
 */
fun interface YawnPathProvider<SOURCE : Any> {
    fun generatePath(context: YawnCompilationContext): String
}
