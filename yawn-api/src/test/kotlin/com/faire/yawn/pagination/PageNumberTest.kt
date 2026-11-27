package com.faire.yawn.pagination

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class PageNumberTest {
    @Test
    fun `can create zero-indexed page numbers`() {
        val firstPage = PageNumber.zeroIndexed(0)
        with(firstPage) {
            assertThat(this).isEqualTo(PageNumber.oneIndexed(1))
            assertThat(zeroIndexedPageNumber).isEqualTo(0)
            assertThat(oneIndexedPageNumber).isEqualTo(1)
        }

        val secondPage = PageNumber.zeroIndexed(1)
        with(secondPage) {
            assertThat(this).isEqualTo(PageNumber.oneIndexed(2))
            assertThat(zeroIndexedPageNumber).isEqualTo(1)
            assertThat(oneIndexedPageNumber).isEqualTo(2)
        }
    }

    @Test
    fun `can create one-indexed page numbers`() {
        val firstPage = PageNumber.oneIndexed(1)
        with(firstPage) {
            assertThat(this).isEqualTo(PageNumber.zeroIndexed(0))
            assertThat(zeroIndexedPageNumber).isEqualTo(0)
            assertThat(oneIndexedPageNumber).isEqualTo(1)
        }

        val secondPage = PageNumber.oneIndexed(2)
        with(secondPage) {
            assertThat(this).isEqualTo(PageNumber.zeroIndexed(1))
            assertThat(zeroIndexedPageNumber).isEqualTo(1)
            assertThat(oneIndexedPageNumber).isEqualTo(2)
        }
    }

    @Test
    fun `can use starting and next helpers`() {
        val startingPage = PageNumber.starting()

        with(startingPage) {
            assertThat(this).isEqualTo(PageNumber.zeroIndexed(0))
            assertThat(zeroIndexedPageNumber).isEqualTo(0)
            assertThat(oneIndexedPageNumber).isEqualTo(1)
        }

        val nextPage = startingPage.next()
        with(nextPage) {
            assertThat(this).isEqualTo(PageNumber.zeroIndexed(1))
            assertThat(zeroIndexedPageNumber).isEqualTo(1)
            assertThat(oneIndexedPageNumber).isEqualTo(2)
        }
    }

    @Test
    fun `cannot create invalid page numbers`() {
        assertThatThrownBy { PageNumber.zeroIndexed(-1) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("-1 is not a valid zero-indexed page number")
        assertThatThrownBy { PageNumber.oneIndexed(0) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("0 is not a valid one-indexed page number")
        assertThatThrownBy { PageNumber.oneIndexed(Int.MIN_VALUE) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("${Int.MIN_VALUE} is not a valid one-indexed page number")
    }

    @Test
    fun `can use Long overloads to create page numbers`() {
        assertThat(PageNumber.zeroIndexed(0L).zeroIndexedPageNumber).isEqualTo(0)
        assertThat(PageNumber.oneIndexed(1L).oneIndexedPageNumber).isEqualTo(1)
        assertThat((PageNumber.starting() / 10L).pageSize).isEqualTo(10)
    }

    @Test
    fun `Long overloads reject values outside the Int range instead of truncating them`() {
        // 4_294_967_297 is 2^32 + 1, which truncates to 1 when converted to an Int
        assertThatThrownBy { PageNumber.zeroIndexed(4_294_967_297L) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("4294967297 is not a valid zero-indexed page number")
        assertThatThrownBy { PageNumber.zeroIndexed(-4_294_967_295L) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("-4294967295 is not a valid zero-indexed page number")
        assertThatThrownBy { PageNumber.oneIndexed(4_294_967_298L) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("4294967298 is not a valid one-indexed page number")
        assertThatThrownBy { PageNumber.starting() / 4_294_967_297L }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("4294967297 is not a valid page size")
    }
}
