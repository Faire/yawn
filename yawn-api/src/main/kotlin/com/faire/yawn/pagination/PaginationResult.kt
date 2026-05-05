package com.faire.yawn.pagination

/**
 * A wrapper over paginated results of a query.
 *
 * @property totalResults the total number of results across all pages (not just the current page)
 * @property results the list of results for the current page
 * @property page the original page information used to produce this result
 */
data class PaginationResult<T : Any>(
    val totalResults: Long,
    val results: List<T>,
    val page: Page,
) {
    fun <R : Any> map(mapper: (T) -> R): PaginationResult<R> = mapResults { results.map(mapper) }

    fun <R : Any> mapResults(mapper: (List<T>) -> List<R>): PaginationResult<R> = PaginationResult(
        totalResults = totalResults,
        results = mapper(results),
        page = page,
    )

    /**
     * Whether there is a next page, i.e. whether this is not the last page.
     */
    fun hasNext(): Boolean {
        val nextOffset = page.next().computeOffset()
        return nextOffset < totalResults
    }

    companion object {
        fun <T : Any> empty(page: Page): PaginationResult<T> {
            return fromList(elements = listOf(), page = page)
        }

        fun <T : Any> fromList(
            elements: List<T>,
            page: Page,
        ): PaginationResult<T> = PaginationResult(
            totalResults = elements.size.toLong(),
            results = elements.asSequence()
                .drop(page.computeOffset())
                .take(page.pageSize)
                .toList(),
            page = page,
        )
    }
}
