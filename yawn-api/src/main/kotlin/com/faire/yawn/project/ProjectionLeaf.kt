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
     * A raw SQL projection of a single computed value.
     *
     * The [sqlExpression] may use `{alias}` placeholders for table alias substitution.
     * [columnAlias] is the name the projected column is selected as, which the query factory uses to read
     * the value back out of the result set. [resultType] is used to map that SQL result to a Kotlin type.
     * It is up to the user to guarantee type-safety when using raw SQL projections!
     *
     * A leaf projects **exactly one** column, because it occupies exactly one slot in the resolved result
     * row (see [ProjectionNode.Value], whose mapper receives a single value). To project several values,
     * combine several leaves under a [ProjectionNode.Composite] instead.
     *
     * Note that this describes the projection in ORM-agnostic terms; adapting a single column to whatever
     * arity the underlying ORM expects is the query factory's job. Note also that the single-column shape
     * can only be enforced for what is *declared*: a [sqlExpression] that in fact selects two columns
     * would still shift every subsequent result slot, and validating the implementation of custom
     * projections remains the caller's responsibility.
     */
    data class Sql<SOURCE : Any>(
        val sqlExpression: String,
        val columnAlias: String,
        val resultType: KClass<*>,
    ) : ProjectionLeaf<SOURCE>

    /**
     * A raw SQL projection of a single computed value, assembled at render time.
     *
     * Unlike [Sql], the expression is built by [render] rather than supplied up front, because the ORM will only
     * resolve an entity property to its physical column while it is rendering the query. That is what makes
     * [YawnSqlScope.sql] possible. The expression is bare: the query factory selects it under an alias it
     * generates, so two SQL values in the same query can never be read from the same column.
     *
     * Note that this cannot use structural equality the way the other leaves do, since [render] is a function.
     * Two of these therefore never deduplicate onto one result slot, even when they would render identically.
     */
    class SqlValue<SOURCE : Any>(
        val render: YawnSqlScope<SOURCE>.() -> String,
        val resultType: KClass<*>,
    ) : ProjectionLeaf<SOURCE>

    /**
     * Wraps another leaf with a SQL modifier (e.g. DISTINCT).
     */
    data class Modifier<SOURCE : Any>(
        val kind: ModifierKind,
        val inner: ProjectionLeaf<SOURCE>,
    ) : ProjectionLeaf<SOURCE>

    /**
     * A bridge that lets a hand-written [YawnQueryProjection] participate in a projection tree.
     *
     * This is the one leaf that is *not* ORM-agnostic: [YawnQueryProjection.compile] hands back the ORM's own
     * projection type, so this leaf can only ever be compiled by the ORM that produced it. That is a deliberate
     * trade for migration - it lets a v2 tree contain projections Yawn does not know how to describe yet, so
     * composing (`pair`, `triple`, a generated `create(...)`) keeps working for the escape hatch users already
     * have. It should be deleted once nothing implements [YawnQueryProjection] by hand.
     *
     * As with [Sql], the wrapped projection must select **exactly one** column, since it occupies exactly one
     * slot in the resolved result row. This is not checkable here; it was equally the caller's responsibility
     * before, as the older composite projections indexed their results the same way.
     *
     * Uses identity equality (no two distinct instances deduplicate), because an arbitrary implementation
     * carries no structure to compare.
     */
    class Legacy<SOURCE : Any>(
        val projection: YawnQueryProjection<SOURCE, *>,
    ) : ProjectionLeaf<SOURCE>
}

/**
 * Views any [YawnQueryProjection] as a [YawnProjector], so it can be nested inside a projection tree.
 *
 * A projection that already describes itself as a tree is returned as-is; anything else is wrapped in a
 * [ProjectionLeaf.Legacy] bridge.
 */
fun <SOURCE : Any, TO> YawnQueryProjection<SOURCE, TO>.asProjector(): YawnProjector<SOURCE, TO> {
    if (this is YawnProjector<SOURCE, TO>) return this

    return YawnValueProjector {
        ProjectionNode.Value(
            leaf = ProjectionLeaf.Legacy(this),
            mapper = { project(it) },
        )
    }
}
