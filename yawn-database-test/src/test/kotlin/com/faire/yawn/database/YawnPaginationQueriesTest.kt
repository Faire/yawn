package com.faire.yawn.database

import com.faire.yawn.pagination.Page
import com.faire.yawn.pagination.PageNumber
import com.faire.yawn.pagination.PaginationResult
import com.faire.yawn.project.YawnPathProvider
import com.faire.yawn.project.YawnProjections
import com.faire.yawn.query.YawnQueryOrder
import com.faire.yawn.setup.entities.Book
import com.faire.yawn.setup.entities.BookClub
import com.faire.yawn.setup.entities.BookClubMemberTableDef
import com.faire.yawn.setup.entities.BookClubTable
import com.faire.yawn.setup.entities.BookTable
import com.faire.yawn.setup.entities.PersonTableDef
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hibernate.sql.JoinType
import org.junit.jupiter.api.Test
import java.sql.SQLException

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
    fun `list with total results - eager collection fan-out truncates without avoidEagerFetchFanout`() {
        transactor.open { session ->
            // The Andersen Fan Club has 5 members (see BookFixtures) - more than the page size below - so the
            // eager `members` collection's join fetch fans it out into enough SQL rows to exhaust the page's
            // LIMIT budget by itself. Without avoidEagerFetchFanout, the Rowling Fan Club is starved off this
            // page even though it should be the 2nd of 3 distinct book clubs, ordered ascending by name.
            val (total, bookClubs) = session.query(BookClubTable)
                .listPaginatedWithTotalResults(
                    page = PageNumber.zeroIndexed(0) / 2,
                    orders = listOf { YawnQueryOrder.asc(name) },
                    uniqueColumn = { id },
                )

            assertThat(total).isEqualTo(3)
            assertThat(bookClubs.map { it.name }.distinct()).containsExactly("Andersen Fan Club")
        }
    }

    @Test
    fun `list with total results - avoidEagerFetchFanout paginates by distinct entity, not fanned row`() {
        transactor.open { session ->
            fun paginate(page: Page): PaginationResult<String> {
                return session.query(BookClubTable)
                    .listPaginatedWithTotalResults(
                        page = page,
                        orders = listOf { YawnQueryOrder.asc(name) },
                        uniqueColumn = { id },
                        avoidEagerFetchFanout = true,
                    )
                    .map { it.name }
            }

            val (total1, page1) = paginate(PageNumber.zeroIndexed(0) / 2)
            assertThat(total1).isEqualTo(3)
            assertThat(page1).containsExactly("Andersen Fan Club", "Rowling Fan Club")

            val (total2, page2) = paginate(PageNumber.zeroIndexed(1) / 2)
            assertThat(total2).isEqualTo(3)
            assertThat(page2).containsExactly("Tolkien Fan Club")

            val (total3, page3) = paginate(PageNumber.zeroIndexed(2) / 2)
            assertThat(total3).isEqualTo(3)
            assertThat(page3).isEmpty()
        }
    }

    @Test
    fun `list with total results - avoidEagerFetchFanout preserves all fanned-out rows for the returned entity`() {
        transactor.open { session ->
            val (_, bookClubs) = session.query(BookClubTable)
                .listPaginatedWithTotalResults(
                    page = PageNumber.zeroIndexed(0) / 1,
                    orders = listOf { YawnQueryOrder.asc(name) },
                    uniqueColumn = { id },
                    avoidEagerFetchFanout = true,
                )

            val andersenFanClub = bookClubs.single()
            assertThat(andersenFanClub.name).isEqualTo("Andersen Fan Club")
            assertThat(andersenFanClub.members.map { it.name }).containsExactlyInAnyOrder(
                "Andersen Fan Club - Member 1",
                "Andersen Fan Club - Member 2",
                "Andersen Fan Club - Member 3",
                "Andersen Fan Club - Member 4",
                "Andersen Fan Club - Member 5",
            )
        }
    }

    @Test
    fun `list with total results - does not leak its pagination restrictions into the caller's builder`() {
        transactor.open { session ->
            val bookClubs = session.query(BookClubTable)
            bookClubs.listPaginatedWithTotalResults(
                page = PageNumber.zeroIndexed(0) / 2,
                orders = listOf { YawnQueryOrder.asc(name) },
                uniqueColumn = { id },
                avoidEagerFetchFanout = true,
            )

            // Before the fix the builder had inherited the page's `id IN (...)` filter and listed only two clubs.
            assertThat(bookClubs.list().map { it.name }.distinct())
                .containsExactlyInAnyOrder("Andersen Fan Club", "Rowling Fan Club", "Tolkien Fan Club")
        }
    }

    @Test
    fun `list with total results - avoidEagerFetchFanout pages an explicit collection join by distinct entity`() {
        transactor.open { session ->
            fun paginate(page: Page): PaginationResult<String> {
                return session.query(BookClubTable) { clubs ->
                    join(clubs.members, joinType = JoinType.LEFT_OUTER_JOIN)
                }.listPaginatedWithTotalResults(
                    page = page,
                    orders = listOf { YawnQueryOrder.asc(name) },
                    uniqueColumn = { id },
                    avoidEagerFetchFanout = true,
                ).map { it.name }
            }

            // An explicit join fans the page-of-keys query out as well, on any database: Hibernate only skips the
            // eager fetch there. Without the GROUP BY on the unique column, the Andersen Fan Club's 5 member rows
            // fill a page of 2 keys on their own and the Rowling Fan Club silently drops off the page.
            val (total1, page1) = paginate(PageNumber.zeroIndexed(0) / 2)
            assertThat(total1).isEqualTo(3)
            assertThat(page1).containsExactly("Andersen Fan Club", "Rowling Fan Club")

            val (total2, page2) = paginate(PageNumber.zeroIndexed(1) / 2)
            assertThat(total2).isEqualTo(3)
            assertThat(page2).containsExactly("Tolkien Fan Club")

            val (total3, page3) = paginate(PageNumber.zeroIndexed(2) / 2)
            assertThat(total3).isEqualTo(3)
            assertThat(page3).isEmpty()
        }
    }

    @Test
    fun `list with total results - forceAnsiCompliance pages an eager collection by distinct entity`() {
        transactor.open { session ->
            fun paginate(page: Page): PaginationResult<BookClub> {
                return session.query(BookClubTable)
                    .listPaginatedWithTotalResults(
                        page = page,
                        orders = listOf { YawnQueryOrder.asc(name) },
                        uniqueColumn = { id },
                        forceAnsiCompliance = true,
                    )
            }

            val (total1, page1) = paginate(PageNumber.zeroIndexed(0) / 2)
            assertThat(total1).isEqualTo(3)
            assertThat(page1.map { it.name }).containsExactly("Andersen Fan Club", "Rowling Fan Club")
            assertThat(page1.first().members.map { it.name }).containsExactlyInAnyOrder(
                "Andersen Fan Club - Member 1",
                "Andersen Fan Club - Member 2",
                "Andersen Fan Club - Member 3",
                "Andersen Fan Club - Member 4",
                "Andersen Fan Club - Member 5",
            )

            val (total2, page2) = paginate(PageNumber.zeroIndexed(1) / 2)
            assertThat(total2).isEqualTo(3)
            assertThat(page2.map { it.name }).containsExactly("Tolkien Fan Club")

            val (total3, page3) = paginate(PageNumber.zeroIndexed(2) / 2)
            assertThat(total3).isEqualTo(3)
            assertThat(page3).isEmpty()
        }
    }

    @Test
    fun `list with total results - forceAnsiCompliance supports ordering by a joined table's column`() {
        transactor.open { session ->
            fun paginate(page: Page): PaginationResult<String> {
                lateinit var authors: PersonTableDef<Book>
                return session.query(BookTable) { books ->
                    authors = join(books.author)
                }.listPaginatedWithTotalResults(
                    page = page,
                    orders = listOf(
                        { YawnQueryOrder.asc(authors.name) },
                        { YawnQueryOrder.desc(name) },
                    ),
                    uniqueColumn = { id },
                    forceAnsiCompliance = true,
                ).map { it.name }
            }

            val (total1, books1) = paginate(PageNumber.zeroIndexed(0) / 2)
            assertThat(total1).isEqualTo(6)
            assertThat(books1).containsExactly("The Ugly Duckling", "The Little Mermaid")

            val (total2, books2) = paginate(PageNumber.zeroIndexed(1) / 2)
            assertThat(total2).isEqualTo(6)
            assertThat(books2).containsExactly("The Emperor's New Clothes", "Harry Potter")

            val (total3, books3) = paginate(PageNumber.zeroIndexed(2) / 2)
            assertThat(total3).isEqualTo(6)
            assertThat(books3).containsExactly("The Hobbit", "Lord of the Rings")

            val (total4, books4) = paginate(PageNumber.zeroIndexed(3) / 2)
            assertThat(total4).isEqualTo(6)
            assertThat(books4).isEmpty()
        }
    }

    @Test
    fun `list with total results - forceAnsiCompliance groups by an order column of a joined collection`() {
        transactor.open { session ->
            // An INNER join on purpose: a LEFT join would page the member-less clubs first, because H2 sorts NULLs
            // first, and this test is about the club that actually has members to order by.
            lateinit var members: BookClubMemberTableDef<BookClub>
            val (total, results) = session.query(BookClubTable) { clubs ->
                members = join(clubs.members)
            }.listPaginatedWithTotalResults(
                page = PageNumber.zeroIndexed(0) / 2,
                orders = listOf { YawnQueryOrder.asc(members.name) },
                uniqueColumn = { id },
                forceAnsiCompliance = true,
            )

            // Ordering by a joined collection's column is exactly the case ANSI SQL rejects unless that column is
            // also grouped (see the guard test below). The page-of-keys query yields one row per (club, member
            // name), so a page of 2 keys collapses to a single club; that is inherent to the grouped shape and
            // matches the legacy helper.
            assertThat(total).isEqualTo(1)
            assertThat(results.map { it.name }).containsExactly("Andersen Fan Club")
        }
    }

    @Test
    fun `grouped query rejects an ungrouped order column of a joined collection on H2`() {
        transactor.open { session ->
            // This is the exact shape the test above would produce if forceAnsiCompliance stopped adding the order
            // column to the GROUP BY: H2 (like Postgres and MySQL's ONLY_FULL_GROUP_BY) rejects it at the database.
            assertThatThrownBy {
                session.project(BookClubTable) { clubs ->
                    val members = join(clubs.members)
                    order(YawnQueryOrder.asc(members.name))
                    project(YawnProjections.groupBy(clubs.id))
                }.list()
            }
                .rootCause()
                .isInstanceOf(SQLException::class.java)
                .hasMessageContaining("must be in the GROUP BY list")
        }
    }

    @Test
    fun `list with total results - forceAnsiCompliance rejects an order that is not a plain column`() {
        transactor.open { session ->
            assertThatThrownBy {
                session.query(BookClubTable).listPaginatedWithTotalResults(
                    page = PageNumber.zeroIndexed(0) / 2,
                    orders = listOf { YawnQueryOrder.asc(YawnPathProvider<BookClub> { "name" }) },
                    uniqueColumn = { id },
                    forceAnsiCompliance = true,
                )
            }
                .isInstanceOf(UnsupportedOperationException::class.java)
                .hasMessageContaining("requires every order to be a plain column")
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
