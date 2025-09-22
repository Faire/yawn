package com.faire.yawn.project

import com.faire.yawn.YawnTableDef
import com.faire.yawn.query.YawnCompilationContext
import org.hibernate.criterion.Projection
import org.hibernate.criterion.Projections
import org.hibernate.type.StandardBasicTypes

/**
 * Yawn equivalent of Hibernate [Projections].
 * A utility object to create type-safe [YawnQueryProjection].
 */
object YawnProjections {
  internal class Distinct<SOURCE : Any, TO>(
      private val projection: YawnQueryProjection<SOURCE, TO>,
  ) : YawnQueryProjection<SOURCE, TO> {
    override fun compile(
        context: YawnCompilationContext,
    ): Projection = Projections.distinct(projection.compile(context))

    override fun project(value: Any?): TO = projection.project(value)
  }

  fun <SOURCE : Any, TO> distinct(
      projection: YawnQueryProjection<SOURCE, TO>,
  ): YawnQueryProjection<SOURCE, TO> {
    return Distinct(projection)
  }

  internal class Count<SOURCE : Any, FROM : Any?>(
      private val columnDef: YawnTableDef<SOURCE, *>.ColumnDef<FROM>,
  ) : YawnQueryProjection<SOURCE, Long> {
    override fun compile(
        context: YawnCompilationContext,
    ): Projection = Projections.count(columnDef.generatePath(context))

    override fun project(value: Any?): Long = value as Long
  }

  fun <SOURCE : Any, FROM : Any?> count(
      columnDef: YawnTableDef<SOURCE, *>.ColumnDef<FROM>,
  ): YawnQueryProjection<SOURCE, Long> {
    return Count(columnDef)
  }

  internal class CountDistinct<SOURCE : Any, FROM : Any?>(
      private val columnDef: YawnTableDef<SOURCE, *>.ColumnDef<FROM>,
  ) : YawnQueryProjection<SOURCE, Long> {
    override fun compile(
        context: YawnCompilationContext,
    ): Projection = Projections.countDistinct(columnDef.generatePath(context))

    override fun project(value: Any?): Long = value as Long
  }

  fun <SOURCE : Any, FROM : Any?> countDistinct(
      columnDef: YawnTableDef<SOURCE, *>.ColumnDef<FROM>,
  ): YawnQueryProjection<SOURCE, Long> {
    return CountDistinct(columnDef)
  }

  internal class SumNullable<SOURCE : Any, FROM : Number?>(
      private val columnDef: YawnTableDef<SOURCE, *>.ColumnDef<FROM>,
  ) : YawnQueryProjection<SOURCE, Long?> {
    override fun compile(
        context: YawnCompilationContext,
    ): Projection = Projections.sum(columnDef.generatePath(context))

    override fun project(value: Any?): Long? = value as Long?
  }

  @JvmName("sumNullable")
  fun <SOURCE : Any, FROM : Number?> sum(
      columnDef: YawnTableDef<SOURCE, *>.ColumnDef<FROM>,
  ): YawnQueryProjection<SOURCE, Long?> {
    return SumNullable(columnDef)
  }

  internal class Sum<SOURCE : Any, FROM : Number>(
      private val columnDef: YawnTableDef<SOURCE, *>.ColumnDef<FROM>,
  ) : YawnQueryProjection<SOURCE, Long> {
    override fun compile(
        context: YawnCompilationContext,
    ): Projection = Projections.sum(columnDef.generatePath(context))

    override fun project(value: Any?): Long = value as Long
  }

  fun <SOURCE : Any, FROM : Number> sum(
      columnDef: YawnTableDef<SOURCE, *>.ColumnDef<FROM>,
  ): YawnQueryProjection<SOURCE, Long> {
    return Sum(columnDef)
  }

  internal class AvgNullable<SOURCE : Any, FROM : Any?>(
      private val columnDef: YawnTableDef<SOURCE, *>.ColumnDef<FROM>,
  ) : YawnQueryProjection<SOURCE, Double?> {
    override fun compile(
        context: YawnCompilationContext,
    ): Projection = Projections.avg(columnDef.generatePath(context))

    override fun project(value: Any?): Double? = value as Double?
  }

