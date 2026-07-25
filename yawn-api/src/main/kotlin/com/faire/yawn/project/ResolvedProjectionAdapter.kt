package com.faire.yawn.project

import com.faire.yawn.query.YawnCompilationContext
import org.hibernate.criterion.Projection
import org.hibernate.criterion.Projections

/**
 * Bridges a [ResolvedProjection] into the existing [YawnQueryProjection] pipeline.
 *
 * This adapter compiles each [ProjectionLeaf] to a Hibernate [Projection] and handles
 * result extraction from Hibernate's raw output into the [ResolvedProjection.mapRow] contract.
 */
class ResolvedProjectionAdapter<SOURCE : Any, TO>(
    private val resolved: ResolvedProjection<SOURCE, TO>,
) : YawnQueryProjection<SOURCE, TO> {
    override fun compile(context: YawnCompilationContext): Projection {
        val nodes = resolved.nodes
        check(nodes.isNotEmpty()) { "Cannot compile an empty projection." }

        if (nodes.size == 1) {
            return compileLeaf(context, nodes[0].leaf)
        }

        return Projections.projectionList().apply {
            for (node in nodes) {
                add(compileLeaf(context, node.leaf))
            }
        }
    }

    override fun project(value: Any?): TO {
        val values = if (resolved.nodes.size == 1) {
            listOf(value)
        } else {
            @Suppress("UNCHECKED_CAST")
            (value as Array<Any?>).toList()
        }
        return resolved.mapRow(values)
    }

    private fun compileLeaf(
        context: YawnCompilationContext,
        leaf: ProjectionLeaf<SOURCE>,
    ): Projection = when (leaf) {
        is ProjectionLeaf.Property -> Projections.property(leaf.column.generatePath(context))
        is ProjectionLeaf.Aggregate -> compileAggregate(context, leaf)
        is ProjectionLeaf.RowCount -> Projections.rowCount()
        is ProjectionLeaf.Sql -> HibernateYawnSqlProjection(leaf)
        is ProjectionLeaf.SqlValue -> ScopedYawnSqlProjection(context, leaf)
        is ProjectionLeaf.Modifier -> compileModifier(context, leaf)
        is ProjectionLeaf.Legacy -> leaf.projection.compile(context)
    }

    private fun compileAggregate(
        context: YawnCompilationContext,
        leaf: ProjectionLeaf.Aggregate<SOURCE>,
    ): Projection {
        val path = leaf.column.generatePath(context)
        return when (leaf.kind) {
            AggregateKind.COUNT -> Projections.count(path)
            AggregateKind.COUNT_DISTINCT -> Projections.countDistinct(path)
            AggregateKind.SUM -> Projections.sum(path)
            AggregateKind.AVG -> Projections.avg(path)
            AggregateKind.MIN -> Projections.min(path)
            AggregateKind.MAX -> Projections.max(path)
            AggregateKind.GROUP_BY -> Projections.groupProperty(path)
        }
    }

    private fun compileModifier(
        context: YawnCompilationContext,
        leaf: ProjectionLeaf.Modifier<SOURCE>,
    ): Projection = when (leaf.kind) {
        ModifierKind.DISTINCT -> Projections.distinct(compileLeaf(context, leaf.inner))
    }
}
