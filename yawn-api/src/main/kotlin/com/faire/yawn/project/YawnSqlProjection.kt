package com.faire.yawn.project

import com.faire.yawn.query.YawnCompilationContext
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
 * Hibernate [Projection] for a single raw SQL value, as
 * [com.faire.yawn.criteria.query.ProjectedYawnQueryScope.sqlValue] produces.
 *
 * Yawn implements this rather than calling `Projections.sqlProjection`, because rendering the SQL gives access to
 * [CriteriaQuery] at render time. That is the only place the ORM will resolve an entity property to the physical
 * column(s) backing it, which raw SQL has to name, and it is what [YawnSqlScope.sql] is built on.
 *
 * Yawn also owns the result alias. The expression a caller writes is bare, and is selected under a name taken from
 * the compilation [context] and unique within it, so two SQL values in one query can never be read from the same
 * column and the caller never has to invent a name.
 *
 * So `sqlValue<Long> { "SUM(${'$'}{books.numberOfPages.sql})" }` renders as `SUM(this_.numberOfPages) as _yawn_ct0`.
 */
internal class YawnSqlProjection<SOURCE : Any>(
    private val context: YawnCompilationContext,
    private val leaf: ProjectionLeaf.SqlValue<SOURCE>,
) : Projection {
    private val columnAlias: String = context.generateResultAlias()
    private val type: Type = leaf.resultType.toHibernateType()

    override fun toSqlString(
        criteria: Criteria,
        position: Int,
        criteriaQuery: CriteriaQuery,
    ): String {
        val scope = CriteriaSqlScope<SOURCE>(context, criteria, criteriaQuery)
        return "${leaf.render(scope)} as $columnAlias"
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
