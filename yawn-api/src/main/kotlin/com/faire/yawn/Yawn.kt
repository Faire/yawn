package com.faire.yawn

import com.faire.yawn.YawnTableDefParent.RootTableDefParent
import com.faire.yawn.criteria.builder.DetachedProjectedTypeSafeCriteriaBuilder
import com.faire.yawn.criteria.builder.ProjectedTypeSafeCriteriaBuilder
import com.faire.yawn.criteria.builder.TypeSafeCriteriaBuilder
import com.faire.yawn.criteria.query.ProjectedTypeSafeCriteriaQuery
import com.faire.yawn.criteria.query.TypeSafeCriteriaQuery
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
        lambda: TypeSafeCriteriaQuery<T, DEF>.(tableDef: DEF) -> Unit = {},
    ): TypeSafeCriteriaBuilder<T, DEF> {
        val query = YawnQuery<T, T>(tClass)
        val tableDef = tableRef.create(parent = RootTableDefParent)
        return TypeSafeCriteriaBuilder.create(tableDef, queryFactory, query, lambda)
    }

    fun <T : Any, DEF : YawnTableDef<T, T>, PROJECTION : Any?> project(
        tClass: Class<T>,
        tableRef: YawnTableRef<T, DEF>,
        lambda: ProjectedTypeSafeCriteriaQuery<T, T, DEF, PROJECTION>.(
            tableDef: DEF,
        ) -> YawnQueryProjection<T, PROJECTION>,
    ): ProjectedTypeSafeCriteriaBuilder<T, DEF, PROJECTION> {
        val query = YawnQuery<T, T>(tClass)
        val tableDef = tableRef.create(parent = RootTableDefParent)
        return ProjectedTypeSafeCriteriaBuilder.create(tableDef, queryFactory, query, lambda)
    }

    companion object {
        inline fun <reified T : Any, DEF : YawnTableDef<T, T>, PROJECTION : Any?> createProjectedDetachedCriteria(
            tableRef: YawnTableRef<T, DEF>,
            noinline lambda:
            ProjectedTypeSafeCriteriaQuery<T, T, DEF, PROJECTION>.(tableDef: DEF) -> YawnQueryProjection<T, PROJECTION>,
        ): DetachedProjectedTypeSafeCriteriaBuilder<T, T, DEF, PROJECTION> {
            val query = YawnQuery<T, T>(T::class.java)

            @Suppress("UNCHECKED_CAST") // DEF is YawnTableDef<T, T>, so this cast is safe
            val tableDef = tableRef.forSubQuery<T>() as DEF
            return DetachedProjectedTypeSafeCriteriaBuilder.create(tableDef, query, lambda)
        }
    }
}
