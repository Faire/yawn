package com.faire.yawn.query

import com.faire.yawn.YawnDef
import com.faire.yawn.YawnTableDef
import com.faire.yawn.query.YawnQueryRestriction.And
import com.faire.yawn.query.YawnQueryRestriction.Between
import com.faire.yawn.query.YawnQueryRestriction.Equals
import com.faire.yawn.query.YawnQueryRestriction.EqualsOrIsNull
import com.faire.yawn.query.YawnQueryRestriction.EqualsProperty
import com.faire.yawn.query.YawnQueryRestriction.GreaterThan
import com.faire.yawn.query.YawnQueryRestriction.GreaterThanOrEqualTo
import com.faire.yawn.query.YawnQueryRestriction.GreaterThanOrEqualToProperty
import com.faire.yawn.query.YawnQueryRestriction.GreaterThanProperty
import com.faire.yawn.query.YawnQueryRestriction.ILike
import com.faire.yawn.query.YawnQueryRestriction.In
import com.faire.yawn.query.YawnQueryRestriction.IsEmpty
import com.faire.yawn.query.YawnQueryRestriction.IsNotEmpty
import com.faire.yawn.query.YawnQueryRestriction.IsNotNull
import com.faire.yawn.query.YawnQueryRestriction.IsNull
import com.faire.yawn.query.YawnQueryRestriction.LessThan
import com.faire.yawn.query.YawnQueryRestriction.LessThanOrEqualTo
import com.faire.yawn.query.YawnQueryRestriction.LessThanOrEqualToProperty
import com.faire.yawn.query.YawnQueryRestriction.LessThanProperty
import com.faire.yawn.query.YawnQueryRestriction.Like
import com.faire.yawn.query.YawnQueryRestriction.Not
import com.faire.yawn.query.YawnQueryRestriction.NotEquals
import com.faire.yawn.query.YawnQueryRestriction.NotEqualsProperty
import com.faire.yawn.query.YawnQueryRestriction.NotIn
import com.faire.yawn.query.YawnQueryRestriction.Or
import org.hibernate.criterion.MatchMode
import org.hibernate.criterion.Restrictions

/**
 * Helper to create [YawnQueryCriterion]s.
 * Yawn's equivalent to Hibernate's [Restrictions].
 */
object YawnRestrictions {
  fun <SOURCE : Any, F> eq(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      value: F & Any,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(Equals(column, value))
  }

  fun <SOURCE : Any, F> eq(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      otherColumn: YawnDef<SOURCE, *>.YawnColumnDef<F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(EqualsProperty(column, otherColumn))
  }

  fun <SOURCE : Any, F> ne(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      value: F & Any,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(NotEquals(column, value))
  }

  fun <SOURCE : Any, F> ne(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      otherColumn: YawnDef<SOURCE, *>.YawnColumnDef<F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(NotEqualsProperty(column, otherColumn))
  }

  fun <SOURCE : Any, F> gt(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      value: F & Any,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(GreaterThan(column, value))
  }

  fun <SOURCE : Any, F> gt(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      otherColumn: YawnDef<SOURCE, *>.YawnColumnDef<F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(GreaterThanProperty(column, otherColumn))
  }

  fun <SOURCE : Any, F> ge(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      value: F & Any,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(GreaterThanOrEqualTo(column, value))
  }

  fun <SOURCE : Any, F> ge(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      otherColumn: YawnDef<SOURCE, *>.YawnColumnDef<F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(GreaterThanOrEqualToProperty(column, otherColumn))
  }

  fun <SOURCE : Any, F> lt(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      value: F & Any,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(LessThan(column, value))
  }

  fun <SOURCE : Any, F> lt(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      otherColumn: YawnDef<SOURCE, *>.YawnColumnDef<F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(LessThanProperty(column, otherColumn))
  }

  fun <SOURCE : Any, F> le(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      value: F & Any,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(LessThanOrEqualTo(column, value))
  }

  fun <SOURCE : Any, F> le(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      otherColumn: YawnDef<SOURCE, *>.YawnColumnDef<F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(LessThanOrEqualToProperty(column, otherColumn))
  }

  fun <SOURCE : Any, F> between(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      lo: F & Any,
      hi: F & Any,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(Between(column, lo, hi))
  }

  fun <SOURCE : Any> not(
      yawnQueryCriterion: YawnQueryCriterion<SOURCE>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(Not(yawnQueryCriterion))
  }

  fun <SOURCE : Any> or(lhs: YawnQueryCriterion<SOURCE>, rhs: YawnQueryCriterion<SOURCE>): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(Or(lhs, rhs))
  }

  fun <SOURCE : Any> or(vararg criterion: YawnQueryCriterion<SOURCE>): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(Or(*criterion))
  }

  fun <SOURCE : Any> and(vararg criterion: YawnQueryCriterion<SOURCE>): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(And(*criterion))
  }

  fun <SOURCE : Any, F : String?> like(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      value: String,
      matchMode: MatchMode = MatchMode.EXACT,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(Like(column, value, matchMode))
  }

  fun <SOURCE : Any, F : String?> iLike(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      value: String,
      matchMode: MatchMode = MatchMode.EXACT,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(ILike(column, value, matchMode))
  }

  fun <SOURCE : Any, F> isNotNull(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(IsNotNull(column))
  }

  fun <SOURCE : Any, F> isNull(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(IsNull(column))
  }

  fun <SOURCE : Any, F> eqOrIsNull(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      value: F,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(EqualsOrIsNull(column, value))
  }

  fun <SOURCE : Any, F> `in`(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      values: Collection<F & Any>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(In(column, values))
  }

  fun <SOURCE : Any, F> notIn(
      column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      values: Collection<F & Any>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(NotIn(column, values))
  }

  fun <SOURCE : Any> isEmpty(
      column: YawnTableDef<SOURCE, *>.JoinColumnDef<*, *>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(IsEmpty(column))
  }

  fun <SOURCE : Any> isNotEmpty(
      column: YawnTableDef<SOURCE, *>.JoinColumnDef<*, *>,
  ): YawnQueryCriterion<SOURCE> {
    return YawnQueryCriterion(IsNotEmpty(column))
  }
}
