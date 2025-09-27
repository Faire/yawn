package com.faire.yawn.setup.hibernate

import com.faire.yawn.YawnTableDef
import com.faire.yawn.query.CompiledYawnQuery
import com.faire.yawn.query.YawnCompilationContext
import com.faire.yawn.query.YawnQuery
import com.faire.yawn.query.YawnQueryFactory
import com.faire.yawn.query.YawnQueryRestriction.And
import org.hibernate.Session

internal class YawnTestQueryFactory(
    val session: Session,
) : YawnQueryFactory {
    override fun <T : Any> compile(query: YawnQuery<*, T>, tableDef: YawnTableDef<*, *>): CompiledYawnQuery<T> {
        val context = YawnCompilationContext.fromQuery(query)

        @Suppress("DEPRECATION")
        val rawQuery = context.generateAlias(tableDef)
            ?.let { rootAlias ->
                session.createCriteria(query.clazz, rootAlias)
            }
            ?: session.createCriteria(query.clazz)

        for (hint in query.queryHints) {
            rawQuery.addQueryHint(hint.hint)
        }

        for (join in query.joins) {
            val alias = checkNotNull(context.generateAlias(join.parent)) {
                "Unable to generate alias for join"
            }

            if (join.joinCriteria.isNotEmpty()) {
                val criterion = And(join.joinCriteria)
                rawQuery.createAlias(join.path(context), alias, join.joinType, criterion.compile(context))
            } else {
                rawQuery.createAlias(join.path(context), alias, join.joinType)
            }
        }

        for (criterion in query.criteria.map { it.yawnRestriction.compile(context) }) {
            rawQuery.add(criterion)
        }

        for (order in query.orders) {
            rawQuery.addOrder(order.compile(context))
        }

        val maxResults = query.maxResults
        if (maxResults != null) {
            rawQuery.setMaxResults(maxResults)
        }
        val offset = query.offset
        if (offset != null) {
            rawQuery.setFirstResult(offset)
        }

        val hibernateProjection = query.projection?.compile(context)
        if (hibernateProjection != null) {
            rawQuery.setProjection(hibernateProjection)
        }

        return YawnTestCompiledQuery(rawQuery)
    }
}
