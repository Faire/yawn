package com.faire.yawn.query

import com.faire.yawn.YawnDef
import com.faire.yawn.criteria.builder.DetachedProjectedTypeSafeCriteriaBuilder
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

  fun <SOURCE : Any, F : Any?> eq(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(EqualsDetached(column, detachedProjectedTypeSafeCriteriaBuilder))
  }

  fun <SOURCE : Any, F : Any?> ne(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(NotEqualsDetached(column, detachedProjectedTypeSafeCriteriaBuilder))
  }

  fun <SOURCE : Any, F : Any?> gt(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(GreaterThanDetached(column, detachedProjectedTypeSafeCriteriaBuilder))
  }

  fun <SOURCE : Any, F : Any?> ge(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(GreaterThanOrEqualToDetached(column, detachedProjectedTypeSafeCriteriaBuilder))
  }

  fun <SOURCE : Any, F : Any?> le(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(LessThanOrEqualToDetached(column, detachedProjectedTypeSafeCriteriaBuilder))
  }

  fun <SOURCE : Any, F : Any?> lt(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(LessThanDetached(column, detachedProjectedTypeSafeCriteriaBuilder))
  }

  fun <SOURCE : Any, F : Any?> `in`(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(InDetached(column, detachedProjectedTypeSafeCriteriaBuilder))
  }

  fun <SOURCE : Any, F : Any?> notIn(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(NotInDetached(column, detachedProjectedTypeSafeCriteriaBuilder))
  }

  fun <SOURCE : Any, F : Any?> exists(
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(ExistsDetached(detachedProjectedTypeSafeCriteriaBuilder))
  }

  fun <SOURCE : Any, F : Any?> notExists(
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(NotExistsDetached(detachedProjectedTypeSafeCriteriaBuilder))
  }

  fun <SOURCE : Any, F : Any?> eqAll(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(EqualsAllDetached(column, detachedProjectedTypeSafeCriteriaBuilder))
  }

  fun <SOURCE : Any, F : Any?> geAll(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(GreaterThanOrEqualToAllDetached(column, detachedProjectedTypeSafeCriteriaBuilder))
  }

  fun <SOURCE : Any, F : Any?> geSome(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(GreaterThanOrEqualToSomeDetached(column, detachedProjectedTypeSafeCriteriaBuilder))
  }

  fun <SOURCE : Any, F : Any?> gtAll(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(GreaterThanAllDetached(column, detachedProjectedTypeSafeCriteriaBuilder))
  }

  fun <SOURCE : Any, F : Any?> gtSome(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(GreaterThanSomeDetached(column, detachedProjectedTypeSafeCriteriaBuilder))
  }

  fun <SOURCE : Any, F : Any?> leAll(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(LessThanOrEqualToAllDetached(column, detachedProjectedTypeSafeCriteriaBuilder))
  }

  fun <SOURCE : Any, F : Any?> leSome(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(LessThanOrEqualToSomeDetached(column, detachedProjectedTypeSafeCriteriaBuilder))
  }

  fun <SOURCE : Any, F : Any?> ltAll(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(LessThanAllDetached(column, detachedProjectedTypeSafeCriteriaBuilder))
  }

  fun <SOURCE : Any, F : Any?> ltSome(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      detachedProjectedTypeSafeCriteriaBuilder: DetachedProjectedTypeSafeCriteriaBuilder<*, *, *, F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(LessThanSomeDetached(column, detachedProjectedTypeSafeCriteriaBuilder))
  }

  // TODO(yawn): Support properties... functions using a data class projection
}
