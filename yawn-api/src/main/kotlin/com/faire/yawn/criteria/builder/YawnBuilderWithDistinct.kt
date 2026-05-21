package com.faire.yawn.criteria.builder

/**
 * An interface for query builders that support query-level DISTINCT.
 *
 * This is implemented by both [ProjectedYawnQueryBuilder] (top-level queries)
 * and [DetachedProjectedYawnBuilder] (subqueries / detached criteria).
 * Hibernate only supports DISTINCT through projections, so non-projected builders cannot use this.
 *
 * Query-level DISTINCT applies `SELECT DISTINCT ...` to the entire projection.
 * This is separate from aggregate-level distinct (e.g. `COUNT(DISTINCT col)`),
 * which is handled via [com.faire.yawn.project.AggregateKind.COUNT_DISTINCT].
 *
 * @param CRITERIA the concrete builder type, for fluent chaining.
 */
interface YawnBuilderWithDistinct<CRITERIA : BaseYawnBuilder<*>> {
    /**
     * Applies query-level `SELECT DISTINCT` to this projected query.
     *
     * Unlike [com.faire.yawn.project.YawnProjections.distinct], which wraps a projection
     * and relies on Hibernate's string-concatenation hack, this method sets a query-level flag
     * that wraps the entire compiled projection with `Projections.distinct(...)` at compilation time.
     *
     * @param distinct whether to apply DISTINCT. Defaults to `true`. Pass `false` to turn off
     *   distinct for queries built piecemeal or to set from a variable.
     * @return this builder for chaining.
     */
    fun distinct(distinct: Boolean = true): CRITERIA
}
