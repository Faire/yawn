package com.faire.yawn.pagination

/**
 * A wrapper over a page number, standardizing it as a validated, index-agnostic Int.
 * Adds convenient helpers and transformations for all your needs.
 */
@JvmInline
value class PageNumber private constructor(val zeroIndexedPageNumber: Int) {
    init {
        check(zeroIndexedPageNumber >= 0) { "$zeroIndexedPageNumber is not a valid zero-indexed page number" }
    }

    /**
     * Returns the subsequent page number to this.
     * NOTE: does not validate existence - see [PaginationResult.hasNext] for that.
     */
    fun next(): PageNumber = PageNumber(zeroIndexedPageNumber = zeroIndexedPageNumber + 1)

    val oneIndexedPageNumber: Int
        get() = zeroIndexedPageNumber + 1

    operator fun div(pageSize: Int): Page = Page(pageNumber = this, pageSize = pageSize)
    operator fun div(pageSize: Long): Page {
        check(pageSize in 1..Int.MAX_VALUE) { "$pageSize is not a valid page size" }
        return this / pageSize.toInt()
    }

    companion object {
        fun zeroIndexed(pageNumber: Int): PageNumber = PageNumber(zeroIndexedPageNumber = pageNumber)
        fun zeroIndexed(pageNumber: Long): PageNumber {
            check(pageNumber in 0..Int.MAX_VALUE) { "$pageNumber is not a valid zero-indexed page number" }
            return zeroIndexed(pageNumber.toInt())
        }

        fun oneIndexed(pageNumber: Int): PageNumber {
            check(pageNumber >= 1) { "$pageNumber is not a valid one-indexed page number" }
            return PageNumber(zeroIndexedPageNumber = pageNumber - 1)
        }
        fun oneIndexed(pageNumber: Long): PageNumber {
            check(pageNumber in 1..Int.MAX_VALUE) { "$pageNumber is not a valid one-indexed page number" }
            return oneIndexed(pageNumber.toInt())
        }

        fun starting(): PageNumber = PageNumber(zeroIndexedPageNumber = 0)
    }
}
