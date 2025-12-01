package com.faire.yawn.pagination

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PaginationResultTest {
    @Test
    fun `can create and map PaginationResult`() {
        val page = PageNumber.starting() / 10
        val result = page.toResults(
            totalResults = 100,
            results = List(10) { it },

        )

        assertThat(result.totalResults).isEqualTo(100)
        assertThat(result.results).containsExactly(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
        assertThat(result.page.pageNumber.zeroIndexedPageNumber).isEqualTo(0)
        assertThat(result.page.pageSize).isEqualTo(10)

        val mapped = result.map { it * 2 }
        assertThat(result.totalResults).isEqualTo(100)
        assertThat(mapped.results).containsExactly(0, 2, 4, 6, 8, 10, 12, 14, 16, 18)
        assertThat(result.page.pageNumber.zeroIndexedPageNumber).isEqualTo(0)
        assertThat(result.page.pageSize).isEqualTo(10)
    }
}
