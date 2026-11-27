package com.faire.yawn.pagination

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class PageTest {
    @Test
    fun `can create page from page number and obtain offset`() {
        val page = PageNumber.starting() / 10

        with(page) {
            assertThat(pageNumber.zeroIndexedPageNumber).isEqualTo(0)
            assertThat(pageSize).isEqualTo(10)

            assertThat(computeOffset()).isEqualTo(0)
            assertThat(next().computeOffset()).isEqualTo(10)
            assertThat(next().next().computeOffset()).isEqualTo(20)
        }
    }

    @Test
    fun `computes offsets up to the Int limit`() {
        val lastPage = PageNumber.zeroIndexed(Int.MAX_VALUE) / 1
        assertThat(lastPage.computeOffset()).isEqualTo(Int.MAX_VALUE)
    }

    @Test
    fun `rejects pages whose offset would overflow an Int`() {
        assertThatThrownBy { (PageNumber.zeroIndexed(1_000_000) / 5_000).computeOffset() }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage(
                "Zero-indexed page 1000000 of size 5000 starts at offset 5000000000, " +
                    "which exceeds the maximum supported offset of 2147483647",
            )
    }
}
