package com.faire.yawn.project

import com.faire.yawn.YawnDef
import kotlin.reflect.KClass

/**
 * An ORM-agnostic descriptor of a single atomic projection.
 *
 * Leaves are the terminal elements that produce actual SQL in the compiled query projection tree.
 * The Query Factory is responsible for converting each leaf to the underlying ORM's implementation projection.
 *
 * Leaf deduplication uses data class equality: two [Property] leaves with the same [YawnDef.YawnColumnDef]
 * reference, or two [Aggregate] leaves with the same [AggregateKind] and column, are considered identical
 * and will share an index in the re-packed result list.
 */
sealed interface ProjectionLeaf<SOURCE : Any> {
    /**
     * A simple column property access (SQL: `alias.column`).
     */
    data class Property<SOURCE : Any>(
        val column: YawnDef<SOURCE, *>.YawnColumnDef<*>,
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
     * A raw SQL projection for custom expressions.
     *
     * The [sqlExpression] may use `{alias}` placeholders for table alias substitution.
     * [resultTypes] are used by the query factory to map SQL results to Kotlin types.
     * It is up to the user to guarantee type-safety when using raw SQL projections!
     */
    data class Sql<SOURCE : Any>(
        val sqlExpression: String,
        val aliases: List<String>,
        val resultTypes: List<KClass<*>>,
    ) : ProjectionLeaf<SOURCE>

    /**
     * Wraps another leaf with a SQL modifier (e.g. DISTINCT).
     */
    data class Modifier<SOURCE : Any>(
        val kind: ModifierKind,
        val inner: ProjectionLeaf<SOURCE>,
    ) : ProjectionLeaf<SOURCE>
}
