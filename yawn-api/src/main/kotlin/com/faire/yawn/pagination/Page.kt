package com.faire.yawn.pagination

/**
 * A type-safe wrapper over a page number and size for page-based pagination.
 * Built on top of [PageNumber].
 */
data class Page(
    val pageNumber: PageNumber,
    val pageSize: Int,
) {
    init {
        check(pageSize >= 1) { "$pageSize is not a valid page size" }
    }

    fun computeOffset(): Int = pageNumber.zeroIndexedPageNumber * pageSize

    fun next(): Page = Page(pageNumber = pageNumber.next(), pageSize = pageSize)

    fun <T : Any> toResults(
        totalResults: Long,
        results: List<T>,
    ): PaginationResult<T> = PaginationResult(
        totalResults = totalResults,
        results = results,
        page = this,
    )
}
