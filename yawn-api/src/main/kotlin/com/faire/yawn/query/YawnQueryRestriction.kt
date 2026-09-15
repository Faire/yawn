package com.faire.yawn.query

import com.faire.yawn.YawnDef
import com.faire.yawn.YawnTableDef
import org.hibernate.criterion.Criterion
import org.hibernate.criterion.MatchMode
import org.hibernate.criterion.Restrictions

interface YawnQueryRestriction<SOURCE : Any> {
    interface YawnQueryRestrictionWithNestedRestriction<SOURCE : Any> : YawnQueryRestriction<SOURCE> {
        val criteria: List<YawnQueryCriterion<SOURCE>>
    }

    fun compile(context: YawnCompilationContext): Criterion

    class Equals<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        private val value: F & Any,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.eq(property.generatePath(context), property.adaptValue(value))
    }

    class EqualsProperty<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        private val otherProperty: YawnDef<SOURCE, *>.YawnColumnDef<F>,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.eqProperty(property.generatePath(context), otherProperty.generatePath(context))
    }

    class NotEquals<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        private val value: F & Any,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.ne(property.generatePath(context), property.adaptValue(value))
    }

    class NotEqualsProperty<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        private val otherProperty: YawnDef<SOURCE, *>.YawnColumnDef<F>,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.neProperty(property.generatePath(context), otherProperty.generatePath(context))
    }

    class GreaterThan<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        private val value: F & Any,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.gt(property.generatePath(context), property.adaptValue(value))
    }

    class GreaterThanProperty<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        private val otherProperty: YawnDef<SOURCE, *>.YawnColumnDef<F>,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.gtProperty(property.generatePath(context), otherProperty.generatePath(context))
    }

    class GreaterThanOrEqualTo<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        private val value: F & Any,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.ge(property.generatePath(context), property.adaptValue(value))
    }

    class GreaterThanOrEqualToProperty<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        private val otherProperty: YawnDef<SOURCE, *>.YawnColumnDef<F>,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.geProperty(property.generatePath(context), otherProperty.generatePath(context))
    }

    class LessThan<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        private val value: F & Any,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.lt(property.generatePath(context), property.adaptValue(value))
    }

    class LessThanProperty<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        private val otherProperty: YawnDef<SOURCE, *>.YawnColumnDef<F>,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.ltProperty(property.generatePath(context), otherProperty.generatePath(context))
    }

    class LessThanOrEqualTo<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        private val value: F & Any,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.le(property.generatePath(context), property.adaptValue(value))
    }

    class LessThanOrEqualToProperty<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        private val otherProperty: YawnDef<SOURCE, *>.YawnColumnDef<F>,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.leProperty(property.generatePath(context), otherProperty.generatePath(context))
    }

    class Between<SOURCE : Any, F>(
        private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        private val lo: F & Any,
        private val hi: F & Any,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.between(
            property.generatePath(context),
            property.adaptValue(lo),
            property.adaptValue(hi),
        )
    }

    class Not<SOURCE : Any>(
        private val criterion: YawnQueryCriterion<SOURCE>,
    ) : YawnQueryRestrictionWithNestedRestriction<SOURCE> {
        override val criteria: List<YawnQueryCriterion<SOURCE>> = listOf(criterion)

        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.not(criterion.yawnRestriction.compile(context))
    }

    class Or<SOURCE : Any>(
        override val criteria: List<YawnQueryCriterion<SOURCE>>,
    ) : YawnQueryRestrictionWithNestedRestriction<SOURCE> {
        internal constructor(vararg criteria: YawnQueryCriterion<SOURCE>) : this(criteria.toList())

        override fun compile(context: YawnCompilationContext): Criterion = Restrictions.or(
            *criteria.map { it.yawnRestriction.compile(context) }.toTypedArray(),
        )
    }

    class And<SOURCE : Any>(
        override val criteria: List<YawnQueryCriterion<SOURCE>>,
    ) : YawnQueryRestrictionWithNestedRestriction<SOURCE> {
        constructor(vararg criteria: YawnQueryCriterion<SOURCE>) : this(criteria.toList())

        override fun compile(context: YawnCompilationContext): Criterion = Restrictions.and(
            *criteria.map { it.yawnRestriction.compile(context) }.toTypedArray(),
        )
    }

    class Like<SOURCE : Any, F>(
        private val column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        private val value: F & Any,
        private val matchMode: MatchMode,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion {
            val path = column.generatePath(context)
            return when (val adaptedValue = column.adaptNonNullValue(value)) {
                is String -> Restrictions.like(path, adaptedValue, matchMode)
                // The column is mapped by Hibernate itself (e.g. through an AttributeConverter), so the value has to
                // be bound as the column's own type; Hibernate cannot build the pattern out of it for us.
                else -> Restrictions.like(path, column.requireExactMatchMode(adaptedValue, matchMode))
            }
        }
    }

    class ILike<SOURCE : Any, F>(
        private val column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        private val value: F & Any,
        private val matchMode: MatchMode,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion {
            val path = column.generatePath(context)
            return when (val adaptedValue = column.adaptNonNullValue(value)) {
                is String -> Restrictions.ilike(path, adaptedValue, matchMode)
                else -> throw UnsupportedOperationException(
                    """
                        iLike is not supported on column $column, whose value adapts to
                        ${adaptedValue.javaClass.name} rather than a String.
                        Hibernate's IlikeExpression stringifies the bound value, which then fails to bind against a
                        column mapped through an AttributeConverter.
                        Use `like` with the wildcards embedded in the value instead, or map the underlying String
                        column and use `iLike` on that.
                    """.trimIndent(),
                )
            }
        }
    }

    class IsNotNull<SOURCE : Any, F>(
        private val column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.isNotNull(column.generatePath(context))
    }

    class IsNull<SOURCE : Any, F>(
        private val column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.isNull(column.generatePath(context))
    }

    class EqualsOrIsNull<SOURCE : Any, F>(
        private val column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        private val value: F,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.eqOrIsNull(column.generatePath(context), column.adaptValue(value))
    }

    class In<SOURCE : Any, F>(
        private val column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        private val values: Collection<F & Any>,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(context: YawnCompilationContext): Criterion {
            return if (values.isEmpty()) {
                Restrictions.sqlRestriction("0=1")
            } else {
                Restrictions.`in`(column.generatePath(context), values.map { column.adaptValue(it) })
            }
        }
    }

    class NotIn<SOURCE : Any, F>(
        private val column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        private val values: Collection<F & Any>,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(context: YawnCompilationContext): Criterion {
            return if (values.isEmpty()) {
                Restrictions.sqlRestriction("1=1")
            } else {
                Restrictions.not(Restrictions.`in`(column.generatePath(context), values.map { column.adaptValue(it) }))
            }
        }
    }

    class IsEmpty<SOURCE : Any>(
        private val joinColumn: YawnTableDef<SOURCE, *>.JoinColumnDef<*, *>,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.isEmpty(joinColumn.generatePath(context))
    }

    class IsNotEmpty<SOURCE : Any>(
        private val joinColumn: YawnTableDef<SOURCE, *>.JoinColumnDef<*, *>,
    ) : YawnQueryRestriction<SOURCE> {
        override fun compile(
            context: YawnCompilationContext,
        ): Criterion = Restrictions.isNotEmpty(joinColumn.generatePath(context))
    }
}

