package com.faire.yawn.query

import com.faire.yawn.YawnTableDef
import com.faire.yawn.YawnTableDefParent.AssociationTableDefParent
import com.faire.yawn.criteria.query.JoinTypeSafeCriteriaQuery
import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.query.YawnQueryRestriction.YawnQueryRestrictionWithNestedRestriction
import org.hibernate.sql.JoinType

/**
 * The mutable building block used by Yawn to allow users to build queries piece-meal.
 * It can be compiled into a [CompiledYawnQuery] using a [YawnQueryFactory].
 */
data class YawnQuery<SOURCE : Any, T : Any>(
    val clazz: Class<T>,
    val criteria: MutableList<YawnQueryCriterion<SOURCE>> = mutableListOf(),
    val joins: MutableList<YawnQueryJoin<SOURCE>> = mutableListOf(),
    val orders: MutableList<YawnQueryOrder<SOURCE>> = mutableListOf(),
    val queryHints: MutableList<YawnQueryHint<SOURCE>> = mutableListOf(),
    var projection: YawnQueryProjection<SOURCE, *>? = null,

    var offset: Int? = null,
    var maxResults: Int? = null,
    var lockMode: YawnLockMode? = null,
) : YawnCriteriaQuery<SOURCE, T> {

    override fun addCriterion(criterion: YawnQueryCriterion<SOURCE>) {
        criteria.add(criterion)
    }

    internal fun <D : YawnTableDef<SOURCE, F>, F : Any> registerJoin(
        column: YawnTableDef<SOURCE, *>.JoinColumnDef<F, D>,
        joinType: JoinType,
        lambda: JoinTypeSafeCriteriaQuery<SOURCE, F, D>.(tableDef: D) -> Unit = {},
    ): YawnQueryJoin<SOURCE> {
        val parent = AssociationTableDefParent(column)
        val join = YawnQueryJoin<SOURCE>(
            columnDef = column,
            parent = parent,
            joinType = joinType,
        )
        join.joinCriteria.addAll(
            createJoinCriteria(column, join, lambda),
        )

        joins.add(join)
        return join
    }

    private fun <F : Any, D : YawnTableDef<SOURCE, F>> createJoinCriteria(
        column: YawnTableDef<SOURCE, *>.JoinColumnDef<F, D>,
        join: YawnQueryJoin<SOURCE>,
        lambda: JoinTypeSafeCriteriaQuery<SOURCE, F, D>.(tableDef: D) -> Unit,
    ): List<YawnQueryCriterion<SOURCE>> {
        val def = column.joinTableDef(join.parent)

        val criteria = mutableListOf<YawnQueryCriterion<SOURCE>>()
        val subQuery = object : YawnCriteriaQuery<SOURCE, F> {
            override fun addCriterion(criterion: YawnQueryCriterion<SOURCE>) {
                criteria.add(criterion)
            }
        }

        JoinTypeSafeCriteriaQuery.applyLambda(subQuery, def, lambda)

        return criteria
    }

    fun clone(): YawnQuery<SOURCE, T> {
        return copy(
            criteria = MutableList(criteria.size) { criteria[it].copy() },
            joins = MutableList(joins.size) { joins[it].copy() },
            orders = MutableList(orders.size) { orders[it].copy() },
            queryHints = MutableList(queryHints.size) { queryHints[it] },
        )
    }

    fun hasSubQuery(): Boolean {
        @Suppress("UNCHECKED_CAST")
        val criteriaStack = ArrayDeque(criteria) as ArrayDeque<YawnQueryCriterion<*>>

        while (criteriaStack.isNotEmpty()) {
            val criterion = criteriaStack.removeLast()
            when (criterion.yawnRestriction) {
                is YawnQueryRestrictionWithNestedRestriction<*> -> criteriaStack.addAll(
                    criterion.yawnRestriction.criteria,
                )
                is YawnDetachedQueryRestriction<*, *> -> return true
                else -> continue
            }
        }

        return false
    }
}
