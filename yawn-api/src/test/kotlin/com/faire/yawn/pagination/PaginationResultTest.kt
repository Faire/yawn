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

        with(result) {
            assertThat(totalResults).isEqualTo(100)
            assertThat(results).containsExactly(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
            assertThat(page.pageNumber.zeroIndexedPageNumber).isEqualTo(0)
            assertThat(page.pageSize).isEqualTo(10)
        }

        with(result.map { it * 2 }) {
            assertThat(totalResults).isEqualTo(100)
            assertThat(results).containsExactly(0, 2, 4, 6, 8, 10, 12, 14, 16, 18)
            assertThat(page.pageNumber.zeroIndexedPageNumber).isEqualTo(0)
            assertThat(page.pageSize).isEqualTo(10)
        }

        with(result.mapResults { results -> results.filter { it % 2 == 0 } }) {
            assertThat(totalResults).isEqualTo(100)
            assertThat(results).containsExactly(0, 2, 4, 6, 8)
            assertThat(page.pageNumber.zeroIndexedPageNumber).isEqualTo(0)
            assertThat(page.pageSize).isEqualTo(10)
        }
    }

    @Test
    fun `can create PaginationResult from list`() {
        val elements = List(26) { it }

        val startingPage = PageNumber.starting() / 10

        with(PaginationResult.fromList(elements = elements, page = startingPage)) {
            assertThat(totalResults).isEqualTo(26)
            assertThat(results).containsExactlyElementsOf(0..9)
            assertThat(page.pageNumber.zeroIndexedPageNumber).isEqualTo(0)
            assertThat(page.pageSize).isEqualTo(10)
        }
        with(PaginationResult.fromList(elements = elements, page = startingPage.next())) {
            assertThat(totalResults).isEqualTo(26)
            assertThat(results).containsExactlyElementsOf(10..19)
            assertThat(page.pageNumber.zeroIndexedPageNumber).isEqualTo(1)
            assertThat(page.pageSize).isEqualTo(10)
        }
        with(PaginationResult.fromList(elements = elements, page = startingPage.next().next())) {
            assertThat(totalResults).isEqualTo(26)
            assertThat(results).containsExactlyElementsOf(20..25)
            assertThat(page.pageNumber.zeroIndexedPageNumber).isEqualTo(2)
            assertThat(page.pageSize).isEqualTo(10)
        }
    }

    @Test
    fun `can create empty PaginationResult`() {
        val page = PageNumber.starting() / 10
        with(PaginationResult.empty<Int>(page = page)) {
            assertThat(totalResults).isEqualTo(0)
            assertThat(results).isEmpty()
            assertThat(page.pageNumber.zeroIndexedPageNumber).isEqualTo(0)
            assertThat(page.pageSize).isEqualTo(10)
        }
    }
}
