package com.faire.yawn.criteria.query

import com.faire.yawn.YawnDef
import com.faire.yawn.YawnTableDef
import com.faire.yawn.criteria.builder.DetachedProjectedYawnBuilder
import com.faire.yawn.query.YawnQueryCriterion
import com.faire.yawn.query.YawnQueryWithCriterion
import com.faire.yawn.query.YawnRestrictions
import com.faire.yawn.query.YawnSubQueryRestrictions
import org.hibernate.criterion.MatchMode

/**
 * A delegatable interface for Query DSL classes supporting WHERE clauses (via [addEq], etc.).
 * This serves for all implementations of [BaseYawnQueryScope] (just extracted for organization purposes).
 */
sealed interface YawnQueryScopeWithWhere<SOURCE : Any, T : Any> {
    fun provideQuery(): YawnQueryWithCriterion<SOURCE, T>

    fun add(criterion: YawnQueryCriterion<SOURCE>)

    fun <F> addEq(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        value: F & Any,
    ) {
        add(YawnRestrictions.eq(column, value))
    }

    fun <F> addEq(
        column: YawnTableDef<SOURCE, *>.JoinColumnDefWithForeignKey<*, *, F>,
        value: F & Any,
    ) {
        addEq(column.foreignKey, value)
    }

    fun <F> addEq(
        column: YawnTableDef<SOURCE, *>.JoinColumnDefWithForeignKey<*, *, F>,
        otherColumn: YawnTableDef<SOURCE, *>.JoinColumnDefWithForeignKey<*, *, F>,
    ) {
        addEq(column.foreignKey, otherColumn.foreignKey)
    }

    fun <F> addEq(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        otherColumn: YawnDef<SOURCE, *>.YawnColumnDef<F>,
    ) {
        add(YawnRestrictions.eq(column, otherColumn))
    }

