package com.faire.yawn

import com.faire.yawn.YawnTableDefParent.RootTableDefParent
import com.faire.yawn.criteria.builder.DetachedProjectedYawnBuilder
import com.faire.yawn.criteria.builder.EntityYawnQueryBuilder
import com.faire.yawn.criteria.builder.ProjectedYawnQueryBuilder
import com.faire.yawn.criteria.query.EntityYawnQueryScope
import com.faire.yawn.criteria.query.ProjectedYawnQueryScope
import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.query.YawnQuery
import com.faire.yawn.query.YawnQueryFactory

/**
 * Main Yawn entrypoint, allows you to create queries using a provided [YawnQueryFactory].
 */
class Yawn(
    private val queryFactory: YawnQueryFactory,
) {
    fun <T : Any, DEF : YawnTableDef<T, T>> query(
        tClass: Class<T>,
        tableRef: YawnTableRef<T, DEF>,
        lambda: EntityYawnQueryScope<T, DEF>.(tableDef: DEF) -> Unit = {},
    ): EntityYawnQueryBuilder<T, DEF> {
        val query = YawnQuery<T, T>(tClass)
        val tableDef = tableRef.create(parent = RootTableDefParent)
        return EntityYawnQueryBuilder.create(tableDef, queryFactory, query, lambda)
    }

    fun <T : Any, DEF : YawnTableDef<T, T>, PROJECTION : Any?> project(
        tClass: Class<T>,
        tableRef: YawnTableRef<T, DEF>,
        lambda: ProjectedYawnQueryScope<T, T, DEF, PROJECTION>.(
            tableDef: DEF,
        ) -> YawnQueryProjection<T, PROJECTION>,
    ): ProjectedYawnQueryBuilder<T, DEF, PROJECTION> {
        val query = YawnQuery<T, T>(tClass)
        val tableDef = tableRef.create(parent = RootTableDefParent)
        return ProjectedYawnQueryBuilder.create(tableDef, queryFactory, query, lambda)
    }

    companion object {
        inline fun <reified T : Any, DEF : YawnTableDef<T, T>, PROJECTION : Any?> createProjectedDetachedCriteria(
            tableRef: YawnTableRef<T, DEF>,
            noinline lambda:
            ProjectedYawnQueryScope<T, T, DEF, PROJECTION>.(tableDef: DEF) -> YawnQueryProjection<T, PROJECTION>,
        ): DetachedProjectedYawnBuilder<T, T, DEF, PROJECTION> {
            val query = YawnQuery<T, T>(T::class.java)

            @Suppress("UNCHECKED_CAST") // DEF is YawnTableDef<T, T>, so this cast is safe
            val tableDef = tableRef.forSubQuery<T>() as DEF
            return DetachedProjectedYawnBuilder.create(tableDef, query, lambda)
        }
    }
}
