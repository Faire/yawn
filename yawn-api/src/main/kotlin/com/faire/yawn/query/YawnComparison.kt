package com.faire.yawn.query

import com.faire.yawn.YawnDef

enum class YawnComparison {
    EQ, LT, LE, GT, GE;

    fun <SOURCE : Any, F> compare(
        column: YawnDef<SOURCE, *>.YawnColumnDef<F>,
        value: F & Any,
    ): YawnQueryCriterion<SOURCE> = when (this) {
        EQ -> YawnRestrictions.eq(column, value)
        LT -> YawnRestrictions.lt(column, value)
        LE -> YawnRestrictions.le(column, value)
        GT -> YawnRestrictions.gt(column, value)
        GE -> YawnRestrictions.ge(column, value)
    }
}
