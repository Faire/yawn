package com.faire.yawn.database

import com.faire.yawn.project.YawnProjections
import com.faire.yawn.setup.entities.BookTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class YawnAggregationNullabilityTest : BaseYawnDatabaseTest() {
    @Test
    fun `count with addIsNotNull`() {
        transactor.open { session ->
            val count = session.project(BookTable) { books ->
                val notes = addIsNotNull(books.notes)
                project(YawnProjections.count(notes))
            }.uniqueResult()!!

            // 3 books have notes: harry potter, lord of the rings, the hobbit
            assertThat(count).isEqualTo(3)
        }
    }

    @Test
    fun `countDistinct with addIsNotNull`() {
        transactor.open { session ->
            val distinctCount = session.project(BookTable) { books ->
                val notes = addIsNotNull(books.notes)
                project(YawnProjections.countDistinct(notes))
            }.uniqueResult()!!

            // harry potter and the hobbit share the same note
            assertThat(distinctCount).isEqualTo(2)
        }
    }

    @Test
    fun `sum with addIsNotNull`() {
        transactor.open { session ->
            val total = session.project(BookTable) { books ->
                val rating = addIsNotNull(books.rating)
                project(YawnProjections.sum(rating))
            }.uniqueResult()!!

            // lord of the rings (10) + the hobbit (9)
            assertThat(total).isEqualTo(19)
        }
    }

    @Test
    fun `avg with addIsNotNull`() {
        transactor.open { session ->
            val average = session.project(BookTable) { books ->
                val rating = addIsNotNull(books.rating)
                project(YawnProjections.avg(rating))
            }.uniqueResult()!!

            // (10 + 9) / 2
            assertThat(average).isEqualTo(9.5)
        }
    }

    @Test
    fun `max with addIsNotNull`() {
        transactor.open { session ->
            val maxRating = session.project(BookTable) { books ->
                val rating = addIsNotNull(books.rating)
                project(YawnProjections.max(rating))
            }.uniqueResult()!!

            assertThat(maxRating).isEqualTo(10)
        }
    }

    @Test
    fun `min with addIsNotNull`() {
        transactor.open { session ->
            val minRating = session.project(BookTable) { books ->
                val rating = addIsNotNull(books.rating)
                project(YawnProjections.min(rating))
            }.uniqueResult()!!

            assertThat(minRating).isEqualTo(9)
        }
    }

    @Test
    fun `groupBy with addIsNotNull`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                val notes = addIsNotNull(books.notes)
                val rating = addIsNotNull(books.rating)
                project(
                    YawnProjections.pair(
                        YawnProjections.groupBy(notes),
                        YawnProjections.sum(rating),
                    ),
                )
            }.list()

            // lord of the rings has rating 10, the hobbit has rating 9
            assertThat(results).containsExactlyInAnyOrder(
                "Note for The Hobbit and Harry Potter" to 9L,
                "Note for Lord of the Rings" to 10L,
            )
        }
    }
}