    /**
     * Implies that the sub query returns a single result.
     */
    fun <F> addEq(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.eq(column, detachedBuilder))
    }

    fun <F> addEq(
        column: YawnTableDef<SOURCE, *>.JoinColumnDefWithForeignKey<*, *, F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.eq(column.foreignKey, detachedBuilder))
    }

    fun <F> addGt(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        value: F & Any,
    ) {
        add(YawnRestrictions.gt(column, value))
    }

    fun <F> addGt(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        otherColumn: YawnDef<SOURCE, *>.YawnColumnDef<F>,
    ) {
        add(YawnRestrictions.gt(column, otherColumn))
    }

    /**
     * Implies that the sub query returns a single result.
     */
    fun <F> addGt(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.gt(column, detachedBuilder))
    }

    fun <F> addGe(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        value: F & Any,
    ) {
        add(YawnRestrictions.ge(column, value))
    }

    fun <F> addGe(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        otherColumn: YawnDef<SOURCE, *>.YawnColumnDef<F>,
    ) {
        add(YawnRestrictions.ge(column, otherColumn))
    }

    /**
     * Implies that the sub query returns a single result.
     */
    fun <F> addGe(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.ge(column, detachedBuilder))
    }

    fun <F> addLe(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        value: F & Any,
    ) {
        add(YawnRestrictions.le(column, value))
    }

    fun <F> addLe(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        otherColumn: YawnDef<SOURCE, *>.YawnColumnDef<F>,
    ) {
        add(YawnRestrictions.le(column, otherColumn))
    }

    /**
     * Implies that the sub query returns a single result.
     */
    fun <F> addLe(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.le(column, detachedBuilder))
    }

    fun <F> addLt(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        value: F & Any,
    ) {
        add(YawnRestrictions.lt(column, value))
    }

    fun <F> addLt(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        otherColumn: YawnDef<SOURCE, *>.YawnColumnDef<F>,
    ) {
        add(YawnRestrictions.lt(column, otherColumn))
    }

    /**
     * Implies that the sub query returns a single result.
     */
    fun <F> addLt(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.lt(column, detachedBuilder))
    }

    fun <F> addBetween(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        lo: F & Any,
        hi: F & Any,
    ) {
        add(YawnRestrictions.between(column, lo, hi))
    }

    fun addOr(lhs: YawnQueryCriterion<SOURCE>, rhs: YawnQueryCriterion<SOURCE>) {
        add(YawnRestrictions.or(lhs, rhs))
    }

    fun addOr(vararg predicates: YawnQueryCriterion<SOURCE>) {
        add(YawnRestrictions.or(*predicates))
    }

    fun addOr(predicates: List<YawnQueryCriterion<SOURCE>>) {
        add(YawnRestrictions.or(predicates))
    }

    /**
     * Adds an OR condition with nullable predicates, automatically filtering out null values.
     * Equivalent to `addOr(listOfNotNull(criteria1, criteria2, ...))` but more concise.
     *
     * Use case: When building dynamic queries where some criteria may be conditionally null.
     * Instead of manually filtering nulls with `addOr(listOfNotNull(...))`, you can pass
     * nullable criteria directly and let this method handle the filtering.
     *
     * Note: If all predicates are null (resulting in an empty list), no condition is added to the query.
     */
    fun addOrOfNotNull(vararg predicates: YawnQueryCriterion<SOURCE>?) {
        val eligiblePredicates = predicates.filterNotNull()
        if (eligiblePredicates.isEmpty()) return

        add(YawnRestrictions.or(eligiblePredicates))
    }

    fun addAnd(lhs: YawnQueryCriterion<SOURCE>, rhs: YawnQueryCriterion<SOURCE>) {
        add(YawnRestrictions.and(lhs, rhs))
    }

    fun addAnd(vararg predicates: YawnQueryCriterion<SOURCE>) {
        add(YawnRestrictions.and(*predicates))
    }

    fun addAnd(predicates: List<YawnQueryCriterion<SOURCE>>) {
        add(YawnRestrictions.and(predicates))
    }

    /**
     * Adds an AND condition with nullable predicates, automatically filtering out null values.
     * Equivalent to `addAnd(listOfNotNull(criteria1, criteria2, ...))` but more concise.
     *
     * Use case: When building dynamic queries where some criteria may be conditionally null.
     * Instead of manually filtering nulls with `addAnd(listOfNotNull(...))`, you can pass
     * nullable criteria directly and let this method handle the filtering.
     *
     * Note: If all predicates are null (resulting in an empty list), no condition is added to the query.
     */
    fun addAndOfNotNull(vararg predicates: YawnQueryCriterion<SOURCE>?) {
        val eligiblePredicates = predicates.filterNotNull()
        if (eligiblePredicates.isEmpty()) return

        add(YawnRestrictions.and(eligiblePredicates))
    }

    fun <F> addNotEq(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        value: F & Any,
    ) {
        add(YawnRestrictions.ne(column, value))
    }

    fun <F> addNotEq(
        column: YawnTableDef<SOURCE, *>.JoinColumnDefWithForeignKey<*, *, F>,
        value: F & Any,
    ) {
        add(YawnRestrictions.ne(column.foreignKey, value))
    }

    /**
     * Implies that the sub query returns a single result.
     */
    fun <F> addNotEq(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.ne(column, detachedBuilder))
    }

    fun <F> addNotEq(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        otherColumn: YawnDef<SOURCE, *>.YawnColumnDef<F>,
    ) {
        add(YawnRestrictions.ne(column, otherColumn))
    }

    // TODO(yawn): addCaseInsensitiveNotEq
    // TODO(yawn): addCaseInsensitiveEq

    fun <F : String?> addLike(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        value: F & Any,
        matchMode: MatchMode = MatchMode.EXACT,
    ) {
        add(YawnRestrictions.like(column, value, matchMode))
    }

    fun <F : String?> addILike(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        value: F & Any,
        matchMode: MatchMode = MatchMode.EXACT,
    ) {
        add(YawnRestrictions.iLike(column, value, matchMode))
    }

    fun <F : String?> addNotLike(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        value: F & Any,
        matchMode: MatchMode = MatchMode.EXACT,
    ) {
        add(YawnRestrictions.not(YawnRestrictions.like(column, value, matchMode)))
    }

    fun <F : String?> addNotILike(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        value: F & Any,
        matchMode: MatchMode = MatchMode.EXACT,
    ) {
        add(YawnRestrictions.not(YawnRestrictions.iLike(column, value, matchMode)))
    }

    fun <F> addIsNotNull(
        column: YawnTableDef<SOURCE, *>.JoinColumnDefWithForeignKey<*, *, F>,
    ): YawnDef<SOURCE, *>.YawnColumnDef<F & Any> {
        return addIsNotNull(column.foreignKey)
    }

    fun <F> addIsNotNull(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
    ): YawnDef<SOURCE, *>.YawnColumnDef<F & Any> {
        add(YawnRestrictions.isNotNull(column))
        @Suppress("UNCHECKED_CAST")
        return column as YawnDef<SOURCE, *>.YawnColumnDef<F & Any>
    }

    fun <F> addIsNull(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
    ) {
        add(YawnRestrictions.isNull(column))
    }

    fun <F> addEqOrIsNull(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        value: F,
    ) {
        add(YawnRestrictions.eqOrIsNull(column, value))
    }

    // TODO(yawn): doesn't have any of our custom stuff that checks large IN clause
    fun <F> addIn(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        values: Collection<F & Any>,
    ) {
        add(YawnRestrictions.`in`(column, values))
    }

    fun <F> addIn(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        vararg collection: F & Any,
    ) {
        add(YawnRestrictions.`in`(column, collection.toSet()))
    }

    fun <F : Any> addIn(
        column: YawnTableDef<SOURCE, *>.JoinColumnDefWithForeignKey<*, *, F>,
        values: Collection<F>,
    ) {
        add(YawnRestrictions.`in`(column.foreignKey, values))
    }

    fun <F> addIn(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.`in`(column, detachedBuilder))
    }

    fun <F : Any> addIn(
        column: YawnTableDef<SOURCE, *>.JoinColumnDefWithForeignKey<*, *, F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.`in`(column.foreignKey, detachedBuilder))
    }

    fun <F> addNotIn(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        vararg collection: F & Any,
    ) {
        add(YawnRestrictions.notIn(column, collection.toSet()))
    }

    fun <F> addNotIn(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        values: Collection<F & Any>,
    ) {
        add(YawnRestrictions.notIn(column, values))
    }

    fun <F> addNotIn(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.notIn(column, detachedBuilder))
    }

    fun <F> addExists(
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.exists(detachedBuilder))
    }

    fun <F> addNotExists(
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.notExists(detachedBuilder))
    }

    fun <F> addEqAll(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.eqAll(column, detachedBuilder))
    }

    fun <F> addGeAll(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.geAll(column, detachedBuilder))
    }

    fun <F> addGeSome(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.geSome(column, detachedBuilder))
    }

    fun <F> addGtAll(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.gtAll(column, detachedBuilder))
    }

    fun <F> addGtSome(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.gtSome(column, detachedBuilder))
    }

    fun <F> addLeAll(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.leAll(column, detachedBuilder))
    }

    fun <F> addLeSome(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.leSome(column, detachedBuilder))
    }

    fun <F> addLtAll(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.ltAll(column, detachedBuilder))
    }

    fun <F> addLtSome(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.ltSome(column, detachedBuilder))
    }

    fun addIsEmpty(
        definition: YawnTableDef<SOURCE, *>.JoinColumnDef<*, *>,
    ) {
        add(YawnRestrictions.isEmpty(definition))
    }

    fun addIsNotEmpty(
        definition: YawnTableDef<SOURCE, *>.JoinColumnDef<*, *>,
    ) {
        add(YawnRestrictions.isNotEmpty(definition))
    }
}

internal class YawnQueryScopeWithWhereDelegate<SOURCE : Any, T : Any>(
    private val query: YawnQueryWithCriterion<SOURCE, T>,
) : YawnQueryScopeWithWhere<SOURCE, T> {
    override fun provideQuery(): YawnQueryWithCriterion<SOURCE, T> = query

    override fun add(criterion: YawnQueryCriterion<SOURCE>) {
        query.addCriterion(criterion)
    }
}