  @JvmName("avgNullable")
  fun <SOURCE : Any, FROM : Number?> avg(
      columnDef: YawnTableDef<SOURCE, *>.ColumnDef<FROM>,
  ): YawnQueryProjection<SOURCE, Double?> {
    return AvgNullable(columnDef)
  }

  internal class Avg<SOURCE : Any, FROM : Number>(
      private val columnDef: YawnTableDef<SOURCE, *>.ColumnDef<FROM>,
  ) : YawnQueryProjection<SOURCE, Double> {
    override fun compile(
        context: YawnCompilationContext,
    ): Projection = Projections.avg(columnDef.generatePath(context))

    override fun project(value: Any?): Double = value as Double
  }

  fun <SOURCE : Any, FROM : Number> avg(
      columnDef: YawnTableDef<SOURCE, *>.ColumnDef<FROM>,
  ): YawnQueryProjection<SOURCE, Double> {
    return Avg(columnDef)
  }

  internal class Max<SOURCE : Any, FROM : Comparable<FROM>?>(
      private val columnDef: YawnTableDef<SOURCE, *>.ColumnDef<FROM>,
  ) : YawnQueryProjection<SOURCE, FROM> {
    override fun compile(
        context: YawnCompilationContext,
    ): Projection = Projections.max(columnDef.generatePath(context))

    @Suppress("UNCHECKED_CAST")
    override fun project(value: Any?): FROM = value as FROM
  }

  fun <SOURCE : Any, FROM : Comparable<FROM>?> max(
      columnDef: YawnTableDef<SOURCE, *>.ColumnDef<FROM>,
  ): YawnQueryProjection<SOURCE, FROM> {
    return Max(columnDef)
  }

  internal class Min<SOURCE : Any, FROM : Comparable<FROM>?>(
      private val columnDef: YawnTableDef<SOURCE, *>.ColumnDef<FROM>,
  ) : YawnQueryProjection<SOURCE, FROM> {
    override fun compile(
        context: YawnCompilationContext,
    ): Projection = Projections.min(columnDef.generatePath(context))

    @Suppress("UNCHECKED_CAST")
    override fun project(value: Any?): FROM = value as FROM
  }

  fun <SOURCE : Any, FROM : Comparable<FROM>?> min(
      columnDef: YawnTableDef<SOURCE, *>.ColumnDef<FROM>,
  ): YawnQueryProjection<SOURCE, FROM> {
    return Min(columnDef)
  }

  internal class GroupBy<SOURCE : Any, FROM : Any?>(
      private val columnDef: YawnTableDef<SOURCE, *>.ColumnDef<FROM>,
  ) : YawnQueryProjection<SOURCE, FROM> {
    override fun compile(
        context: YawnCompilationContext,
    ): Projection = Projections.groupProperty(columnDef.generatePath(context))

    @Suppress("UNCHECKED_CAST")
    override fun project(value: Any?): FROM = value as FROM
  }

  fun <SOURCE : Any, FROM : Any?> groupBy(
      columnDef: YawnTableDef<SOURCE, *>.ColumnDef<FROM>,
  ): YawnQueryProjection<SOURCE, FROM> {
    return GroupBy(columnDef)
  }

  internal class RowCount<SOURCE : Any> : YawnQueryProjection<SOURCE, Long> {
    override fun compile(context: YawnCompilationContext): Projection = Projections.rowCount()

    override fun project(value: Any?): Long = value as Long
  }

  fun <SOURCE : Any> rowCount(): YawnQueryProjection<SOURCE, Long> {
    return RowCount()
  }

  internal class SelectConstant<SOURCE : Any>(
      private val constant: String,
  ) : YawnQueryProjection<SOURCE, String> {
    override fun compile(context: YawnCompilationContext): Projection {
      return Projections.sqlProjection(
          "'$constant' as $CONSTANT_ALIAS",
          arrayOf(CONSTANT_ALIAS),
          arrayOf(StandardBasicTypes.STRING),
      )
    }

    override fun project(value: Any?): String = value as String
  }

