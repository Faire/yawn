package com.faire.yawn.pagination

data class PaginationResult<T : Any>(
    val totalResults: Long,
    val results: List<T>,
    val page: Page,
) {
    fun <R : Any> map(mapper: (T) -> R): PaginationResult<R> = PaginationResult(
        totalResults = totalResults,
        results = results.map(mapper),
        page = page,
    )

    companion object {
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
