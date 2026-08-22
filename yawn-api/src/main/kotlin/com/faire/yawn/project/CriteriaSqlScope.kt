package com.faire.yawn.project

import com.faire.yawn.query.YawnCompilationContext
import org.hibernate.Criteria
import org.hibernate.HibernateException
import org.hibernate.criterion.CriteriaQuery

/**
 * The [YawnSqlScope] handed to an expression for a Hibernate Criteria query renderer.
 * Responsible for providing a [YawnPathProvider] with a `.sql` compiler to render a SQL fragment in a projection.
 */
internal class CriteriaSqlScope<SOURCE : Any>(
    private val context: YawnCompilationContext,
    private val criteria: Criteria,
    private val criteriaQuery: CriteriaQuery,
) : YawnSqlScope<SOURCE> {
    override val YawnPathProvider<SOURCE>.sql: String
        get() {
            val path = generatePath(context)

            val columns = try {
                criteriaQuery.getColumnsUsingProjection(criteria, path)
            } catch (e: HibernateException) {
                throw IllegalStateException(
                    """
                    Could not resolve path "$path" to a column in a SQL projection.
                    If it is on a joined table, make sure the join is part of this query.
                    """.trimIndent(),
                    e,
                )
            }

            check(columns.size == 1) {
                """
                    Path "$path" is a multi-column mapping backed by columns (${columns.joinToString()}),
                    and thus has no single-value substitute into a SQL projection.
                    Reference one of its columns individually instead.
                """.trimIndent()
            }

            return columns.single()
        }
}
