package com.faire.yawn.database

import com.faire.yawn.query.YawnComparison
import com.faire.yawn.setup.entities.BookTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class YawnComparisonDatabaseTest : BaseYawnDatabaseTest() {

    @Test
    fun `compare filters books by numberOfPages with LT`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(YawnComparison.LT.compare(books.numberOfPages, 110L))
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder("The Little Mermaid")
        }
    }

    @Test
    fun `compare filters books by numberOfPages with LE`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(YawnComparison.LE.compare(books.numberOfPages, 110L))
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "The Little Mermaid",
                    "The Ugly Duckling",
                )
        }
    }

    @Test
    fun `compare filters books by numberOfPages with GT`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(YawnComparison.GT.compare(books.numberOfPages, 300L))
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "Harry Potter",
                    "Lord of the Rings",
                )
        }
    }

    @Test
    fun `compare filters books by name with EQ`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(YawnComparison.EQ.compare(books.name, "The Hobbit"))
            }.list()

            assertThat(results.single().name).isEqualTo("The Hobbit")
        }
    }

    /**
     * Demonstrates the core use case: a single YawnComparison parameter applied to
     * columns of different types (Long and String) in the same query. This is impossible
     * with a Kotlin function reference because F would need to unify across both column types.
     */
    @Test
    fun `same comparison applies to columns of different types in a single query`() {
        transactor.open { session ->
            val results = queryBooksFiltered(
                session,
                comparison = YawnComparison.GE,
                pageThreshold = 300L,
                nameThreshold = "The",
            )

            assertThat(results).containsExactlyInAnyOrder("The Hobbit")
        }
    }

    @Test
    fun `switching comparison changes query results`() {
        transactor.open { session ->
            val leResults = queryBooksFiltered(
                session,
                comparison = YawnComparison.LE,
                pageThreshold = 1000L,
                nameThreshold = "M",
            )
            val gtResults = queryBooksFiltered(
                session,
                comparison = YawnComparison.GT,
                pageThreshold = 1000L,
                nameThreshold = "M",
            )

            assertThat(leResults).containsExactlyInAnyOrder(
                "Harry Potter",
                "Lord of the Rings",
            )
            assertThat(gtResults).isEmpty()
        }
    }

    /**
     * Helper that applies the same comparison to columns of different types (Long and String).
     * This pattern is what YawnComparison enables — F is resolved independently at each call site.
     */
    private fun queryBooksFiltered(
        session: com.faire.yawn.setup.hibernate.YawnTestSession,
        comparison: YawnComparison,
        pageThreshold: Long,
        nameThreshold: String,
    ): List<String> {
        return session.query(BookTable) { books ->
            add(comparison.compare(books.numberOfPages, pageThreshold))
            add(comparison.compare(books.name, nameThreshold))
        }.list().map { it.name }
    }
}
