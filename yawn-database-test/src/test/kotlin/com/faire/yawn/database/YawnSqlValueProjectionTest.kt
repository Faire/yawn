package com.faire.yawn.database

import com.faire.yawn.project.YawnProjection
import com.faire.yawn.project.YawnProjections
import com.faire.yawn.setup.entities.BookCoverTable
import com.faire.yawn.setup.entities.BookProjectedQueryScope
import com.faire.yawn.setup.entities.BookTable
import com.faire.yawn.setup.entities.BookTableDefType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hibernate.HibernateException
import org.junit.jupiter.api.Test

internal class YawnSqlValueProjectionTest : BaseYawnDatabaseTest() {
    @YawnProjection
    internal data class AuthorPages(
        val author: String,
        val doubledPages: Long,
    )

    @Test
    fun `computed SUM expression nested in a generated projection`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                val authors = join(books.author)
                project(
                    YawnSqlValueProjectionTest_AuthorPagesProjection.create(
                        author = YawnProjections.groupBy(authors.name),
                        doubledPages = sqlValue { "SUM(${books.numberOfPages.sql} * 2)" },
                    ),
                )
            }.list()

            assertThat(results).containsExactlyInAnyOrder(
                AuthorPages("J.R.R. Tolkien", 2_600),
                AuthorPages("J.K. Rowling", 1_000),
                AuthorPages("Hans Christian Andersen", 660),
            )
        }
    }

    @Test
    fun `resolves a property whose column is mapped to a different name`() {
        transactor.open { session ->
            // Book.callNumber is @Column(name = "call_number"): naming the property would not resolve.
            val prefixes = session.project(BookTable) { books ->
                project(sqlValue<String?> { "LEFT(${books.callNumber.sql}, 2)" })
            }.list()

            assertThat(prefixes.toSet()).containsExactlyInAnyOrder("PR", "PZ", null)
        }
    }

    @Test
    fun `resolves properties on joined tables and embedded types`() {
        transactor.open { session ->
            val result = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(books.name, "The Hobbit")
                project(
                    YawnProjections.pair(
                        // joined table: resolves with the join's own alias
                        sqlValue<Long> { "LENGTH(${authors.name.sql})" },
                        // embedded property
                        sqlValue<Long> { "${books.sales.paperBacksSold.sql} / 1000" },
                    ),
                )
            }.uniqueResult()!!

            assertThat(result).isEqualTo(14L to 2_000L)
        }
    }

    @Test
    fun `nullable result type`() {
        transactor.open { session ->
            // Harry Potter has no rating, so the expression evaluates to NULL for it
            val ratings = session.project(BookTable) { books ->
                addIn(books.name, setOf("The Hobbit", "Harry Potter"))
                orderAsc(books.name)
                project(
                    YawnProjections.pair(
                        books.name,
                        sqlValue<Int?> { "${books.rating.sql} * 10" },
                    ),
                )
            }.list()

            assertThat(ratings).containsExactly(
                "Harry Potter" to null,
                "The Hobbit" to 90,
            )

            // and through uniqueResult()
            val nullRating = session.project(BookTable) { books ->
                addEq(books.name, "Harry Potter")
                project(sqlValue<Int?> { "${books.rating.sql} * 10" })
            }.uniqueResult()

            assertThat(nullRating).isNull()
        }
    }

    @Test
    fun `composes with ordinary columns and aggregations`() {
        transactor.open { session ->
            val result = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")
                project(
                    YawnProjections.triple(
                        YawnProjections.groupBy(authors.name),
                        YawnProjections.count(books.id),
                        sqlValue<Long> { "SUM(${books.numberOfPages.sql} * 2)" },
                    ),
                )
            }.uniqueResult()!!

            assertThat(result).isEqualTo(Triple("J.R.R. Tolkien", 2L, 2_600L))
        }
    }

    @YawnProjection
    internal data class Quad(
        val name: String,
        val first: Long,
        val second: Long,
        val pages: Long,
    )

    @Test
    fun `two different sql values keep their own result slots`() {
        transactor.open { session ->
            // Each gets its own generated alias, so neither reads the other's column, and the ordinary columns
            // on either side do not shift.
            val result = session.project(BookTable) { books ->
                addEq(books.name, "The Hobbit")
                project(
                    YawnSqlValueProjectionTest_QuadProjection.create(
                        books.name,
                        sqlValue { "111" },
                        sqlValue { "222" },
                        books.numberOfPages,
                    ),
                )
            }.uniqueResult()!!

            assertThat(result).isEqualTo(Quad("The Hobbit", 111L, 222L, 300L))
        }
    }

    /**
     * Share one across queries by writing a helper on the scope. Taking the table definition as a parameter is what
     * keeps it portable: an expression that captured a *joined* table's definition would bake in that query's alias.
     */
    private fun BookProjectedQueryScope<Long>.pagesPerBook(books: BookTableDefType) =
        sqlValue<Long> { "SUM(${books.numberOfPages.sql}) / COUNT(*)" }

    @Test
    fun `a projection can be shared across queries via a scope helper`() {
        transactor.open { session ->
            val tolkien = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")
                project(pagesPerBook(books))
            }.uniqueResult()!!
            assertThat(tolkien).isEqualTo(650L)

            val everyone = session.project(BookTable) { books ->
                project(pagesPerBook(books))
            }.uniqueResult()!!
            assertThat(everyone).isEqualTo(355L)
        }
    }

    @Test
    fun `a property backed by several columns cannot be a single value`() {
        transactor.open { session ->
            assertThatThrownBy {
                session.project(BookCoverTable) { covers ->
                    project(sqlValue<Long> { "LENGTH(${covers.cid.sql})" })
                }.list()
            }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("Path \"cid\" is a multi-column mapping backed by columns")
                .hasMessageContaining("(this_.book_id, this_.owner_id)")
                .hasMessageContaining("and thus has no single-value substitute into a SQL projection.")
                .hasMessageContaining("Reference one of its columns individually instead.")
        }
    }

    @Test
    fun `an individual column of a composite key can be a single value`() {
        transactor.open { session ->
            val bookTokens = session.project(BookCoverTable) { covers ->
                project(sqlValue<String> { "CONCAT('book_', ${covers.cid.bookId.sql})" })
            }.list()

            assertThat(bookTokens).containsExactlyInAnyOrder("book_1", "book_3")
        }
    }

    @Test
    fun `interpolating a column without sql cannot silently succeed`() {
        transactor.open { session ->
            // Forgetting the scoped `.sql` helper interpolates the ColumnDef's default toString(),
            // which ensures a sufficiently loud SQL error.
            assertThatThrownBy {
                session.project(BookTable) { books ->
                    // bad: must call `.sql` to resolve column name in context!
                    project(sqlValue<Long> { "SUM(${books.numberOfPages})" })
                }.uniqueResult()
            }.isInstanceOf(HibernateException::class.java)
        }
    }
}
