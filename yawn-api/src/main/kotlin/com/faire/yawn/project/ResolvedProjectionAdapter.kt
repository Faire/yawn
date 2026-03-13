package com.faire.yawn.project

import com.faire.yawn.query.YawnCompilationContext
import org.hibernate.criterion.Projection
import org.hibernate.criterion.Projections
import org.hibernate.type.StandardBasicTypes
import org.hibernate.type.Type
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass

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
            return compileLeaf(nodes[0].leaf, context)
        }

        return Projections.projectionList().apply {
            for (node in nodes) {
                add(compileLeaf(node.leaf, context))
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
        leaf: ProjectionLeaf<SOURCE>,
        context: YawnCompilationContext,
    ): Projection = when (leaf) {
        is ProjectionLeaf.Property -> Projections.property(leaf.column.generatePath(context))
        is ProjectionLeaf.Aggregate -> compileAggregate(leaf, context)
        is ProjectionLeaf.RowCount -> Projections.rowCount()
        is ProjectionLeaf.Sql -> compileSql(leaf)
        is ProjectionLeaf.Modifier -> compileModifier(leaf, context)
    }

    private fun compileAggregate(
        leaf: ProjectionLeaf.Aggregate<SOURCE>,
        context: YawnCompilationContext,
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

    private fun compileSql(leaf: ProjectionLeaf.Sql<SOURCE>): Projection {
        return Projections.sqlProjection(
            leaf.sqlExpression,
            leaf.aliases.toTypedArray(),
            leaf.resultTypes.map { it.toHibernateType() }.toTypedArray(),
        )
    }

    private fun compileModifier(
        leaf: ProjectionLeaf.Modifier<SOURCE>,
        context: YawnCompilationContext,
    ): Projection = when (leaf.kind) {
        ModifierKind.DISTINCT -> Projections.distinct(compileLeaf(leaf.inner, context))
    }

    companion object {
        private fun KClass<*>.toHibernateType(): Type = when (this) {
            String::class -> StandardBasicTypes.STRING
            Long::class -> StandardBasicTypes.LONG
            Int::class -> StandardBasicTypes.INTEGER
            Double::class -> StandardBasicTypes.DOUBLE
            Float::class -> StandardBasicTypes.FLOAT
            Boolean::class -> StandardBasicTypes.BOOLEAN
            Short::class -> StandardBasicTypes.SHORT
            Byte::class -> StandardBasicTypes.BYTE
            BigDecimal::class -> StandardBasicTypes.BIG_DECIMAL
            BigInteger::class -> StandardBasicTypes.BIG_INTEGER
            else -> error("Unsupported SQL projection result type: $this")
        }
    }
}
