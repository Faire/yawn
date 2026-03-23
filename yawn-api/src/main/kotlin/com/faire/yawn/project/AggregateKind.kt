package com.faire.yawn.project

/**
 * The kind of aggregate or grouping projection to apply.
 * Each kind maps to a specific SQL aggregation or clause.
 */
enum class AggregateKind {
    COUNT,
    COUNT_DISTINCT,
    SUM,
    AVG,
    MIN,
    MAX,
    GROUP_BY,
}
