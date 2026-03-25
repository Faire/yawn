package com.faire.yawn.query

import com.faire.yawn.YawnTableDef
import com.faire.yawn.YawnTableDefParent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class YawnComparisonTest {

    private object TestDef : YawnTableDef<TestEntity, TestEntity>(YawnTableDefParent.RootTableDefParent) {
        val amount: ColumnDef<Int> = ColumnDef("amount")
    }

    private class TestEntity

    @Test
    fun `EQ delegates to YawnRestrictions eq`() {
        val criterion = YawnComparison.EQ.compare(TestDef.amount, 10)
        assertThat(criterion.yawnRestriction).isInstanceOf(YawnQueryRestriction.Equals::class.java)
    }

    @Test
    fun `LT delegates to YawnRestrictions lt`() {
        val criterion = YawnComparison.LT.compare(TestDef.amount, 10)
        assertThat(criterion.yawnRestriction).isInstanceOf(YawnQueryRestriction.LessThan::class.java)
    }

    @Test
    fun `LE delegates to YawnRestrictions le`() {
        val criterion = YawnComparison.LE.compare(TestDef.amount, 10)
        assertThat(criterion.yawnRestriction).isInstanceOf(YawnQueryRestriction.LessThanOrEqualTo::class.java)
    }

    @Test
    fun `GT delegates to YawnRestrictions gt`() {
        val criterion = YawnComparison.GT.compare(TestDef.amount, 10)
        assertThat(criterion.yawnRestriction).isInstanceOf(YawnQueryRestriction.GreaterThan::class.java)
    }

    @Test
    fun `GE delegates to YawnRestrictions ge`() {
        val criterion = YawnComparison.GE.compare(TestDef.amount, 10)
        assertThat(criterion.yawnRestriction).isInstanceOf(YawnQueryRestriction.GreaterThanOrEqualTo::class.java)
    }

    @Test
    fun `compare produces same restriction type as calling YawnRestrictions directly`() {
        for (comparison in YawnComparison.entries) {
            val fromEnum = comparison.compare(TestDef.amount, 42)
            val fromRestrictions = when (comparison) {
                YawnComparison.EQ -> YawnRestrictions.eq(TestDef.amount, 42)
                YawnComparison.LT -> YawnRestrictions.lt(TestDef.amount, 42)
                YawnComparison.LE -> YawnRestrictions.le(TestDef.amount, 42)
                YawnComparison.GT -> YawnRestrictions.gt(TestDef.amount, 42)
                YawnComparison.GE -> YawnRestrictions.ge(TestDef.amount, 42)
            }
            assertThat(fromEnum.yawnRestriction::class).isEqualTo(fromRestrictions.yawnRestriction::class)
        }
    }

    @Test
    fun `all five comparison variants are present`() {
        assertThat(YawnComparison.entries).containsExactly(
            YawnComparison.EQ,
            YawnComparison.LT,
            YawnComparison.LE,
            YawnComparison.GT,
            YawnComparison.GE,
        )
    }
}
