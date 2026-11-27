package com.faire.yawn.setup.hibernate

import com.faire.yawn.Yawn
import com.faire.yawn.YawnEntityTableDef
import com.faire.yawn.YawnTableRef
import com.faire.yawn.criteria.builder.EntityYawnQueryBuilder
import com.faire.yawn.criteria.builder.ProjectedYawnQueryBuilder
import com.faire.yawn.criteria.query.EntityYawnQueryScope
import com.faire.yawn.criteria.query.ProjectedYawnQueryScope
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

    fun <T : BaseEntity<T>, DEF : YawnEntityTableDef<T, T>> query(
        tableRef: YawnTableRef<T, DEF>,
        lambda: EntityYawnQueryScope<T, DEF>.(tableDef: DEF) -> Unit = {},
    ): EntityYawnQueryBuilder<T, DEF> {
        return yawn.query(tableRef, lambda)
    }

    fun <T : BaseEntity<T>, DEF : YawnEntityTableDef<T, T>, PROJECTION : Any?> project(
        tableRef: YawnTableRef<T, DEF>,
        lambda: ProjectedYawnQueryScope<T, T, DEF, PROJECTION>.(tableDef: DEF) -> YawnQueryProjection<T, PROJECTION>,
    ): ProjectedYawnQueryBuilder<T, DEF, PROJECTION> {
        return yawn.project(tableRef, lambda)
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
