package com.faire.yawn.database

import com.faire.yawn.pagination.Page
import com.faire.yawn.pagination.PageNumber
import com.faire.yawn.pagination.PaginationResult
import com.faire.yawn.query.YawnQueryOrder
import com.faire.yawn.setup.entities.BookTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class YawnPaginationQueriesTest : BaseYawnDatabaseTest() {
    @Test
    fun `count distinct`() {
        transactor.open { session ->
            val count = session.query(BookTable)
                .countDistinct { originalLanguage }
            assertThat(count).isEqualTo(2)
        }
    }

    @Test
    fun `list paginated`() {
        transactor.open { session ->
            fun paginate(page: Page): List<String> {
                return session.query(BookTable)
                    .listPaginated(
                        page = page,
                        orders = listOf(
                            { YawnQueryOrder.asc(originalLanguage) },
                            { YawnQueryOrder.desc(name) },
                        ),
                    )
                    .map { it.name }
            }

            val books1 = paginate(PageNumber.zeroIndexed(0) / 2)
            assertThat(books1).containsExactly("The Ugly Duckling", "The Little Mermaid")

            val books2 = paginate(PageNumber.zeroIndexed(1) / 2)
            assertThat(books2).containsExactly("The Emperor's New Clothes", "The Hobbit")

            val books3 = paginate(PageNumber.zeroIndexed(2) / 2)
            assertThat(books3).containsExactly("Lord of the Rings", "Harry Potter")

            val book4 = paginate(PageNumber.zeroIndexed(3) / 2)
            assertThat(book4).isEmpty()

            val bigPage1 = paginate(PageNumber.zeroIndexed(0) / 4)
            assertThat(bigPage1).containsExactly(
                "The Ugly Duckling",
                "The Little Mermaid",
                "The Emperor's New Clothes",
                "The Hobbit",
            )

            val bigPage2 = paginate(PageNumber.zeroIndexed(1) / 4)
            assertThat(bigPage2).containsExactly(
                "Lord of the Rings",
                "Harry Potter",
            )

            val hugePage = paginate(PageNumber.zeroIndexed(2) / 100)
            assertThat(hugePage).isEmpty()
        }
    }

    @Test
    fun `set paginated`() {
        transactor.open { session ->
            fun paginate(page: Page): Set<String> {
                return session.query(BookTable)
                    .applyProjection { books ->
                        val authors = join(books.author)
                        project(authors.name)
                    }
                    .setPaginated(
                        page = page,
                        orders = listOf { YawnQueryOrder.asc(name) },
                    )
            }

            val authors = paginate(PageNumber.zeroIndexed(0) / 2) + paginate(PageNumber.zeroIndexed(1) / 2)
            assertThat(authors).containsExactlyInAnyOrder("Hans Christian Andersen", "J.K. Rowling", "J.R.R. Tolkien")
        }
    }

    @Test
    fun `list with total results`() {
        transactor.open { session ->
            fun paginate(page: Page): PaginationResult<String> {
                return session.query(BookTable)
                    .listPaginatedWithTotalResults(
                        page = page,
                        orders = listOf(
                            { YawnQueryOrder.asc(originalLanguage) },
                            { YawnQueryOrder.desc(name) },
                        ),
                        uniqueColumn = { id },
                    )
                    .map { it.name }
            }

            val (total1, books1) = paginate(PageNumber.zeroIndexed(0) / 2)
            assertThat(total1).isEqualTo(6)
            assertThat(books1).containsExactly("The Ugly Duckling", "The Little Mermaid")

            val (total2, books2) = paginate(PageNumber.zeroIndexed(1) / 2)
            assertThat(total2).isEqualTo(6)
            assertThat(books2).containsExactly("The Emperor's New Clothes", "The Hobbit")

            val (total3, books3) = paginate(PageNumber.zeroIndexed(2) / 2)
            assertThat(total3).isEqualTo(6)
            assertThat(books3).containsExactly("Lord of the Rings", "Harry Potter")

            val (total4, books4) = paginate(PageNumber.zeroIndexed(3) / 2)
            assertThat(total4).isEqualTo(6)
            assertThat(books4).isEmpty()

            val (totalBig1, bigPage1) = paginate(PageNumber.zeroIndexed(0) / 4)
            assertThat(totalBig1).isEqualTo(6)
            assertThat(bigPage1).containsExactly(
                "The Ugly Duckling",
                "The Little Mermaid",
                "The Emperor's New Clothes",
                "The Hobbit",
            )

            val (totalBig2, bigPage2) = paginate(PageNumber.zeroIndexed(1) / 4)
            assertThat(totalBig2).isEqualTo(6)
            assertThat(bigPage2).containsExactly(
                "Lord of the Rings",
                "Harry Potter",
            )

            val (totalHuge, hugePage) = paginate(PageNumber.zeroIndexed(2) / 100)
            assertThat(totalHuge).isEqualTo(6)
            assertThat(hugePage).isEmpty()
        }
    }

    @Test
    fun `list with join`() {
        transactor.open { session ->
            val (total, books) = session.query(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "Hans Christian Andersen")
            }.listPaginatedWithTotalResults(
                page = PageNumber.zeroIndexed(1) / 2,
                orders = listOf { YawnQueryOrder.desc(name) },
                uniqueColumn = { id },
            )

            assertThat(total).isEqualTo(3)
            assertThat(books.map { it.name }).containsExactly(
                "The Emperor's New Clothes",
            )
        }
    }

    @Test
    fun `do paginated`() {
        val results = mutableListOf<String>()

        transactor.open { session ->
            session.query(BookTable).doPaginated(
                pageSize = 2,
                orders = listOf { YawnQueryOrder.asc(name) },
                action = { books ->
                    for (book in books) {
                        results.add(book.author.name)
                    }
                },
            )
        }

        assertThat(results).containsExactly(
            "J.K. Rowling", // Harry Potter
            "J.R.R. Tolkien", // Lord of the Rings
            "Hans Christian Andersen", // The Emperor's New Clothes
            "J.R.R. Tolkien", // The Hobbit
            "Hans Christian Andersen", // The Little Mermaid
            "Hans Christian Andersen", // The Ugly Duckling
        )
    }

    @Test
    fun `list batched`() {
        transactor.open { session ->
            val results = session.query(BookTable).listBatched(
                batchSize = 2,
                orders = listOf { YawnQueryOrder.asc(name) },
            )

            assertThat(results.map { it.name }).containsExactly(
                "Harry Potter",
                "Lord of the Rings",
                "The Emperor's New Clothes",
                "The Hobbit",
                "The Little Mermaid",
                "The Ugly Duckling",
            )
        }
    }

    @Test
    fun `list batched - projection`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                project(books.name)
            }.listBatched(
                batchSize = 2,
                orders = listOf { YawnQueryOrder.asc(name) },
            )

            assertThat(results).containsExactly(
                "Harry Potter",
                "Lord of the Rings",
                "The Emperor's New Clothes",
                "The Hobbit",
                "The Little Mermaid",
                "The Ugly Duckling",
            )
        }
    }

    @Test
    fun `set batched`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                val authors = join(books.author)
                project(authors.name)
            }.setBatched(
                batchSize = 2,
                orders = listOf { YawnQueryOrder.asc(name) },
            )

            assertThat(results).containsExactly(
                "J.K. Rowling",
                "J.R.R. Tolkien",
                "Hans Christian Andersen",
            )
        }
    }

    @Test
    fun `paginate with projection`() {
        transactor.open { session ->
            val bookNames = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "Hans Christian Andersen")
                project(books.name)
            }.paginate(
                page = PageNumber.zeroIndexed(0) / 3,
                orders = listOf { YawnQueryOrder.desc(numberOfPages) },
            ).list()

            assertThat(bookNames).containsExactly(
                "The Emperor's New Clothes", // 120 pages
                "The Ugly Duckling", // 110 pages
                "The Little Mermaid", // 100 pages
            )
        }
    }

    @Test
    fun `do paginate with projection`() {
        val bookNames = mutableListOf<String>()
        transactor.open { session ->
            session.project(BookTable) { books ->
                project(books.name)
            }.doPaginated(
                pageSize = 3,
                orders = listOf { YawnQueryOrder.asc(numberOfPages) },
                action = { names ->
                    for ((idx, bookName) in names.withIndex()) {
                        bookNames.add("[$idx] $bookName")
                    }
                },
            )
        }

        assertThat(bookNames).containsExactly(
            "[0] The Little Mermaid", // 100 pages
            "[1] The Ugly Duckling", // 110 pages
            "[2] The Emperor's New Clothes", // 120 pages
            "[0] The Hobbit", // 300 pages
            "[1] Harry Potter", // 500 pages
            "[2] Lord of the Rings", // 1000 pages
        )
    }
}
