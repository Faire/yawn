package com.faire.yawn.query

import org.hibernate.Criteria
import org.hibernate.criterion.CriteriaQuery
import org.hibernate.criterion.Criterion
import org.hibernate.engine.spi.TypedValue
import org.hibernate.type.StringType

/**
 * A `LIKE` against the column's own SQL text, binding the pattern as a [StringType] instead of letting Hibernate
 * resolve the parameter type from the mapped property.
 *
 * This is what makes pattern matching possible on a column Hibernate maps through an `AttributeConverter`: the
 * standard expressions bind the value as the property's own type, which rejects a `String` pattern outright, and in
 * the case-insensitive variant stringify the value before binding, which a converted column cannot bind either.
 * Resolving the column and binding the pattern ourselves sidesteps both, since the underlying column is text.
 *
 * @param pattern the full pattern, wildcards already applied.
 */
internal class StringPatternCriterion(
    private val propertyPath: String,
    private val pattern: String,
    private val caseInsensitive: Boolean,
) : Criterion {
    override fun toSqlString(criteria: Criteria, criteriaQuery: CriteriaQuery): String {
        val columns = criteriaQuery.getColumnsUsingProjection(criteria, propertyPath)
        if (columns.size != 1) {
            throw UnsupportedOperationException(
                """
                    Pattern matching on $propertyPath is not supported, because it spans ${columns.size} columns:
                    ${columns.joinToString()}.
                    Only a single-column property can be matched as text; match the individual columns instead.
                """.trimIndent(),
            )
        }

        val column = columns.single()
        if (!caseInsensitive) {
            return "$column like ?"
        }

        val lowercase = criteriaQuery.factory.jdbcServices.dialect.lowercaseFunction
        return "$lowercase($column) like $lowercase(?)"
    }

    override fun getTypedValues(criteria: Criteria, criteriaQuery: CriteriaQuery): Array<TypedValue> {
        return arrayOf(TypedValue(StringType.INSTANCE, pattern))
    }
}
