package com.faire.yawn.query

import com.faire.yawn.YawnDef
import com.faire.yawn.criteria.builder.DetachedProjectedYawnBuilder
import com.faire.yawn.query.YawnDetachedQueryRestriction.EqualsAllDetached
import com.faire.yawn.query.YawnDetachedQueryRestriction.EqualsDetached
import com.faire.yawn.query.YawnDetachedQueryRestriction.ExistsDetached
import com.faire.yawn.query.YawnDetachedQueryRestriction.GreaterThanAllDetached
import com.faire.yawn.query.YawnDetachedQueryRestriction.GreaterThanDetached
import com.faire.yawn.query.YawnDetachedQueryRestriction.GreaterThanOrEqualToAllDetached
import com.faire.yawn.query.YawnDetachedQueryRestriction.GreaterThanOrEqualToDetached
import com.faire.yawn.query.YawnDetachedQueryRestriction.GreaterThanOrEqualToSomeDetached
import com.faire.yawn.query.YawnDetachedQueryRestriction.GreaterThanSomeDetached
import com.faire.yawn.query.YawnDetachedQueryRestriction.InDetached
import com.faire.yawn.query.YawnDetachedQueryRestriction.LessThanAllDetached
import com.faire.yawn.query.YawnDetachedQueryRestriction.LessThanDetached
import com.faire.yawn.query.YawnDetachedQueryRestriction.LessThanOrEqualToAllDetached
import com.faire.yawn.query.YawnDetachedQueryRestriction.LessThanOrEqualToDetached
import com.faire.yawn.query.YawnDetachedQueryRestriction.LessThanOrEqualToSomeDetached
import com.faire.yawn.query.YawnDetachedQueryRestriction.LessThanSomeDetached
import com.faire.yawn.query.YawnDetachedQueryRestriction.NotEqualsDetached
import com.faire.yawn.query.YawnDetachedQueryRestriction.NotExistsDetached
import com.faire.yawn.query.YawnDetachedQueryRestriction.NotInDetached
import org.hibernate.criterion.Subqueries

/**
 * Helper to create [YawnQueryCriterion]s for sub queries.
 * Equivalent to Hibernate's [Subqueries].
 */
object YawnSubQueryRestrictions {
    // TODO(yawn): Decouple these from Hibernate

    fun <SOURCE : Any, F> eq(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(EqualsDetached(column, detachedBuilder))
    }

    fun <SOURCE : Any, F> ne(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(NotEqualsDetached(column, detachedBuilder))
    }

    fun <SOURCE : Any, F> gt(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(GreaterThanDetached(column, detachedBuilder))
    }

    fun <SOURCE : Any, F> ge(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(GreaterThanOrEqualToDetached(column, detachedBuilder))
    }

    fun <SOURCE : Any, F> le(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(LessThanOrEqualToDetached(column, detachedBuilder))
    }

    fun <SOURCE : Any, F> lt(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(LessThanDetached(column, detachedBuilder))
    }

    fun <SOURCE : Any, F> `in`(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(InDetached(column, detachedBuilder))
    }

    fun <SOURCE : Any, F> notIn(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(NotInDetached(column, detachedBuilder))
    }

    fun <SOURCE : Any, F> exists(
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(ExistsDetached(detachedBuilder))
    }

    fun <SOURCE : Any, F> notExists(
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(NotExistsDetached(detachedBuilder))
    }

    fun <SOURCE : Any, F> eqAll(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(EqualsAllDetached(column, detachedBuilder))
    }

    fun <SOURCE : Any, F> geAll(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(GreaterThanOrEqualToAllDetached(column, detachedBuilder))
    }

    fun <SOURCE : Any, F> geSome(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(GreaterThanOrEqualToSomeDetached(column, detachedBuilder))
    }

    fun <SOURCE : Any, F> gtAll(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(GreaterThanAllDetached(column, detachedBuilder))
    }

    fun <SOURCE : Any, F> gtSome(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(GreaterThanSomeDetached(column, detachedBuilder))
    }

    fun <SOURCE : Any, F> leAll(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(LessThanOrEqualToAllDetached(column, detachedBuilder))
    }

    fun <SOURCE : Any, F> leSome(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(LessThanOrEqualToSomeDetached(column, detachedBuilder))
    }

    fun <SOURCE : Any, F> ltAll(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(LessThanAllDetached(column, detachedBuilder))
    }

    fun <SOURCE : Any, F> ltSome(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        detachedBuilder: DetachedProjectedYawnBuilder<*, *, *, F>,
    ): YawnQueryCriterion<SOURCE> {
        return YawnQueryCriterion(LessThanSomeDetached(column, detachedBuilder))
    }

    // TODO(yawn): Support properties... functions using a data class projection
}
