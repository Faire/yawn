package com.faire.yawn.pagination

/**
 * A wrapper over a validated pair of page number and page size, for page-based pagination.
 * Built on top of [PageNumber], which abstracts away the indexing convention.
 */
data class Page(
    val pageNumber: PageNumber,
    val pageSize: Int,
) {
    init {
        check(pageSize >= 1) { "$pageSize is not a valid page size" }
    }

    /**
     * The OFFSET to use in a paginated query for this page.
     * Hibernate takes the offset as an Int, so pages that would start past [Int.MAX_VALUE] are rejected.
     */
    fun computeOffset(): Int {
        val offset = pageNumber.zeroIndexedPageNumber.toLong() * pageSize
        check(offset <= Int.MAX_VALUE) {
            "Zero-indexed page ${pageNumber.zeroIndexedPageNumber} of size $pageSize starts at offset $offset, " +
                "which exceeds the maximum supported offset of ${Int.MAX_VALUE}"
        }
        return offset.toInt()
    }

    /**
     * Returns a [Page] representing the following page after this one, with the same [pageSize].
     * NOTE: does not validate existence - see [PaginationResult.hasNext] for that.
     */
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
