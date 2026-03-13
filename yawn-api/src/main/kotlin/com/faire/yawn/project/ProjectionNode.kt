package com.faire.yawn.project

import com.faire.yawn.YawnDef

/**
 * A declarative description of a projection's structure.
 *
 * The [ProjectorResolver] resolution engine walks this tree to produce a flat [ResolvedProjection].
 * There are four variants:
 * * [Value]: a single leaf projection (column, aggregate, SQL, etc.). Only Values will survive resolution.
 * * [Composite]: a grouping of multiple children with a mapper. Flattened during resolution.
 * * [Constant]: a fixed value, with no corresponding SQL. Eliminated during resolution.
 * * [Mapped]: wraps another projection with an in-memory transformation. Eliminated during resolution.
 */
sealed interface ProjectionNode<SOURCE : Any, TO> {
    /**
     * A single leaf projection that maps to one slot in the SQL result.
     *
     * @param leaf the ORM-agnostic projection descriptor.
     * @param mapper transforms the raw SQL value to the desired type. Defaults to a simple unchecked cast.
     */
    data class Value<SOURCE : Any, TO>(
        val leaf: ProjectionLeaf<SOURCE>,
        val mapper: (Any?) -> TO = {
            @Suppress("UNCHECKED_CAST")
            it as TO
        },
    ) : ProjectionNode<SOURCE, TO>

    /**
     * Groups multiple child projections and combines their results with a mapper.
     *
     * During resolution, composites are recursively flattened: their children's leaves are added to
     * the parent's flat list, and the mapper is composed to reconstruct the grouped result.
     */
    data class Composite<SOURCE : Any, TO>(
        val children: List<YawnProjector<SOURCE, *>>,
        val mapper: (List<Any?>) -> TO,
    ) : ProjectionNode<SOURCE, TO>

    /**
     * A constant value that requires no SQL. Eliminated during resolution.
     */
    data class Constant<SOURCE : Any, TO>(
        val value: TO,
    ) : ProjectionNode<SOURCE, TO>

    /**
     * Wraps another projection with a post-query transform. Eliminated during resolution;
     * the transform is folded into the composed mapper.
     */
    data class Mapped<SOURCE : Any, FROM, TO>(
        val from: YawnProjector<SOURCE, FROM>,
        val transform: (FROM) -> TO,
    ) : ProjectionNode<SOURCE, TO>

    companion object {
        fun <SOURCE : Any, TO> property(
            column: YawnDef<SOURCE, *>.YawnColumnDef<TO>,
        ): Value<SOURCE, TO> = Value(ProjectionLeaf.Property(column))

        fun <SOURCE : Any, TO> aggregate(
            kind: AggregateKind,
            column: YawnDef<SOURCE, *>.YawnColumnDef<TO>,
        ): Value<SOURCE, TO> = Value(ProjectionLeaf.Aggregate(kind, column))

        fun <SOURCE : Any, TO> aggregateAs(
            kind: AggregateKind,
            column: YawnDef<SOURCE, *>.YawnColumnDef<*>,
        ): Value<SOURCE, TO> = Value(ProjectionLeaf.Aggregate(kind, column))

        fun <SOURCE : Any> rowCount(): Value<SOURCE, Long> =
            Value(ProjectionLeaf.RowCount())

        fun <SOURCE : Any, TO> sql(
            sqlExpression: String,
            aliases: List<String>,
            resultTypes: List<kotlin.reflect.KClass<*>>,
        ): Value<SOURCE, TO> = Value(ProjectionLeaf.Sql(sqlExpression, aliases, resultTypes))

        fun <SOURCE : Any, TO> constant(value: TO): Constant<SOURCE, TO> = Constant(value)

        fun <SOURCE : Any, FROM, TO> mapped(
            from: YawnProjector<SOURCE, FROM>,
            transform: (FROM) -> TO,
        ): Mapped<SOURCE, FROM, TO> = Mapped(from, transform)

        fun <SOURCE : Any, A, B, R> composite(
            a: YawnProjector<SOURCE, A>,
            b: YawnProjector<SOURCE, B>,
            mapper: (A, B) -> R,
        ): Composite<SOURCE, R> = Composite(listOf(a, b)) { values ->
            @Suppress("UNCHECKED_CAST")
            mapper(values[0] as A, values[1] as B)
        }

        fun <SOURCE : Any, A, B, C, R> composite(
            a: YawnProjector<SOURCE, A>,
            b: YawnProjector<SOURCE, B>,
            c: YawnProjector<SOURCE, C>,
            mapper: (A, B, C) -> R,
        ): Composite<SOURCE, R> = Composite(listOf(a, b, c)) { values ->
            @Suppress("UNCHECKED_CAST")
            mapper(values[0] as A, values[1] as B, values[2] as C)
        }

        fun <SOURCE : Any, R> composite(
            children: List<YawnProjector<SOURCE, *>>,
            mapper: (List<Any?>) -> R,
        ): Composite<SOURCE, R> = Composite(children, mapper)
    }
}
