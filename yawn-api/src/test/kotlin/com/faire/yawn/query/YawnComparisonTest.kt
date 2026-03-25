package com.faire.yawn.query

import com.faire.yawn.YawnTableDef
import com.faire.yawn.YawnTableDefParent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class YawnComparisonTest {

    private class OrderEntity

    /**
     * Simulates a table with columns of different types — the scenario where
     * you can't pass YawnRestrictions::le as a function parameter because F
     * is invariant and would need to unify across Int, String, and Long.
     */
    private object OrderDef : YawnTableDef<OrderEntity, OrderEntity>(YawnTableDefParent.RootTableDefParent) {
        val amount: ColumnDef<Int> = ColumnDef("amount")
        val createdAt: ColumnDef<String> = ColumnDef("created_at")
        val itemCount: ColumnDef<Long> = ColumnDef("item_count")
    }

    /**
     * A helper that applies the same comparison to columns of different types.
     * This is the pattern YawnComparison enables: F is resolved independently
     * at each compare() call site, so a single comparison value works across
     * ColumnDef<Int>, ColumnDef<String>, and ColumnDef<Long>.
     *
     * This cannot be expressed with a Kotlin function type parameter like
     * `(YawnDef<SOURCE, *>.YawnColumnDef<F>, F & Any) -> YawnQueryCriterion<SOURCE>`
     * because F would have to be fixed for the entire function signature.
     */
    private fun buildOrderCriteria(
        comparison: YawnComparison,
        amount: Int,
        createdAt: String,
        itemCount: Long,
    ): List<YawnQueryCriterion<OrderEntity>> {
        return listOf(
            comparison.compare(OrderDef.amount, amount),
            comparison.compare(OrderDef.createdAt, createdAt),
            comparison.compare(OrderDef.itemCount, itemCount),
        )
    }

    @Test
    fun `same comparison applies to columns of different types`() {
        val criteria = buildOrderCriteria(
            comparison = YawnComparison.LE,
            amount = 100,
            createdAt = "2026-01-01",
            itemCount = 50L,
        )

        assertThat(criteria).hasSize(3)
        assertThat(criteria).allSatisfy { criterion ->
            assertThat(criterion.yawnRestriction)
                .isInstanceOf(YawnQueryRestriction.LessThanOrEqualTo::class.java)
        }
    }

    @Test
    fun `switching comparison changes all generated restrictions`() {
        val leCriteria = buildOrderCriteria(YawnComparison.LE, 100, "2026-01-01", 50L)
        val gtCriteria = buildOrderCriteria(YawnComparison.GT, 100, "2026-01-01", 50L)

        assertThat(leCriteria).allSatisfy { criterion ->
            assertThat(criterion.yawnRestriction)
                .isInstanceOf(YawnQueryRestriction.LessThanOrEqualTo::class.java)
        }
        assertThat(gtCriteria).allSatisfy { criterion ->
            assertThat(criterion.yawnRestriction)
                .isInstanceOf(YawnQueryRestriction.GreaterThan::class.java)
        }
    }

    @Test
    fun `each variant produces the expected restriction type`() {
        val expected = mapOf(
            YawnComparison.EQ to YawnQueryRestriction.Equals::class.java,
            YawnComparison.LT to YawnQueryRestriction.LessThan::class.java,
            YawnComparison.LE to YawnQueryRestriction.LessThanOrEqualTo::class.java,
            YawnComparison.GT to YawnQueryRestriction.GreaterThan::class.java,
            YawnComparison.GE to YawnQueryRestriction.GreaterThanOrEqualTo::class.java,
        )

        for ((comparison, restrictionClass) in expected) {
            val criterion = comparison.compare(OrderDef.amount, 42)
            assertThat(criterion.yawnRestriction)
                .`as`("YawnComparison.%s", comparison.name)
                .isInstanceOf(restrictionClass)
        }
    }

    @Test
    fun `all comparison variants are present`() {
        assertThat(YawnComparison.entries).containsExactly(
            YawnComparison.EQ,
            YawnComparison.LT,
            YawnComparison.LE,
            YawnComparison.GT,
            YawnComparison.GE,
        )
    }
}
