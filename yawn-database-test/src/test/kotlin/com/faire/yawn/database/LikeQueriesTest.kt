package com.faire.yawn.database

import com.faire.yawn.query.YawnRestrictions
import com.faire.yawn.setup.entities.BookTable
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.criterion.MatchMode
import org.junit.jupiter.api.Test

internal class LikeQueriesTest : BaseYawnDatabaseTest() {
    @Test
    fun `like - books starting with The`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                addLike(books.name, "The %")
            }.list()

            assertThat(results.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "The Emperor's New Clothes",
                "The Little Mermaid",
                "The Ugly Duckling",
            )
        }
    }

    @Test
    fun `like is case-sensitive - books starting with the`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                addLike(books.name, "the %")
            }.list()

            assertThat(results).isEmpty()
        }
    }

    @Test
    fun `iLike is case-insensitive - books starting with the`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                addILike(books.name, "the %")
            }.list()

            assertThat(results.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "The Emperor's New Clothes",
                "The Little Mermaid",
                "The Ugly Duckling",
            )
        }
    }

    @Test
    fun `not like - authors that do not start with J`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                val authors = join(books.author) // people who have books
                addNotLike(authors.name, "J.%")
                project(authors.name)
            }.set()

            assertThat(results).containsExactlyInAnyOrder(
                "Hans Christian Andersen",
            )
        }
    }

    @Test
    fun `not like is case-sensitive - authors that do not start with j`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                val authors = join(books.author) // people who have books
                addNotLike(authors.name, "j.%")
                project(authors.name)
            }.set()

            assertThat(results).containsExactlyInAnyOrder(
                "Hans Christian Andersen",
                "J.K. Rowling",
                "J.R.R. Tolkien",
            )
        }
    }

    @Test
    fun `not iLike is case-insensitive - authors that do not start with j`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                val authors = join(books.author) // people who have books
                addNotILike(authors.name, "j.%")
                project(authors.name)
            }.set()

            assertThat(results).containsExactlyInAnyOrder(
                "Hans Christian Andersen",
            )
        }
    }

    @Test
    fun `like on non-nullable column - authors starting with JRR`() {
        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                val authors = join(books.author)
                addLike(authors.name, "J.R.R%")
            }.list()

            assertThat(books.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "Lord of the Rings",
            )
        }
    }

    @Test
    fun `like on nullable column - notes starting with Note for`() {
        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                addLike(books.notes, "Note for%")
            }.list()

            assertThat(books.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "Lord of the Rings",
                "Harry Potter",
            )
        }
    }

    @Test
    fun `like with match mode on non-nullable column - authors starting with JRR`() {
        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                val authors = join(books.author)
                addLike(authors.name, "J.R.R", MatchMode.START)
            }.list()

            assertThat(books.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "Lord of the Rings",
            )
        }
    }

    @Test
    fun `like with match mode END - books ending in Mermaid`() {
        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                addLike(books.name, "Mermaid", MatchMode.END)
            }.list()

            assertThat(books.map { it.name }).containsExactlyInAnyOrder(
                "The Little Mermaid",
            )
        }
    }

    @Test
    fun `like with match mode on nullable column - notes starting with Note for`() {
        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                addLike(books.notes, "Note for", MatchMode.START)
            }.list()

            assertThat(books.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "Lord of the Rings",
                "Harry Potter",
            )
        }
    }

    @Test
    fun `iLike on non-nullable column - authors starting with JRR`() {
        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                val authors = join(books.author)
                addILike(authors.name, "j.r.r%")
            }.list()

            assertThat(books.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "Lord of the Rings",
            )
        }
    }

    @Test
    fun `iLike on nullable column - notes starting with Note for`() {
        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                addILike(books.notes, "nOtE FoR%")
            }.list()

            assertThat(books.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "Lord of the Rings",
                "Harry Potter",
            )
        }
    }

    @Test
    fun `iLike with match mode on non-nullable column - authors starting with JRR`() {
        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                val authors = join(books.author)
                addILike(authors.name, "j.r.r", MatchMode.START)
            }.list()

            assertThat(books.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "Lord of the Rings",
            )
        }
    }

    @Test
    fun `iLike with match mode on nullable column - notes starting with Note for`() {
        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                addILike(books.notes, "nOtE fOr", MatchMode.START)
            }.list()

            assertThat(books.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "Lord of the Rings",
                "Harry Potter",
            )
        }
    }

    @Test
    fun `like via restriction - books starting with The`() {
        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                add(YawnRestrictions.like(books.name, "The %"))
            }.list()

            assertThat(books.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "The Emperor's New Clothes",
                "The Little Mermaid",
                "The Ugly Duckling",
            )
        }
    }

    @Test
    fun `iLike via restriction - books starting with the`() {
        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                add(YawnRestrictions.iLike(books.name, "the %"))
            }.list()

            assertThat(books.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "The Emperor's New Clothes",
                "The Little Mermaid",
                "The Ugly Duckling",
            )
        }
    }
}
