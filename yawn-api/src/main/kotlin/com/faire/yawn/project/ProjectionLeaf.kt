package com.faire.yawn.project

import com.faire.yawn.YawnDef
import kotlin.reflect.KClass

/**
 * An ORM-agnostic descriptor of a single atomic projection.
 *
 * Leaves are the terminal elements that produce actual SQL in the compiled query projection tree.
 * The Query Factory is responsible for converting each leaf to the underlying ORM's implementation projection.
 *
 * Leaf deduplication uses data class equality: two [Property] leaves with the same [YawnPathProvider]
 * reference, or two [Aggregate] leaves with the same [AggregateKind] and column, are considered identical
 * and will share an index in the re-packed result list.
 */
sealed interface ProjectionLeaf<SOURCE : Any> {
    /**
     * A property access (SQL: `alias.column`).
     *
     * Works with any [YawnPathProvider], including both [YawnDef.YawnColumnDef]
     * and [com.faire.yawn.YawnTableDef.JoinColumnDef].
     */
    data class Property<SOURCE : Any>(
        val column: YawnPathProvider<SOURCE>,
    ) : ProjectionLeaf<SOURCE>

    /**
     * An aggregate or grouping projection on a column (SQL: `SUM(alias.column)`, `GROUP BY alias.column`, etc.).
     */
    data class Aggregate<SOURCE : Any>(
        val kind: AggregateKind,
        val column: YawnDef<SOURCE, *>.YawnColumnDef<*>,
    ) : ProjectionLeaf<SOURCE>

    /**
     * A row count projection (SQL: `COUNT(*)`).
     */
    class RowCount<SOURCE : Any> : ProjectionLeaf<SOURCE> {
        override fun equals(other: Any?): Boolean = other is RowCount<*>
        override fun hashCode(): Int = RowCount::class.hashCode()
    }

    /**
     * A raw SQL projection of a single computed value, assembled at render time.
     *
     * The expression is built by [render] rather than supplied up front, because the ORM will only resolve an
     * entity property to its physical column while it is rendering the query. That is what makes
     * [YawnSqlScope.sql] possible. The expression is bare: the query factory selects it under an alias it
     * generates, so two SQL values in the same query can never be read from the same column.
     *
     * A leaf projects **exactly one** column, because it occupies exactly one slot in the resolved result row
     * (see [ProjectionNode.Value], whose mapper receives a single value). To project several values, combine
     * several leaves under a [ProjectionNode.Composite] instead. That shape can only be enforced for what is
     * *declared*: an expression that in fact selects two columns would still shift every subsequent result
     * slot, and guaranteeing the type-safety of raw SQL remains the caller's responsibility.
     *
     * Note that this cannot use structural equality the way the other leaves do, since [render] is a function.
     * Two of these therefore never deduplicate onto one result slot, even when they would render identically.
     */
    class SqlValue<SOURCE : Any>(
        val render: YawnSqlScope<SOURCE>.() -> String,
        val resultType: KClass<*>,
    ) : ProjectionLeaf<SOURCE>

    /**
     * Selects another leaf under a name, so that the rest of the query can refer back to it.
     *
     * An aggregate has no name of its own to sort on, so ordering by one means selecting it under an alias and
     * ordering by that alias instead. [alias] supplies the name, and is the same object the `ORDER BY` clause
     * resolves its path from - which is what keeps the two halves agreeing on it.
     */
    data class Aliased<SOURCE : Any>(
        val inner: ProjectionLeaf<SOURCE>,
        val alias: YawnPathProvider<SOURCE>,
    ) : ProjectionLeaf<SOURCE>
}
