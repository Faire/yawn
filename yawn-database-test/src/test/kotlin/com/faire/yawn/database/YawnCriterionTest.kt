package com.faire.yawn.database

import com.faire.yawn.query.YawnRestrictions.and
import com.faire.yawn.query.YawnRestrictions.between
import com.faire.yawn.query.YawnRestrictions.eq
import com.faire.yawn.query.YawnRestrictions.eqOrIsNull
import com.faire.yawn.query.YawnRestrictions.ge
import com.faire.yawn.query.YawnRestrictions.gt
import com.faire.yawn.query.YawnRestrictions.iLike
import com.faire.yawn.query.YawnRestrictions.`in`
import com.faire.yawn.query.YawnRestrictions.isEmpty
import com.faire.yawn.query.YawnRestrictions.isNotEmpty
import com.faire.yawn.query.YawnRestrictions.isNotNull
import com.faire.yawn.query.YawnRestrictions.isNull
import com.faire.yawn.query.YawnRestrictions.le
import com.faire.yawn.query.YawnRestrictions.like
import com.faire.yawn.query.YawnRestrictions.lt
import com.faire.yawn.query.YawnRestrictions.ne
import com.faire.yawn.query.YawnRestrictions.not
import com.faire.yawn.query.YawnRestrictions.notIn
import com.faire.yawn.query.YawnRestrictions.or
import com.faire.yawn.setup.entities.BookTable
import com.faire.yawn.setup.entities.PersonTable
import com.faire.yawn.setup.entities.PublisherTable
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.criterion.MatchMode
import org.junit.jupiter.api.Test

