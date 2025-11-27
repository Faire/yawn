package com.faire.yawn.pagination

/**
 * A type-safe wrapper over a page number, standardizing it as an Int and making it indexing-agnostic, validated, and
 * adding some helper and convenience methods.
 */
@JvmInline
value class PageNumber private constructor(val zeroIndexedPageNumber: Int) {
    init {
        check(zeroIndexedPageNumber >= 0) { "$zeroIndexedPageNumber is not a valid page number" }
    }

    fun next(): PageNumber = PageNumber(zeroIndexedPageNumber = zeroIndexedPageNumber + 1)

    val oneIndexedPageNumber: Int
        get() = zeroIndexedPageNumber + 1

    operator fun div(pageSize: Int): Page = Page(pageNumber = this, pageSize = pageSize)

    companion object {
        fun zeroIndexed(pageNumber: Int): PageNumber = PageNumber(zeroIndexedPageNumber = pageNumber)
        fun oneIndexed(pageNumber: Int): PageNumber = PageNumber(zeroIndexedPageNumber = pageNumber - 1)

        fun starting(): PageNumber = PageNumber(zeroIndexedPageNumber = 0)
    }
}
