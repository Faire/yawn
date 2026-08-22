package com.faire.yawn.project

import org.hibernate.Criteria
import org.hibernate.criterion.CriteriaQuery
import org.hibernate.criterion.Projection
import org.hibernate.type.StandardBasicTypes
import org.hibernate.type.Type
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Date
import kotlin.reflect.KClass

/**
 * Hibernate [Projection] for a single raw SQL value.
 *
 * Yawn implements this rather than calling `Projections.sqlProjection`, because rendering the SQL gives
 * access to [CriteriaQuery] at render time. That is the only place the ORM will resolve an entity
 * property to the physical column(s) backing it, which raw SQL has to name.
 *
 * This encodes the concept of being a single-column projection, including mapping the leaf's Kotlin result
 * type to the ORM's. Subclasses supply [renderSql], and the alias to select it under.
 */
internal abstract class YawnSqlProjection(
    protected val columnAlias: String,
    resultType: KClass<*>,
) : Projection {
    private val type: Type = resultType.toHibernateType()

    /** Builds the select fragment, including its `as` clause. */
    protected abstract fun renderSql(
        criteria: Criteria,
        criteriaQuery: CriteriaQuery,
    ): String

    final override fun toSqlString(
        criteria: Criteria,
        position: Int,
        criteriaQuery: CriteriaQuery,
    ): String = renderSql(criteria, criteriaQuery)

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
        private fun KClass<*>.toHibernateType(): Type = when (this) {
            String::class -> StandardBasicTypes.STRING
            Long::class -> StandardBasicTypes.LONG
            Int::class -> StandardBasicTypes.INTEGER
            Double::class -> StandardBasicTypes.DOUBLE
            Float::class -> StandardBasicTypes.FLOAT
            Boolean::class -> StandardBasicTypes.BOOLEAN
            Short::class -> StandardBasicTypes.SHORT
            Byte::class -> StandardBasicTypes.BYTE
            BigDecimal::class -> StandardBasicTypes.BIG_DECIMAL
            BigInteger::class -> StandardBasicTypes.BIG_INTEGER
            // SQL `date(...)` expressions (and friends) come back from the database as [java.sql.Date].
            Date::class -> StandardBasicTypes.DATE
            else -> error("Unsupported SQL projection result type: $this")
        }
    }
}