internal class YawnCriterionTest : BaseYawnDatabaseTest() {
    @Test
    fun `eq column against String`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(
                    eq(books.name, "The Hobbit"),
                )
            }.list()

            val theHobbit = results.single()
            assertThat(theHobbit.name).isEqualTo("The Hobbit")
            assertThat(theHobbit.author.name).isEqualTo("J.R.R. Tolkien")
        }
    }

    @Test
    fun `eq column against column`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(
                    eq(books.sales.eBooksSold, books.numberOfPages),
                )
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder("The Little Mermaid")
        }
    }

    @Test
    fun `gt column against long`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(
                    gt(books.numberOfPages, 300),
                )
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
    fun `gt column against hibernate supported object`() {
        transactor.open { session ->
            val dateTimeCreatedSecondBook = session.query(BookTable) { books ->
                add(eq(books.name, "The Hobbit"))
            }.uniqueResult()!!.createdAt
            val dbBook = session.query(BookTable) { books ->
                add(gt(books.createdAt, dateTimeCreatedSecondBook))
            }.list()

            assertThat(dbBook)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "Harry Potter",
                    "The Little Mermaid",
                    "The Ugly Duckling",
                    "The Emperor's New Clothes",
                )
        }
    }

    @Test
    fun `joins with book and author`() {
        transactor.open { session ->
            // Find all books by J.R.R. Tolkien
            val results = session.query(BookTable) { books ->
                val author = join(books.author)
                add(eq(author.name, "J.R.R. Tolkien"))
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder("Lord of the Rings", "The Hobbit")
        }

        // Find all people who have a favorite book by J.R.R. Tolkien
        transactor.open { session ->
            val results = session.query(PersonTable) { people ->
                val favoriteBook = join(people.favoriteBook)
                val favoriteBookAuthor = join(favoriteBook.author)
                add(eq(favoriteBookAuthor.name, "J.R.R. Tolkien"))
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder("J.K. Rowling", "Paul Duchesne")
        }
    }

    @Test
    fun `or with and combinations`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                val authors = join(books.author)
                add(
                    or(
                        and(
                            eq(authors.name, "Hans Christian Andersen"),
                            gt(books.numberOfPages, 100),
                        ),
                        and(
                            ge(books.numberOfPages, 300),
                        ),
                    ),
                )
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "The Hobbit",
                    "Lord of the Rings",
                    "Harry Potter",
                    "The Ugly Duckling",
                    "The Emperor's New Clothes",
                )
        }
    }

    @Test
    fun `all and and or overloads - two operands versions`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                val authors = join(books.author)
                val publishers = join(books.publisher)
                addAnd(
                    or(
                        eq(authors.name, "J.R.R. Tolkien"),
                        eq(authors.name, "J.K. Rowling"),
                    ),
                    and(
                        eq(books.name, "Harry Potter"),
                        eq(publishers.name, "Penguin"),
                    ),
                )
                addOr(
                    and(
                        eq(books.name, "Lord of the Rings"),
                        eq(authors.name, "J.R.R. Tolkien"),
                    ),
                    and(
                        eq(books.name, "Harry Potter"),
                        eq(authors.name, "J.K. Rowling"),
                    ),
                )
            }.list()

            assertThat(results.single().name).isEqualTo("Harry Potter")
        }
    }

    @Test
    fun `all and and or overloads - list versions`() {
        val allowedAuthors = listOf(
            "J.R.R. Tolkien",
            "J.K. Rowling",
        )
        val allowedBookNames = listOf(
            "Harry Potter",
            "The Ugly Duckling",
            "The Emperor's New Clothes",
        )

        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                val authors = join(books.author)
                val publishers = join(books.publisher)
                addAnd(
                    listOf(
                        or(allowedAuthors.map { eq(authors.name, it) }),
                        or(allowedBookNames.map { eq(books.name, it) }),
                    ),
                )
                addOr(
                    listOf(
                        and(allowedAuthors.map { eq(authors.name, it) }),
                        and(allowedBookNames.map { eq(books.name, it) }),
                        isNotEmpty(publishers.owners),
                    ),
                )
            }.list()

            assertThat(results.single().name).isEqualTo("Harry Potter")
        }
    }

    @Test
    fun `all and and or overloads - varargs versions`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                val authors = join(books.author)
                val publishers = join(books.publisher)
                val owners = join(publishers.owners)
                addAnd(
                    or(
                        eq(authors.name, "J.R.R. Tolkien"),
                        eq(authors.name, "J.K. Rowling"),
                        eq(authors.name, "Hans Christian Andersen"),
                    ),
                    or(
                        eq(books.name, "Harry Potter"),
                        eq(books.name, "The Little Mermaid"),
                        eq(books.name, "Lord of the Rings"),
                    ),
                    and(
                        eq(publishers.name, "HarperCollins"),
                        eq(publishers.nameLetterCount, 13),
                        eq(owners.name, "Jane Doe"),
                    ),
                )
                addOr(
                    and(
                        eq(books.name, "The Ugly Duckling"),
                        eq(authors.name, "J.R.R. Tolkien"),
                        eq(publishers.name, "Penguin"),
                    ),
                    and(
                        eq(books.name, "Harry Potter"),
                        eq(authors.name, "Hans Christian Andersen"),
                        eq(publishers.name, "Penguin"),
                    ),
                    and(
                        eq(books.name, "Lord of the Rings"),
                        eq(authors.name, "J.R.R. Tolkien"),
                        eq(publishers.name, "HarperCollins"),
                    ),
                )
            }.list()

            assertThat(results.single().name).isEqualTo("Lord of the Rings")
        }
    }

    @Test
    fun `ne column against String`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                val authors = join(books.author)
                add(
                    ne(authors.name, "J.R.R. Tolkien"),
                )
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "Harry Potter",
                    "The Little Mermaid",
                    "The Ugly Duckling",
                    "The Emperor's New Clothes",
                )
        }
    }

    @Test
    fun `ne column against column`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(
                    ne(books.sales.eBooksSold, books.numberOfPages),
                )
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "Lord of the Rings",
                    "The Hobbit",
                    "Harry Potter",
                    "The Ugly Duckling",
                    "The Emperor's New Clothes",
                )
        }
    }

    @Test
    fun `lt column against long`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(
                    lt(books.numberOfPages, 110),
                )
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "The Little Mermaid",
                )
        }
    }

    @Test
    fun `lt column against column`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(
                    lt(books.numberOfPages, books.sales.eBooksSold),
                )
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "Lord of the Rings",
                    "The Hobbit",
                    "Harry Potter",
                    "The Ugly Duckling",
                    "The Emperor's New Clothes",
                )
        }
    }

    @Test
    fun `le column against long`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(
                    le(books.numberOfPages, 110),
                )
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
    fun `le column against column`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(
                    le(books.sales.eBooksSold, books.numberOfPages),
                )
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "The Little Mermaid",
                )
        }
    }

    @Test
    fun `between column with low and high values`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(
                    between(books.numberOfPages, 100, 300),
                )
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "The Hobbit",
                    "The Little Mermaid",
                    "The Ugly Duckling",
                    "The Emperor's New Clothes",
                )
        }
    }

    @Test
    fun `not with criterion`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                val authors = join(books.author)
                add(
                    not(eq(authors.name, "J.R.R. Tolkien")),
                )
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "Harry Potter",
                    "The Little Mermaid",
                    "The Ugly Duckling",
                    "The Emperor's New Clothes",
                )
        }
    }

    @Test
    fun `like with String and match mode`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(
                    like(books.name, "The", MatchMode.START),
                )
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "The Hobbit",
                    "The Little Mermaid",
                    "The Ugly Duckling",
                    "The Emperor's New Clothes",
                )
        }
    }

    @Test
    fun `iLike with String and match mode`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(
                    iLike(books.name, "the", MatchMode.START),
                )
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "The Hobbit",
                    "The Little Mermaid",
                    "The Ugly Duckling",
                    "The Emperor's New Clothes",
                )
        }
    }

    @Test
    fun `isNotNull with column`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(
                    isNotNull(books.notes),
                )
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "Lord of the Rings",
                    "The Hobbit",
                    "Harry Potter",
                )
        }
    }

    @Test
    fun `isNull with column`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(
                    isNull(books.notes),
                )
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "The Little Mermaid",
                    "The Ugly Duckling",
                    "The Emperor's New Clothes",
                )
        }
    }

    @Test
    fun `eqOrIsNull with column and value`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(
                    eqOrIsNull(books.notes, "Note for Lord of the Rings"),
                )
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder("Lord of the Rings")
        }
    }

    @Test
    fun `in with column and collection`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(
                    `in`(books.name, listOf("The Hobbit", "Harry Potter")),
                )
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "The Hobbit",
                    "Harry Potter",
                )
        }
    }

    @Test
    fun `notIn with column and collection`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                add(
                    notIn(books.name, listOf("The Hobbit", "Harry Potter")),
                )
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "Lord of the Rings",
                    "The Little Mermaid",
                    "The Ugly Duckling",
                    "The Emperor's New Clothes",
                )
        }
    }

    @Test
    fun `isEmpty with collection column`() {
        transactor.open { session ->
            val results = session.query(PublisherTable) { publishers ->
                add(
                    isEmpty(publishers.books),
                )
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "Co-Owned",
                )
        }
    }

    @Test
    fun `isNotEmpty with collection column`() {
        transactor.open { session ->
            val results = session.query(PublisherTable) { publishers ->
                add(
                    isNotEmpty(publishers.books),
                )
            }.list()

            assertThat(results)
                .extracting("name")
                .containsExactlyInAnyOrder(
                    "Penguin",
                    "HarperCollins",
                    "Random House",
                )
        }
    }
}