/**
 * Adapts [value] for binding, failing loudly if an adapter turned a non-null value into `null`.
 *
 * Pattern matching always needs a value to bind, so a `null` here can only mean a broken adapter in the metamodel.
 */
private fun <SOURCE : Any, F> YawnDef<SOURCE, *>.YawnColumnDef<F>.adaptNonNullValue(value: F & Any): Any {
    return checkNotNull(adaptValue(value)) {
        """
            The adapter on column $this turned a non-null value into null.
            This means a wrong adapter was code-generated into the metamodel.
            Please open an issue on GitHub with your schema definition.
        """.trimIndent()
    }
}

/**
 * Wildcards cannot be injected into a value that is bound as the column's own type, so [MatchMode] is only supported
 * for columns that adapt down to a [String].
 */
private fun <SOURCE : Any, F> YawnDef<SOURCE, *>.YawnColumnDef<F>.requireExactMatchMode(
    adaptedValue: Any,
    matchMode: MatchMode,
): Any {
    if (matchMode != MatchMode.EXACT) {
        throw UnsupportedOperationException(
            """
                MatchMode.$matchMode is not supported on column $this, whose value adapts to
                ${adaptedValue.javaClass.name} rather than a String.
                Hibernate binds this value as the column's own type, so Yawn cannot wrap it in wildcards for you.
                Embed the wildcards in the value itself and use MatchMode.EXACT instead,
                e.g. like(column, EmailAddress("%@example.com")).
            """.trimIndent(),
        )
    }

    return adaptedValue
}
