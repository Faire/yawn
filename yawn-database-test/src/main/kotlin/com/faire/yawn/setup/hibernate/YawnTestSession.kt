package com.faire.yawn.setup.hibernate

import com.faire.yawn.Yawn
import com.faire.yawn.YawnTableDef
import com.faire.yawn.YawnTableRef
import com.faire.yawn.criteria.builder.ProjectedTypeSafeCriteriaBuilder
import com.faire.yawn.criteria.builder.TypeSafeCriteriaBuilder
import com.faire.yawn.criteria.query.ProjectedTypeSafeCriteriaQuery
import com.faire.yawn.criteria.query.TypeSafeCriteriaQuery
import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.setup.entities.BaseEntity
import com.faire.yawn.setup.entities.TimestampedEntity
import org.hibernate.Session
import java.time.Instant

internal class YawnTestSession(
    val session: Session,
) {
  val yawn = Yawn(queryFactory = YawnTestQueryFactory(session))

  fun <T> save(entity: T): T {
    if (entity is TimestampedEntity<*>) {
      updateTimestamps(entity)
    }

    session.save(entity)
    return entity
  }

  inline fun <reified T : BaseEntity<T>, reified DEF : YawnTableDef<T, T>> query(
      tableRef: YawnTableRef<T, DEF>,
      noinline lambda: TypeSafeCriteriaQuery<T, DEF>.(tableDef: DEF) -> Unit = {},
  ): TypeSafeCriteriaBuilder<T, DEF> {
    return yawn.query(T::class.java, tableRef, lambda)
  }

  inline fun <reified T : BaseEntity<T>, reified DEF : YawnTableDef<T, T>, PROJECTION : Any?> project(
      tableRef: YawnTableRef<T, DEF>,
      noinline lambda:
      ProjectedTypeSafeCriteriaQuery<T, T, DEF, PROJECTION>.(tableDef: DEF) -> YawnQueryProjection<T, PROJECTION>,
  ): ProjectedTypeSafeCriteriaBuilder<T, DEF, PROJECTION> {
    return yawn.project(T::class.java, tableRef, lambda)
  }

  @Suppress("SystemTimeReplacedWithClock")
  private fun updateTimestamps(entity: TimestampedEntity<*>) {
    val now = Instant.now()
    entity.updatedAt = now
    if (!entity.isCreatedAtInitialized()) {
      entity.createdAt = now
    }
  }
}
