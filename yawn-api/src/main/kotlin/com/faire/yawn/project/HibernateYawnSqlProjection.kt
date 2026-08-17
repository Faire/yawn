package com.faire.yawn.project

import org.hibernate.Criteria
import org.hibernate.criterion.CriteriaQuery

/**
 * A [YawnSqlProjection] that follows Hibernate's own `{alias}` convention for raw SQL projections.
 *
 * **Prefer [ProjectionLeaf.SqlValue], which [ProjectedYawnQueryScope.sqlValue] produces.** This exists to compile
 * the older string-based [ProjectionLeaf.Sql], whose expressions were written against `Projections.sqlProjection`
 * and so expect its substitution behavior. Everything peculiar about that behavior is deliberately confined here.
 *
 * The expression names its own result, so it is emitted verbatim apart from the alias substitution.
 */
internal class HibernateYawnSqlProjection<SOURCE : Any>(
    private val leaf: ProjectionLeaf.Sql<SOURCE>,
) : YawnSqlProjection(leaf.columnAlias, leaf.resultType) {
    override fun renderSql(
        criteria: Criteria,
        criteriaQuery: CriteriaQuery,
    ): String {
        // This substitution is not ours to choose: it reproduces, exactly, what Hibernate does for the raw SQL
        // projections this leaf used to compile to, so that expressions already written against
        // `Projections.sqlProjection` keep behaving identically. Compare `SQLProjection#toSqlString`:
        // https://github.com/hibernate/hibernate-orm/blob/5.6/hibernate-core/src/main/java/org/hibernate/criterion/SQLProjection.java#L41
        //
        //     return StringHelper.replace( sql, "{alias}", criteriaQuery.getSQLAlias( criteria ) );
        //
        // Note it is purely textual and unaware of quoting, so an expression containing a literal `{alias}` —
        // inside a string literal or a JSON path, say — is silently rewritten too. That is Hibernate's
        // behavior, reproduced faithfully rather than fixed, since fixing it here would mean this leaf and
        // `Projections.sqlProjection` disagreeing about the same input.
        return leaf.sqlExpression.replace(TABLE_ALIAS_PLACEHOLDER, criteriaQuery.getSQLAlias(criteria))
    }

    private companion object {
        /**
         * Hibernate's convention, not Yawn's: the token users write in a raw SQL projection where the alias of
         * the table being selected from should go. Its value is fixed by Hibernate, so it cannot be renamed.
         */
        private const val TABLE_ALIAS_PLACEHOLDER = "{alias}"
    }
}
