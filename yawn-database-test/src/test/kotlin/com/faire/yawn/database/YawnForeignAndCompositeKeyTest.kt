package com.faire.yawn.database

import com.faire.yawn.Yawn
import com.faire.yawn.setup.entities.BookCoverCompositeId
import com.faire.yawn.setup.entities.BookCoverRankingTable
import com.faire.yawn.setup.entities.BookCoverTable
import com.faire.yawn.setup.entities.BookRankingTable
import com.faire.yawn.setup.entities.BookReviewTable
import com.faire.yawn.setup.entities.BookTable
import com.faire.yawn.setup.entities.PersonTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class YawnForeignAndCompositeKeyTest : BaseYawnDatabaseTest() {

    @Test
    fun `generate using the Id when foreign key not specified`() {
        transactor.open { session ->
            val bookId = session.project(BookTable) { books ->
                addEq(books.name, "Harry Potter")
                project(books.id)
            }.uniqueResult()!!

            val bookRanking = session.query(BookRankingTable) { bookRankings ->
                addEq(bookRankings.bestSeller.foreignKey, bookId)
            }.uniqueResult()!!

            assertThat(bookRanking.ratingYear).isEqualTo(2007)
            assertThat(bookRanking.ratingMonth).isEqualTo(1)
            assertThat(bookRanking.bestSeller.id).isEqualTo(bookId)
        }
    }

    @Test
    fun `simplified foreign key equals syntax`() {
        transactor.open { session ->
            val hpBookId = session.project(BookTable) { books ->
                addEq(books.name, "Harry Potter")
                project(books.id)
            }.uniqueResult()!!

            val hpRanking = session.query(BookRankingTable) { bookRankings ->
                addEq(bookRankings.bestSeller, hpBookId)
            }.uniqueResult()!!

            assertThat(hpRanking.ratingYear).isEqualTo(2007)
            assertThat(hpRanking.ratingMonth).isEqualTo(1)
            assertThat(hpRanking.bestSeller.id).isEqualTo(hpBookId)

            val lordOfTheRingsRanking = session.query(BookRankingTable) { bookRankings ->
                addNotEq(bookRankings.bestSeller, hpBookId)
            }.uniqueResult()!!

            assertThat(lordOfTheRingsRanking.ratingYear).isEqualTo(1966)
            assertThat(lordOfTheRingsRanking.ratingMonth).isEqualTo(12)
            assertThat(lordOfTheRingsRanking.bestSeller.id).isNotEqualTo(hpBookId)
        }
    }

    @Test
    fun `simplified foreign key in clause syntax`() {
        transactor.open { session ->
            val hpBookId = session.project(BookTable) { books ->
                addEq(books.name, "Harry Potter")
                project(books.id)
            }.uniqueResult()!!

            val lordOfTheRingsBookId = session.project(BookTable) { books ->
                addEq(books.name, "Lord of the Rings")
                project(books.id)
            }.uniqueResult()!!

            val singleInResult = session.query(BookRankingTable) { bookRankings ->
                addIn(bookRankings.bestSeller, setOf(lordOfTheRingsBookId))
            }.list()

            assertThat(singleInResult.map { Pair(it.ratingYear, it.bestSeller.name) })
                .containsExactly(Pair(1966, "Lord of the Rings"))

            val multipleInResult = session.query(BookRankingTable) { bookRankings ->
                addIn(bookRankings.bestSeller, setOf(hpBookId, lordOfTheRingsBookId))
            }.list()

            assertThat(multipleInResult.map { Pair(it.ratingYear, it.bestSeller.name) })
                .containsExactlyInAnyOrder(
                    Pair(2007, "Harry Potter"),
                    Pair(1966, "Lord of the Rings"),
                )
        }
    }

    @Test
    fun `simplified foreign key in clause and subquery syntax`() {
        val subQuery = Yawn.createProjectedDetachedCriteria(PersonTable) { people ->
            addIn(people.name, "J.K. Rowling", "Luan Nico")

            project(people.favoriteBook.foreignKey)
        }

        transactor.open { session ->
            val bookRankings = session.query(BookRankingTable) { bookRankings ->
                addIn(bookRankings.bestSeller, subQuery)
            }.list()

            assertThat(bookRankings.map { Pair(it.ratingYear, it.bestSeller.name) })
                .containsExactlyInAnyOrder(
                    Pair(2007, "Harry Potter"),
                    Pair(1966, "Lord of the Rings"),
                )
        }
    }

    @Test
    fun `query on custom named non-ID foreign key works and respects foreign key type`() {
        transactor.open { session ->
            val result = session.query(BookReviewTable) { bookReviews ->
                addEq(bookReviews.book.foreignKey, "PR6039 .O32 L67")
            }.uniqueResult()!!
            assertThat(result.reviewText).isEqualTo("Frodo was pretty cool.")
            assertThat(result.reviewer.name).isEqualTo("John Doe")
        }
    }

    @Test
    fun `queries work with partial or whole composite IDs`() {
        transactor.open { session ->
            val hpBookId = session.project(BookTable) { books ->
                addEq(books.name, "Harry Potter")
                project(books.id)
            }.uniqueResult()!!

            val luanId = session.project(PersonTable) { people ->
                addEq(people.name, "Luan Nico")
                project(people.id)
            }.uniqueResult()!!

            val simplePartialQueryResult = session.query(BookCoverTable) { bookCovers ->
                addEq(bookCovers.cid.bookId, hpBookId)
            }.uniqueResult()!!

            assertThat(simplePartialQueryResult.inscription).isEqualTo("Harry Potter and the Sorcerer's Stone")
            assertThat(simplePartialQueryResult.owner.name).isEqualTo("Luan Nico")

            val decomposedKeyQueryResult = session.query(BookCoverTable) { bookCovers ->
                addEq(bookCovers.cid.bookId, hpBookId)
                addEq(bookCovers.cid.ownerId, luanId)
            }.uniqueResult()!!

            assertThat(simplePartialQueryResult).isEqualTo(decomposedKeyQueryResult)

            val directCompositeIdQueryResult = session.query(BookCoverTable) { bookCovers ->
                addEq(bookCovers.cid, BookCoverCompositeId(hpBookId, luanId))
            }.uniqueResult()!!

            assertThat(directCompositeIdQueryResult).isEqualTo(directCompositeIdQueryResult)

            val allBookCoverIds = session.query(BookCoverTable)
                .list()
                .map { it.cid }

            val inQueryResult = session.query(BookCoverTable) { bookCovers ->
                addIn(bookCovers.cid, allBookCoverIds)
            }.list()

            assertThat(inQueryResult.map { it.inscription }).containsExactlyInAnyOrder(
                "The Fellowship of the Ring",
                "Harry Potter and the Sorcerer's Stone",
            )
        }
    }

    @Test
    fun `foreign key queries work with composite foreign keys`() {
        transactor.open { session ->
            val hpBookId = session.project(BookTable) { books ->
                addEq(books.name, "Harry Potter")
                project(books.id)
            }.uniqueResult()!!

            val luanId = session.project(PersonTable) { people ->
                addEq(people.name, "Luan Nico")
                project(people.id)
            }.uniqueResult()!!
            val fullQueryResult = session.query(BookCoverRankingTable) { coverRankings ->
                addEq(coverRankings.bookCover.foreignKey, BookCoverCompositeId(hpBookId, luanId))
            }.uniqueResult()!!
            assertThat(fullQueryResult.ranking).isEqualTo(2)
            assertThat(fullQueryResult.judgesComments).isEqualTo("Good construction and solid line work")

            val partialQueryResult = session.query(BookCoverRankingTable) { coverRankings ->
                addEq(coverRankings.bookCover.foreignKey.ownerId, luanId)
            }.uniqueResult()
            assertThat(partialQueryResult).isEqualTo(fullQueryResult)
        }
    }
}
