package com.faire.yawn.pagination

import org.assertj.core.api.Assertions.assertThat
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
}
