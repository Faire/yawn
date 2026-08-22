package com.faire.yawn.project

/**
 * The context available while building a raw SQL expression, via
 * [com.faire.yawn.criteria.query.ProjectedYawnQueryScope.sqlValue].
 *
 * The SQL is assembled as the ORM renders the query, when it can be resolved to a physical backing column.
 */
interface YawnSqlScope<SOURCE : Any> {
    /**
     * Resolves this column to the physical column backing it, qualified by its table alias, e.g. `this_.call_number`.
     *
     * Fails if the property cannot be resolved, or if it is backed by more than one column (a composite key, say),
     * since there would be no single value to substitute into the expression.
     */
    val YawnPathProvider<SOURCE>.sql: String
}
