package com.faire.yawn.project

import com.faire.yawn.query.YawnCompilationContext
import org.hibernate.Criteria
import org.hibernate.criterion.CriteriaQuery

/**
 * A [YawnSqlProjection] whose expression is built through a [YawnSqlScope], as [YawnProjections.sqlValue] produces.
 *
 * This is preferred to [HibernateYawnSqlProjection]; because the expression is assembled here, while the query is
 * being rendered, it can ask the ORM to resolve entity properties to physical columns, rather than being handed a
 * finished string with names patched into it afterwards.
 *
 * Yawn owns the result alias for these projections. The expression is bare, so it is selected under a name taken from
 * the compilation [context] and unique within it, meaning two SQL values in one query can never be read from the same
 * column, and the caller never has to invent a name.
 *
 * So `sqlValue<Long> { "SUM(${'$'}{books.numberOfPages.sql})" }` renders as `SUM(this_.numberOfPages) as _yawn_ct0`.
 */
internal class ScopedYawnSqlProjection<SOURCE : Any>(
    private val context: YawnCompilationContext,
    private val leaf: ProjectionLeaf.SqlValue<SOURCE>,
) : YawnSqlProjection(context.generateResultAlias(), leaf.resultType) {
    override fun renderSql(
        criteria: Criteria,
        criteriaQuery: CriteriaQuery,
    ): String {
        val scope = CriteriaSqlScope<SOURCE>(context, criteria, criteriaQuery)
        return "${leaf.render(scope)} as $columnAlias"
    }
}
