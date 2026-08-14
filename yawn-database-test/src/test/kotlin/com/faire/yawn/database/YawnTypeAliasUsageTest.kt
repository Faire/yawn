package com.faire.yawn.database

import com.faire.yawn.Yawn
import com.faire.yawn.criteria.query.YawnQueryScopeWithWhere
import com.faire.yawn.project.YawnProjections
import com.faire.yawn.project.YawnQueryProjection
import com.faire.yawn.setup.entities.Book
import com.faire.yawn.setup.entities.BookEntityQueryScope
import com.faire.yawn.setup.entities.BookProjectedQueryScope
import com.faire.yawn.setup.entities.BookTable
import com.faire.yawn.setup.entities.BookTableDef
import com.faire.yawn.setup.entities.BookTableDefType
import com.faire.yawn.setup.entities.PersonTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test the generated scope typealiases against examples of the query fragments that they are intended to simplify.
 */
internal class YawnTypeAliasUsageTest : BaseYawnDatabaseTest() {
    /**
     * Share one or more filter conditions across entity queries.
     */
    private fun BookEntityQueryScope.filterShortBooks(books: BookTableDefType) {
        addLt(books.numberOfPages, 500)
    }

    @Test
    fun `entity scope alias shares filtering between queries`() {
        transactor.open { session ->
            val hobbit = session.query(BookTable) { books ->
                filterShortBooks(books)
                addEq(books.name, "The Hobbit")
            }.uniqueResult()!!

            assertThat(hobbit.name).isEqualTo("The Hobbit")

            val allShort = session.query(BookTable) { books ->
                filterShortBooks(books)
            }.list()

            assertThat(allShort.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "The Little Mermaid",
                "The Ugly Duckling",
                "The Emperor's New Clothes",
            )
        }
    }

    /**
     * Share a projection, rather than a filter. It joins too, so the scope is actually used.
     */
    private fun BookProjectedQueryScope<Pair<String, Long>>.authorAndTotalPages(
        books: BookTableDefType,
    ): YawnQueryProjection<Book, Pair<String, Long>> {
        val authors = join(books.author)
        return YawnProjections.pair(
            YawnProjections.groupBy(authors.name),
            YawnProjections.sum(books.numberOfPages),
        )
    }

    @Test
    fun `projected scope alias shares a projection that uses the scope`() {
        transactor.open { session ->
            val everyAuthor = session.project(BookTable) { books ->
                project(authorAndTotalPages(books))
            }.list()

            assertThat(everyAuthor).containsExactlyInAnyOrder(
                "J.R.R. Tolkien" to 1_300L,
                "J.K. Rowling" to 500L,
                "Hans Christian Andersen" to 330L,
            )

            // same helper, query filters first
            val shortBooksOnly = session.project(BookTable) { books ->
                addLt(books.numberOfPages, 500)
                project(authorAndTotalPages(books))
            }.list()

            assertThat(shortBooksOnly).containsExactlyInAnyOrder(
                "J.R.R. Tolkien" to 300L,
                "Hans Christian Andersen" to 330L,
            )
        }
    }

    /**
     * Share one or more filter conditions across projected queries.
     */
    private fun BookProjectedQueryScope<*>.filterToAuthor(
        books: BookTableDefType,
        authorName: String,
    ) {
        val authors = join(books.author)
        addEq(authors.name, authorName)
    }

    @Test
    fun `projected scope alias works for a detached criteria`() {
        val shortBookNames = Yawn.createProjectedDetachedCriteria(BookTable) { books ->
            filterToAuthor(books, "Hans Christian Andersen")
            project(books.name)
        }

        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                addIn(books.name, shortBookNames)
            }.list()

            assertThat(books.map { it.name }).containsExactlyInAnyOrder(
                "The Little Mermaid",
                "The Ugly Duckling",
                "The Emperor's New Clothes",
            )
        }
    }

    /**
     * Join scopes get no alias: their source is the *enclosing* entity, so it cannot be pinned. Filtering is all they
     * can do anyway, and every scope offers that through [YawnQueryScopeWithWhere].
     */
    private fun <SOURCE : Any> YawnQueryScopeWithWhere<SOURCE, Book>.filterLongBooks(
        books: BookTableDef<SOURCE>,
    ) {
        addGt(books.numberOfPages, 500)
    }

    @Test
    fun `a filter can be shared into a join, and with the other scopes`() {
        transactor.open { session ->
            val paul = session.query(PersonTable) { people ->
                addEq(people.name, "Paul Duchesne")
                join(people.favoriteBook) { books ->
                    filterLongBooks(books)
                }
            }.uniqueResult()!!

            assertThat(paul.favoriteBook!!.name).isEqualTo("Lord of the Rings")

            // same helper, now in an entity query rooted at Book
            val longBooks = session.query(BookTable) { books ->
                filterLongBooks(books)
            }.list()

            assertThat(longBooks.map { it.name }).containsExactly("Lord of the Rings")
        }
    }
}
