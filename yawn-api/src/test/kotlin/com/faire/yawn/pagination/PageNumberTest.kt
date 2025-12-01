package com.faire.yawn.pagination

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class PageNumberTest {
    @Test
    fun `can create zero-indexed page numbers`() {
        val firstPage = PageNumber.zeroIndexed(0)
        assertThat(firstPage).isEqualTo(PageNumber.oneIndexed(1))
        assertThat(firstPage.zeroIndexedPageNumber).isEqualTo(0)
        assertThat(firstPage.oneIndexedPageNumber).isEqualTo(1)

        val secondPage = PageNumber.zeroIndexed(1)
        assertThat(secondPage).isEqualTo(PageNumber.oneIndexed(2))
        assertThat(secondPage.zeroIndexedPageNumber).isEqualTo(1)
        assertThat(secondPage.oneIndexedPageNumber).isEqualTo(2)
    }

    @Test
    fun `can create one-indexed page numbers`() {
        val firstPage = PageNumber.oneIndexed(1)
        assertThat(firstPage).isEqualTo(PageNumber.zeroIndexed(0))
        assertThat(firstPage.zeroIndexedPageNumber).isEqualTo(0)
        assertThat(firstPage.oneIndexedPageNumber).isEqualTo(1)

        val secondPage = PageNumber.oneIndexed(2)
        assertThat(secondPage).isEqualTo(PageNumber.zeroIndexed(1))
        assertThat(secondPage.zeroIndexedPageNumber).isEqualTo(1)
        assertThat(secondPage.oneIndexedPageNumber).isEqualTo(2)
    }

    @Test
    fun `can use starting and next helpers`() {
        val startingPage = PageNumber.starting()
        assertThat(startingPage).isEqualTo(PageNumber.zeroIndexed(0))
        assertThat(startingPage.zeroIndexedPageNumber).isEqualTo(0)
        assertThat(startingPage.oneIndexedPageNumber).isEqualTo(1)

        val nextPage = startingPage.next()
        assertThat(nextPage).isEqualTo(PageNumber.zeroIndexed(1))
        assertThat(nextPage.zeroIndexedPageNumber).isEqualTo(1)
        assertThat(nextPage.oneIndexedPageNumber).isEqualTo(2)
    }

    @Test
    fun `cannot create invalid page numbers`() {
        assertThatThrownBy { PageNumber.zeroIndexed(-1) }
            .isInstanceOf(IllegalStateException::class.java)
            .withFailMessage("-1 is not a valid zero-indexed page number")
        assertThatThrownBy { PageNumber.oneIndexed(0) }
            .isInstanceOf(IllegalStateException::class.java)
            .withFailMessage("-1 is not a valid zero-indexed page number")
    }
}
