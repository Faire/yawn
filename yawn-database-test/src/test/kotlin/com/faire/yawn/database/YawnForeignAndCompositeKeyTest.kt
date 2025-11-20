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
            val bookId = session.project(BookTable) { book->
                addEq(book.name, "Harry Potter")
                project(book.id)
            }.uniqueResult()!!

            val bookRanking = session.query(BookRankingTable) { bookRanking ->
                addEq(bookRanking.bestSeller.foreignKey, bookId)
            }.uniqueResult()!!

            assertThat(bookRanking.ratingYear).isEqualTo(2007)
            assertThat(bookRanking.ratingMonth).isEqualTo(1)
            assertThat(bookRanking.bestSeller.id).isEqualTo(bookId)
        }
    }

    @Test
    fun `simplified foreign key equals syntax`() {
        transactor.open { session ->
            val hpBookId = session.project(BookTable) { book->
                addEq(book.name, "Harry Potter")
                project(book.id)
            }.uniqueResult()!!

            val hpRanking = session.query(BookRankingTable) { bookRanking ->
                addEq(bookRanking.bestSeller, hpBookId)
            }.uniqueResult()!!

            assertThat(hpRanking.ratingYear).isEqualTo(2007)
            assertThat(hpRanking.ratingMonth).isEqualTo(1)
            assertThat(hpRanking.bestSeller.id).isEqualTo(hpBookId)

            val lotrRanking = session.query(BookRankingTable) { bookRanking ->
                addNotEq(bookRanking.bestSeller, hpBookId)
            }.uniqueResult()!!

            assertThat(lotrRanking.ratingYear).isEqualTo(1966)
            assertThat(lotrRanking.ratingMonth).isEqualTo(12)
            assertThat(lotrRanking.bestSeller.id).isNotEqualTo(hpBookId)
        }
    }

    @Test
    fun `simplified foreign key in clause syntax`() {
        transactor.open { session ->
            val hpBookId = session.project(BookTable) { book->
                addEq(book.name, "Harry Potter")
                project(book.id)
            }.uniqueResult()!!

            val lotrBookId = session.project(BookTable) { book->
                addEq(book.name, "Lord of the Rings")
                project(book.id)
            }.uniqueResult()!!

            val singleInResult = session.query(BookRankingTable) { bookRanking ->
                addIn(bookRanking.bestSeller, setOf(lotrBookId))
            }.list()

            assertThat(singleInResult.map { Pair(it.ratingYear, it.bestSeller.name) })
                .containsExactly(Pair(1966, "Lord of the Rings"))

            val multipleInResult = session.query(BookRankingTable) { bookRanking ->
                addIn(bookRanking.bestSeller, setOf(hpBookId, lotrBookId))
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
        val subQuery = Yawn.createProjectedDetachedCriteria(PersonTable) { person ->
            addIn(person.name, "J.K. Rowling", "Luan Nico")

            project(person.favoriteBook.foreignKey)
        }

        transactor.open { session ->
            val bookRankings = session.query(BookRankingTable) { bookRanking ->
                addIn(bookRanking.bestSeller, subQuery)
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
            val result = session.query(BookReviewTable) { bookReview ->
                addEq(bookReview.book.foreignKey, "PR6039 .O32 L67")
            }.uniqueResult()!!
            assertThat(result.reviewText).isEqualTo("Frodo was pretty cool.")
            assertThat(result.reviewer.name).isEqualTo("John Doe")
        }
    }

    @Test
    fun `queries work with partial or whole composite IDs`() {
        transactor.open { session ->
            val hpBookId = session.project(BookTable) { book->
                addEq(book.name, "Harry Potter")
                project(book.id)
            }.uniqueResult()!!

            val luanId = session.project(PersonTable) { person ->
                addEq(person.name, "Luan Nico")
                project(person.id)
            }.uniqueResult()!!

            val simplePartialQueryResult = session.query(BookCoverTable) { bookCover ->
                addEq(bookCover.cid.bookId, hpBookId)
            }.uniqueResult()!!

            assertThat(simplePartialQueryResult.inscription).isEqualTo("Harry Potter and the Sorcerer's Stone")
            assertThat(simplePartialQueryResult.owner.name).isEqualTo("Luan Nico")

            val decomposedKeyQueryResult = session.query(BookCoverTable) { bookCover ->
                addEq(bookCover.cid.bookId, hpBookId)
                addEq(bookCover.cid.ownerId, luanId)
            }.uniqueResult()!!

            assertThat(simplePartialQueryResult).isEqualTo(decomposedKeyQueryResult)

            val directCompositeIdQueryResult = session.query(BookCoverTable) { bookCover ->
                addEq(bookCover.cid, BookCoverCompositeId(hpBookId, luanId))
            }.uniqueResult()!!

            assertThat(directCompositeIdQueryResult).isEqualTo(directCompositeIdQueryResult)

            val allBookCoverIds = session.query(BookCoverTable)
                .list()
                .map { it.cid }

            val inQueryResult = session.query(BookCoverTable) { bookCover ->
                addIn(bookCover.cid, allBookCoverIds)
            }.list()

            assertThat(inQueryResult.map { it.inscription }).containsExactlyInAnyOrder(
                "LOTR",
                "Harry Potter and the Sorcerer's Stone"
            )
        }
    }

    @Test
    fun `foreign key queries work with composite foreign keys`() {
        transactor.open { session ->
            val hpBookId = session.project(BookTable) { book->
                addEq(book.name, "Harry Potter")
                project(book.id)
            }.uniqueResult()!!

            val luanId = session.project(PersonTable) { person ->
                addEq(person.name, "Luan Nico")
                project(person.id)
            }.uniqueResult()!!
            val fullQueryResult = session.query(BookCoverRankingTable) { coverRanking ->
                addEq(coverRanking.bookCover.foreignKey, BookCoverCompositeId(hpBookId, luanId))
            }.uniqueResult()!!
            assertThat(fullQueryResult.ranking).isEqualTo(2)
            assertThat(fullQueryResult.judgesComments).isEqualTo("Good construction and solid line work")

            val partialQueryResult = session.query(BookCoverRankingTable) { coverRanking ->
                addEq(coverRanking.bookCover.foreignKey.ownerId, luanId)
            }.uniqueResult()
            assertThat(partialQueryResult).isEqualTo(fullQueryResult)
        }
    }
}
