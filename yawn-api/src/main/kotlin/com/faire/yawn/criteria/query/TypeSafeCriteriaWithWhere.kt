package com.faire.yawn.criteria.query

import com.faire.yawn.YawnDef
import com.faire.yawn.YawnTableDef
import com.faire.yawn.criteria.builder.DetachedProjectedTypeSafeCriteriaBuilder
import com.faire.yawn.query.YawnCriteriaQuery
import com.faire.yawn.query.YawnQueryCriterion
import com.faire.yawn.query.YawnRestrictions
import com.faire.yawn.query.YawnSubQueryRestrictions
import org.hibernate.criterion.MatchMode

/**
 * A type-safe Yawn queries DSL that supports where filters (addEq, etc.).
 * This serves for all implementations of [BaseTypeSafeCriteriaQuery] (just extracted for organization).
 */
sealed interface TypeSafeCriteriaWithWhere<SOURCE : Any, T : Any> {
    fun provideQuery(): YawnCriteriaQuery<SOURCE, T>

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
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.eq(column, detachedProjectedTypeSafeCriteriaBuilder))
    }

    fun <F> addEq(
        column: YawnTableDef<SOURCE, *>.JoinColumnDefWithForeignKey<*, *, F>,
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.eq(column.foreignKey, detachedProjectedTypeSafeCriteriaBuilder))
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
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.gt(column, detachedProjectedTypeSafeCriteriaBuilder))
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
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.ge(column, detachedProjectedTypeSafeCriteriaBuilder))
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
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.le(column, detachedProjectedTypeSafeCriteriaBuilder))
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
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.lt(column, detachedProjectedTypeSafeCriteriaBuilder))
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
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.ne(column, detachedProjectedTypeSafeCriteriaBuilder))
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
        value: String,
        matchMode: MatchMode = MatchMode.EXACT,
    ) {
        add(YawnRestrictions.like(column, value, matchMode))
    }

    fun <F : String?> addILike(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        value: String,
        matchMode: MatchMode = MatchMode.EXACT,
    ) {
        add(YawnRestrictions.iLike(column, value, matchMode))
    }

    fun <F : String?> addNotLike(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        value: String,
        matchMode: MatchMode = MatchMode.EXACT,
    ) {
        add(YawnRestrictions.not(YawnRestrictions.like(column, value, matchMode)))
    }

    fun <F : String?> addNotILike(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        value: String,
        matchMode: MatchMode = MatchMode.EXACT,
    ) {
        add(YawnRestrictions.not(YawnRestrictions.iLike(column, value, matchMode)))
    }

    fun <F> addIsNotNull(
        column: YawnTableDef<SOURCE, *>.JoinColumnDefWithForeignKey<*, *, F>,
    ) {
        addIsNotNull(column.foreignKey)
    }

    fun <F> addIsNotNull(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
    ) {
        add(YawnRestrictions.isNotNull(column))
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
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.`in`(column, detachedProjectedTypeSafeCriteriaBuilder))
    }

    fun <F : Any> addIn(
        column: YawnTableDef<SOURCE, *>.JoinColumnDefWithForeignKey<*, *, F>,
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.`in`(column.foreignKey, detachedProjectedTypeSafeCriteriaBuilder))
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
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.notIn(column, detachedProjectedTypeSafeCriteriaBuilder))
    }

    fun <F> addExists(
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.exists(detachedProjectedTypeSafeCriteriaBuilder))
    }

    fun <F> addNotExists(
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.notExists(detachedProjectedTypeSafeCriteriaBuilder))
    }

    fun <F> addEqAll(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.eqAll(column, detachedProjectedTypeSafeCriteriaBuilder))
    }

    fun <F> addGeAll(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.geAll(column, detachedProjectedTypeSafeCriteriaBuilder))
    }

    fun <F> addGeSome(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.geSome(column, detachedProjectedTypeSafeCriteriaBuilder))
    }

    fun <F> addGtAll(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.gtAll(column, detachedProjectedTypeSafeCriteriaBuilder))
    }

    fun <F> addGtSome(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.gtSome(column, detachedProjectedTypeSafeCriteriaBuilder))
    }

    fun <F> addLeAll(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.leAll(column, detachedProjectedTypeSafeCriteriaBuilder))
    }

    fun <F> addLeSome(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.leSome(column, detachedProjectedTypeSafeCriteriaBuilder))
    }

    fun <F> addLtAll(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.ltAll(column, detachedProjectedTypeSafeCriteriaBuilder))
    }

    fun <F> addLtSome(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
    ) {
        add(YawnSubQueryRestrictions.ltSome(column, detachedProjectedTypeSafeCriteriaBuilder))
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

internal class TypeSafeCriteriaWithWhereDelegate<SOURCE : Any, T : Any>(
    private val query: YawnCriteriaQuery<SOURCE, T>,
) : TypeSafeCriteriaWithWhere<SOURCE, T> {
    override fun provideQuery(): YawnCriteriaQuery<SOURCE, T> = query

    override fun add(criterion: YawnQueryCriterion<SOURCE>) {
        query.addCriterion(criterion)
    }
}
