package com.faire.yawn.query

import com.faire.yawn.YawnDef
import com.faire.yawn.YawnTableDef
import org.hibernate.criterion.Criterion
import org.hibernate.criterion.MatchMode
import org.hibernate.criterion.Restrictions

interface YawnQueryRestriction<SOURCE : Any> {
  fun compile(context: YawnCompilationContext): Criterion

  class Equals<SOURCE : Any, F>(
      private val property: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      private val value: F & Any,
  ) : YawnQueryRestriction<SOURCE> {
    override fun compile(
        context: YawnCompilationContext,
    ): Criterion = Restrictions.eq(property.generatePath(context), value)
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
    ): Criterion = Restrictions.ne(property.generatePath(context), value)
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
    ): Criterion = Restrictions.gt(property.generatePath(context), value)
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
    ): Criterion = Restrictions.ge(property.generatePath(context), value)
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
    ): Criterion = Restrictions.lt(property.generatePath(context), value)
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
    ): Criterion = Restrictions.le(property.generatePath(context), value)
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
    ): Criterion = Restrictions.between(property.generatePath(context), lo, hi)
  }

  class Not<SOURCE : Any>(
      private val criterion: YawnQueryCriterion<SOURCE>,
  ) : YawnQueryRestriction<SOURCE> {
    override fun compile(
        context: YawnCompilationContext,
    ): Criterion = Restrictions.not(criterion.yawnRestriction.compile(context))
  }

  class Or<SOURCE : Any>(
      private val criteria: List<YawnQueryCriterion<SOURCE>>,
  ) : YawnQueryRestriction<SOURCE> {
    internal constructor(vararg criteria: YawnQueryCriterion<SOURCE>) : this(criteria.toList())

    override fun compile(context: YawnCompilationContext): Criterion = Restrictions.or(
        *criteria.map {
        it.yawnRestriction.compile(context)
    }.toTypedArray(),
    )
  }

  class And<SOURCE : Any>(
      private val criteria: List<YawnQueryCriterion<SOURCE>>,
  ) : YawnQueryRestriction<SOURCE> {
    constructor(vararg criteria: YawnQueryCriterion<SOURCE>) : this(criteria.toList())

    override fun compile(context: YawnCompilationContext): Criterion = Restrictions.and(
        *criteria.map {
        it.yawnRestriction.compile(context)
    }.toTypedArray(),
    )
  }

  class Like<SOURCE : Any, F : String?>(
      private val column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      private val value: String,
      private val matchMode: MatchMode,
  ) : YawnQueryRestriction<SOURCE> {
    override fun compile(
        context: YawnCompilationContext,
    ): Criterion = Restrictions.like(column.generatePath(context), value, matchMode)
  }

  class ILike<SOURCE : Any, F : String?>(
      private val column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      private val value: String,
      private val matchMode: MatchMode,
  ) : YawnQueryRestriction<SOURCE> {
    override fun compile(
        context: YawnCompilationContext,
    ): Criterion = Restrictions.ilike(column.generatePath(context), value, matchMode)
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
    ): Criterion = Restrictions.eqOrIsNull(column.generatePath(context), value)
  }

  class In<SOURCE : Any, F>(
      private val column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
      private val values: Collection<F & Any>,
  ) : YawnQueryRestriction<SOURCE> {
    override fun compile(context: YawnCompilationContext): Criterion {
      return if (values.isEmpty()) {
        Restrictions.sqlRestriction("0=1")
      } else {
        Restrictions.`in`(column.generatePath(context), values)
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
        Restrictions.not(Restrictions.`in`(column.generatePath(context), values))
      }
    }
  }

  class IsEmpty<SOURCE : Any>(
      private val joinColumn: YawnTableDef<SOURCE, *>.JoinColumnDef<*, *>,
  ) : YawnQueryRestriction<SOURCE> {
    override fun compile(
        context: YawnCompilationContext,
    ): Criterion = Restrictions.isEmpty(joinColumn.path(context))
  }

  class IsNotEmpty<SOURCE : Any>(
      private val joinColumn: YawnTableDef<SOURCE, *>.JoinColumnDef<*, *>,
  ) : YawnQueryRestriction<SOURCE> {
    override fun compile(
        context: YawnCompilationContext,
    ): Criterion = Restrictions.isNotEmpty(joinColumn.path(context))
  }
}
