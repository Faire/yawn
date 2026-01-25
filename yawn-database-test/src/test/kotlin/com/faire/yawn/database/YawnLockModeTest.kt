package com.faire.yawn.database

import com.faire.yawn.query.YawnLockMode
import com.faire.yawn.setup.entities.BookTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class YawnLockModeTest : BaseYawnDatabaseTest() {
    @Test
    fun `forUpdate() sets PESSIMISTIC_WRITE lock mode`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                addEq(books.name, "The Hobbit")
            }
                .forUpdate()
                .list()

            assertThat(results).hasSize(1)
            assertThat(results.single().name).isEqualTo("The Hobbit")
        }
    }

    @Test
    fun `forShare() sets PESSIMISTIC_READ lock mode`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                addEq(books.name, "The Hobbit")
            }
                .forShare()
                .list()

            assertThat(results).hasSize(1)
            assertThat(results.single().name).isEqualTo("The Hobbit")
        }
    }

    @Test
    fun `setLockMode() with PESSIMISTIC_WRITE`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                addEq(books.name, "The Hobbit")
            }
                .setLockMode(YawnLockMode.PESSIMISTIC_WRITE)
                .list()

            assertThat(results).hasSize(1)
            assertThat(results.single().name).isEqualTo("The Hobbit")
        }
    }

    @Test
    fun `setLockMode() with PESSIMISTIC_READ`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                addEq(books.name, "The Hobbit")
            }
                .setLockMode(YawnLockMode.PESSIMISTIC_READ)
                .list()

            assertThat(results).hasSize(1)
            assertThat(results.single().name).isEqualTo("The Hobbit")
        }
    }

    @Test
    fun `setLockMode() with NONE explicitly set`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                addEq(books.name, "The Hobbit")
            }
                .setLockMode(YawnLockMode.NONE)
                .list()

            assertThat(results).hasSize(1)
            assertThat(results.single().name).isEqualTo("The Hobbit")
        }
    }

    @Test
    fun `forUpdate() works with joins`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")
            }
                .forUpdate()
                .list()

            assertThat(results).hasSize(2)
            assertThat(results.map { it.name }).containsExactlyInAnyOrder("The Hobbit", "Lord of the Rings")
        }
    }

    @Test
    fun `forUpdate() works with uniqueResult()`() {
        transactor.open { session ->
            val result = session.query(BookTable) { books ->
                addEq(books.name, "The Hobbit")
            }
                .forUpdate()
                .uniqueResult()

            assertThat(result).isNotNull()
            assertThat(result!!.name).isEqualTo("The Hobbit")
        }
    }

    @Test
    fun `forUpdate() works with first()`() {
        transactor.open { session ->
            val result = session.query(BookTable) { books ->
                addEq(books.name, "The Hobbit")
            }
                .forUpdate()
                .first()

            assertThat(result).isNotNull()
            assertThat(result!!.name).isEqualTo("The Hobbit")
        }
    }

    @Test
    fun `forUpdate() can be combined with maxResults and offset`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                orderAsc(books.name)
            }
                .forUpdate()
                .maxResults(2)
                .offset(1)
                .list()

            assertThat(results).hasSize(2)
            assertThat(results.map { it.name }).containsExactly("Lord of the Rings", "The Emperor's New Clothes")
        }
    }
}