  fun <SOURCE : Any> selectConstant(constant: String): YawnQueryProjection<SOURCE, String> {
    return SelectConstant(constant)
  }

  internal class Coalesce<SOURCE : Any, FROM : Any?>(
      private val projection: YawnQueryProjection<SOURCE, FROM?>,
      private val defaultValue: FROM,
  ) : YawnQueryProjection<SOURCE, FROM> {
    override fun compile(
        context: YawnCompilationContext,
    ): Projection = projection.compile(context)

    @Suppress("UNCHECKED_CAST")
    override fun project(value: Any?): FROM = (value as FROM?) ?: defaultValue
  }

  fun <SOURCE : Any, FROM : Any> coalesce(
      projection: YawnQueryProjection<SOURCE, FROM?>,
      defaultValue: FROM,
  ): YawnQueryProjection<SOURCE, FROM> {
    return Coalesce(projection, defaultValue)
  }

  internal class Null<SOURCE : Any, FROM : Any> : YawnQueryProjection<SOURCE, FROM?> {
    override fun compile(context: YawnCompilationContext): Projection {
      return Projections.sqlProjection(
          "null as $CONSTANT_ALIAS",
          arrayOf(CONSTANT_ALIAS),
          arrayOf(StandardBasicTypes.STRING),
      )
    }

    override fun project(value: Any?): FROM? = null
  }

  fun <SOURCE : Any, T : Any> `null`(): YawnQueryProjection<SOURCE, T?> {
    return Null()
  }

  internal class PairProjection<SOURCE : Any, A : Any?, B : Any?>(
      private val firstProjection: YawnQueryProjection<SOURCE, A>,
      private val secondProjection: YawnQueryProjection<SOURCE, B>,
  ) : YawnQueryProjection<SOURCE, Pair<A, B>> {
    override fun compile(context: YawnCompilationContext): Projection {
      return Projections.projectionList()
          .add(firstProjection.compile(context))
          .add(secondProjection.compile(context))
    }

    override fun project(value: Any?): Pair<A, B> {
      val queryResult = value as Array<*>
      return Pair(firstProjection.project(queryResult[0]), secondProjection.project(queryResult[1]))
    }
  }

  fun <SOURCE : Any, A : Any?, B : Any?> pair(
      firstProjection: YawnQueryProjection<SOURCE, A>,
      secondProjection: YawnQueryProjection<SOURCE, B>,
  ): YawnQueryProjection<SOURCE, Pair<A, B>> {
    return PairProjection(firstProjection, secondProjection)
  }

  internal class TripleProjection<SOURCE : Any, A : Any?, B : Any?, C : Any?>(
      private val firstProjection: YawnQueryProjection<SOURCE, A>,
      private val secondProjection: YawnQueryProjection<SOURCE, B>,
      private val thirdProjection: YawnQueryProjection<SOURCE, C>,
  ) : YawnQueryProjection<SOURCE, Triple<A, B, C>> {
    override fun compile(context: YawnCompilationContext): Projection {
      return Projections.projectionList()
          .add(firstProjection.compile(context))
          .add(secondProjection.compile(context))
          .add(thirdProjection.compile(context))
    }

    override fun project(value: Any?): Triple<A, B, C> {
      val queryResult = value as Array<*>
      return Triple(
          firstProjection.project(queryResult[0]),
          secondProjection.project(queryResult[1]),
          thirdProjection.project(queryResult[2]),
      )
    }
  }

  fun <SOURCE : Any, A : Any?, B : Any?, C : Any?> triple(
      firstProjection: YawnQueryProjection<SOURCE, A>,
      secondProjection: YawnQueryProjection<SOURCE, B>,
      thirdProjection: YawnQueryProjection<SOURCE, C>,
  ): YawnQueryProjection<SOURCE, Triple<A, B, C>> {
    return TripleProjection(firstProjection, secondProjection, thirdProjection)
  }
}

private const val CONSTANT_ALIAS = "_yawn_ct"
