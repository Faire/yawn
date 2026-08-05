package com.faire.yawn.project

import org.hibernate.Criteria
import org.hibernate.criterion.CriteriaQuery
import org.hibernate.criterion.Projection
import org.hibernate.type.Type

/**
 * Hibernate [Projection] for a single raw SQL value.
 *
 * Yawn implements this rather than calling `Projections.sqlProjection`, because rendering the SQL ourselves is
 * what gives us access to [CriteriaQuery] at render time. That is the only place the ORM will resolve an entity
 * property to the physical column(s) backing it, which raw SQL has to name.
 *
 * This renders exactly what Hibernate's own SQL projection does: [sqlExpression] verbatim, with `{alias}`
 * substituted, selected under [columnAlias]. The expression is therefore expected to name its own result.
 */
internal class YawnSqlProjection(
    private val sqlExpression: String,
    private val columnAlias: String,
    private val type: Type,
) : Projection {
    override fun toSqlString(
        criteria: Criteria,
        position: Int,
        criteriaQuery: CriteriaQuery,
    ): String {
        // `{alias}` is substituted with the SQL alias of the table this criteria selects from, matching how
        // Hibernate's own SQL projections behave.
        return sqlExpression.replace(TABLE_ALIAS_PLACEHOLDER, criteriaQuery.getSQLAlias(criteria))
    }

    override fun toGroupSqlString(
        criteria: Criteria,
        criteriaQuery: CriteriaQuery,
    ): String = ""

    override fun getTypes(
        criteria: Criteria,
        criteriaQuery: CriteriaQuery,
    ): Array<Type> = arrayOf(type)

    /** Only meaningful for projections addressable by a user-facing alias, which this is not. */
    override fun getTypes(
        alias: String?,
        criteria: Criteria,
        criteriaQuery: CriteriaQuery,
    ): Array<Type>? = null

    override fun getColumnAliases(position: Int): Array<String> = arrayOf(columnAlias)

    /** Only meaningful for projections addressable by a user-facing alias, which this is not. */
    override fun getColumnAliases(
        alias: String?,
        position: Int,
    ): Array<String>? = null

    override fun getAliases(): Array<String> = arrayOf(columnAlias)

    override fun isGrouped(): Boolean = false

    private companion object {
        private const val TABLE_ALIAS_PLACEHOLDER = "{alias}"
    }
}
