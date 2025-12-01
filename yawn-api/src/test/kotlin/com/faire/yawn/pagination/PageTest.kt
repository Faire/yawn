package com.faire.yawn.pagination

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PageTest {
    @Test
    fun `can create page from page number and obtain offset`() {
        val page = PageNumber.starting() / 10

        assertThat(page.pageNumber.zeroIndexedPageNumber).isEqualTo(0)
        assertThat(page.pageSize).isEqualTo(10)

        assertThat(page.computeOffset()).isEqualTo(0)
        assertThat(page.next().computeOffset()).isEqualTo(10)
        assertThat(page.next().next().computeOffset()).isEqualTo(20)
    }
}
