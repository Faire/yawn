package com.faire.yawn.database

import com.faire.yawn.query.YawnQueryOrder
import com.faire.yawn.query.YawnQueryOrder.Direction.ASC
import com.faire.yawn.setup.custom.EmailAddress
import com.faire.yawn.setup.entities.Book
import com.faire.yawn.setup.entities.Book.Genre.ADVENTURE
import com.faire.yawn.setup.entities.Book.Genre.FAIRY_TALE
import com.faire.yawn.setup.entities.Book.Genre.FANTASY
import com.faire.yawn.setup.entities.BookRankingTable
import com.faire.yawn.setup.entities.BookTable
import com.faire.yawn.setup.entities.BookViewTable
import com.faire.yawn.setup.entities.Person
import com.faire.yawn.setup.entities.PersonInterface
import com.faire.yawn.setup.entities.PersonTable
import com.faire.yawn.setup.entities.PublisherTable
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hibernate.NullPrecedence.FIRST
import org.hibernate.NullPrecedence.LAST
import org.junit.jupiter.api.Test
import java.sql.SQLException

internal class YawnSimpleQueriesTest : BaseYawnDatabaseTest() {
    @Test
    fun `exists()`() {
        /**
         * The exists() implementation will run a query selecting '1' and seeing if anything is returned.
         *
         * Example:
         *     SELECT
         *         '1' as _yawn_ct
         *     FROM
         *         books this_
         *     WHERE
         *         ...
         *     LIMIT 1
         */

        transactor.open { session ->
            val result1 = session.query(BookTable) { books ->
                addEq(books.name, "The Hobbit")
            }.exists()
            assertThat(result1).isTrue()

            val result2 = session.query(BookTable) { books ->
                addEq(books.name, "The Hobbit")

                val authors = join(books.author)
                addEq(authors.name, "J.K. Rowling")
            }.exists()
            assertThat(result2).isFalse()

            val result3 = session.query(BookTable) { books ->
                val publishers = join(books.publisher)
                addEq(publishers.name, "HarperCollins")

                addGt(books.sales.paperBacksSold, 1_000_000)
            }.exists()
            assertThat(result3).isTrue()

            val result4 = session.query(BookTable) { books ->
                val publishers = join(books.publisher)
                addEq(publishers.name, "HarperCollins")

                addGt(books.sales.paperBacksSold, 2_000_000)
            }.exists()
            assertThat(result4).isFalse()
        }
    }

    @Test
    fun `yawn fetch complete entity`() {
        transactor.open { session ->
            val book = session.query(BookTable) { books ->
                addEq(books.name, "The Hobbit")
            }.uniqueResult()!!

            with(book) {
                assertThat(name).isEqualTo("The Hobbit")
                assertThat(author.name).isEqualTo("J.R.R. Tolkien")
                assertThat(publisher!!.name).isEqualTo("Random House")
                assertThat(genres).containsExactlyInAnyOrder(FANTASY, ADVENTURE)
                assertThat(numberOfPages).isEqualTo(300)
                assertThat(notes).isEqualTo("Note for The Hobbit and Harry Potter")
            }

            with(book.bookMetadata!!) {
                assertThat(isbn).isEqualTo("978-0-261-10221-7")
                assertThat(publicationYear).isEqualTo(1937)
            }

            with(book.sales) {
                assertThat(paperBacksSold).isEqualTo(2_000_000)
                assertThat(hardBacksSold).isEqualTo(1_999_999)
                assertThat(countryWithMostCopiesSold).isEqualTo("UK")
            }
        }
    }

    @Test
    fun `yawn query by string equals`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                addEq(books.name, "The Hobbit")
            }.list()

            val theHobbit = results.single()
            assertThat(theHobbit.name).isEqualTo("The Hobbit")
            assertThat(theHobbit.author.name).isEqualTo("J.R.R. Tolkien")
        }
    }

    @Test
    fun `yawn query by id`() {
        transactor.open { session ->
            val theHobbitId = session.query(BookTable) { books ->
                addEq(books.name, "The Hobbit")
            }.uniqueResult()!!.id

            val theHobbit = session.query(BookTable) { books ->
                addEq(books.id, theHobbitId)
            }.uniqueResult()!!

            with(theHobbit) {
                assertThat(id).isEqualTo(theHobbitId)
                assertThat(author.name).isEqualTo("J.R.R. Tolkien")
            }
        }
    }

    @Test
    fun `yawn query using apply filter`() {
        transactor.open { session ->
            val results = session.query(BookTable)
                .applyFilter { addEq(it.name, "The Hobbit") }
                .list()

            val theHobbit = results.single()
            assertThat(theHobbit.name).isEqualTo("The Hobbit")
            assertThat(theHobbit.author.name).isEqualTo("J.R.R. Tolkien")
        }
    }

    @Test
    fun `yawn query by string multiple`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")
            }.list()

            assertThat(results).hasSize(2)
            assertThat(results.map { it.name }).containsExactlyInAnyOrder("The Hobbit", "Lord of the Rings")
        }
    }

    @Test
    fun `yawn query with max results`() {
        transactor.open { session ->
            val allResults = session.query(BookTable)
                .list()
            assertThat(allResults).hasSize(6)

            val threeResults = session.query(BookTable)
                .maxResults(3)
                .list()
            assertThat(threeResults).hasSize(3)

            val twoResults = session.query(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")
            }
                .maxResults(3)
                .list()
            assertThat(twoResults).hasSize(2)
        }
    }

    @Test
    fun `yawn all ways of ordering`() {
        transactor.open { session ->
            val orderedBooks = setOf(
                "Harry Potter",
                "Lord of the Rings",
                "The Emperor's New Clothes",
                "The Hobbit",
                "The Little Mermaid",
                "The Ugly Duckling",
            )

            fun assertOrderedBooks(books: List<Book>) {
                assertThat(books.map { it.name }).containsExactlyElementsOf(orderedBooks)
            }

            val insideLambda = session.query(BookTable) { books ->
                orderAsc(books.name)
            }.list()
            assertOrderedBooks(insideLambda)

            val orderAsc = session.query(BookTable)
                .orderAsc { name }
                .list()
            assertOrderedBooks(orderAsc)

            val applyOrder = session.query(BookTable)
                .applyOrder { YawnQueryOrder.asc(name) }
                .list()
            assertOrderedBooks(applyOrder)

            val applyOrders = session.query(BookTable)
                .applyOrders(
                    listOf { YawnQueryOrder.asc(name) },
                )
                .list()
            assertOrderedBooks(applyOrders)
        }
    }

    @Test
    fun `yawn query with offset`() {
        transactor.open { session ->
            val allBooks = session.query(BookTable) { orderAsc(it.name) }
                .maxResults(4)
                .list()
                .map { it.name }
            assertThat(allBooks).containsExactly(
                "Harry Potter",
                "Lord of the Rings",
                "The Emperor's New Clothes",
                "The Hobbit",
            )

            val offset0 = session.query(BookTable) { orderAsc(it.name) }
                .maxResults(2)
                .offset(0)
                .list()
                .map { it.name }
            assertThat(offset0).containsExactly(
                "Harry Potter",
                "Lord of the Rings",
            )

            val offset1 = session.query(BookTable) { orderAsc(it.name) }
                .maxResults(2)
                .offset(1)
                .list()
                .map { it.name }
            assertThat(offset1).containsExactly(
                "Lord of the Rings",
                "The Emperor's New Clothes",
            )

            val offset2 = session.query(BookTable) { orderAsc(it.name) }
                .maxResults(2)
                .offset(2)
                .list()
                .map { it.name }

            assertThat(offset2).containsExactly(
                "The Emperor's New Clothes",
                "The Hobbit",
            )
        }
    }

    @Test
    fun `custom type adapter - EmailAddress`() {
        transactor.open { session ->
            val tolkien = session.query(PersonTable) { people ->
                addEq(people.name, "J.R.R. Tolkien")
            }.uniqueResult()!!
            assertThat(tolkien.email).isEqualTo(EmailAddress("tolkien@faire.com"))

            val rowling = session.query(PersonTable) { people ->
                addEq(people.email, EmailAddress("rowling@faire.com"))
            }.uniqueResult()!!
            assertThat(rowling.name).isEqualTo("J.K. Rowling")
        }
    }

    @Test
    fun `query a type with @SerializeAsJson`() {
        transactor.open { session ->
            val dbBook = session.query(BookTable) { books ->
                addEq(books.name, "Lord of the Rings")
            }.uniqueResult()!!

            with(dbBook) {
                assertThat(name).isEqualTo("Lord of the Rings")
                assertThat(bookMetadata!!.publicationYear).isEqualTo(1954)
                assertThat(bookMetadata!!.isbn).isEqualTo("978-3-16-148410-0")
            }
        }
    }

    @Test
    fun `query against a hibernate supported object`() {
        transactor.open { session ->
            val dateTimeCreatedSecondBook = session.query(BookTable) { books ->
                addEq(books.name, "The Hobbit")
            }.uniqueResult()!!.createdAt
            val books = session.query(BookTable) { books ->
                addGt(books.createdAt, dateTimeCreatedSecondBook)
            }.list()

            assertThat(books)
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
    fun `supports joins`() {
        transactor.open { session ->
            val tolkienBooks = session.query(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")
            }.list()

            assertThat(tolkienBooks.map { it.name }).containsExactlyInAnyOrder("The Hobbit", "Lord of the Rings")

            val jHBooks = session.query(BookTable) { books ->
                addLike(books.name, "%H%")

                val authors = join(books.author)
                addLike(authors.name, "J.%")
            }.list()

            assertThat(jHBooks.map { it.name }).containsExactlyInAnyOrder("The Hobbit", "Harry Potter")
        }
    }

    @Test
    fun `supports nested joins`() {
        transactor.open { session ->
            val ranking = session.query(BookRankingTable) { ranking ->
                val bestSeller = join(ranking.bestSeller)
                val author = join(bestSeller.author)
                addEq(author.name, "J.K. Rowling")
            }.uniqueResult()!!

            assertThat(ranking.ratingYear).isEqualTo(2007)
            assertThat(ranking.ratingMonth).isEqualTo(1)
            assertThat(ranking.bestSeller.name).isEqualTo("Harry Potter")
            assertThat(ranking.bestSeller.author.name).isEqualTo("J.K. Rowling")
        }
    }

    @Test
    fun `order by`() {
        transactor.open { session ->
            val resultsAsc = session.query(BookTable) { books ->
                orderAsc(books.name)
            }.list()

            assertThat(resultsAsc.map { it.name }).containsExactly(
                "Harry Potter",
                "Lord of the Rings",
                "The Emperor's New Clothes",
                "The Hobbit",
                "The Little Mermaid",
                "The Ugly Duckling",
            )

            val resultsDesc = session.query(BookTable) { books ->
                orderDesc(books.name)
            }.list()

            assertThat(resultsDesc.map { it.name }).containsExactly(
                "The Ugly Duckling",
                "The Little Mermaid",
                "The Hobbit",
                "The Emperor's New Clothes",
                "Lord of the Rings",
                "Harry Potter",
            )

            val resultMultiple = session.query(BookTable) { books ->
                val authors = join(books.author)
                order(YawnQueryOrder.asc(authors.name), YawnQueryOrder.desc(books.name))
            }.list()

            assertThat(resultMultiple.map { "${it.author.name} - ${it.name}" }).containsExactly(
                "Hans Christian Andersen - The Ugly Duckling",
                "Hans Christian Andersen - The Little Mermaid",
                "Hans Christian Andersen - The Emperor's New Clothes",
                "J.K. Rowling - Harry Potter",
                "J.R.R. Tolkien - The Hobbit",
                "J.R.R. Tolkien - Lord of the Rings",
            )
        }
    }

    @Test
    fun `set finalizer`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                addNotEq(books.name, "Harry Potter")

                val authors = join(books.author)
                project(authors.name)
            }.set()

            assertThat(results).containsExactlyInAnyOrder(
                "Hans Christian Andersen",
                "J.R.R. Tolkien",
            )
        }
    }

    @Test
    fun `yawn query against embedded fields`() {
        transactor.open { session ->
            val byPaperbacksSold = session.query(BookTable) { books ->
                addGt(books.sales.paperBacksSold, 1_000_000)
            }.list()
            val byPaperbacksSoldNames = byPaperbacksSold.map { it.name }
            assertThat(byPaperbacksSoldNames).containsExactlyInAnyOrder("Lord of the Rings", "The Hobbit")

            val theHobbitSales = byPaperbacksSold.single { it.name == "The Hobbit" }.sales
            val theHobbit = session.query(BookTable) { books ->
                addEq(books.sales, theHobbitSales)
            }.uniqueResult()!!
            assertThat(theHobbit.name).isEqualTo("The Hobbit")
            val lotrSales = byPaperbacksSold.single { it.name == "Lord of the Rings" }.sales
            val inQuery = session.query(BookTable) { books ->
                addIn(books.sales, listOf(theHobbitSales, lotrSales))
            }.list().map { it.name }
            assertThat(inQuery).containsExactlyInAnyOrder("Lord of the Rings", "The Hobbit")

            val combinedEmbeddedAndTopLevelFieldQuery = session.query(BookTable) { books ->
                addEq(books.sales.countryWithMostCopiesSold, "UK")

                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")
            }.list().map { it.name }
            assertThat(combinedEmbeddedAndTopLevelFieldQuery).containsExactlyInAnyOrder(
                "Lord of the Rings",
                "The Hobbit",
            )
        }
    }

    @Test
    fun `yawn query against embedded fields through join`() {
        transactor.open { session ->
            val publishersWithMoreThan1MBooksSold = session.query(PublisherTable) { publishers ->
                val books = join(publishers.books)
                addGt(books.sales.paperBacksSold, 1_000_000)
            }
                .set()
                .map { it.name }

            assertThat(publishersWithMoreThan1MBooksSold).containsExactlyInAnyOrder("HarperCollins", "Random House")
        }
    }

    @Test
    fun `yawn in`() {
        transactor.open { session ->
            val allBookNames = session.project(BookTable) { project(it.name) }.set()
            assertThat(allBookNames).hasSize(6)

            val noBooks = session.project(BookTable) { books ->
                addIn(books.name, listOf())
                project(books.name)
            }.list()
            assertThat(noBooks).isEmpty()
            val oneBook = session.project(BookTable) { books ->
                addIn(books.name, listOf("The Hobbit"))
                project(books.name)
            }.list()
            assertThat(oneBook).containsOnly("The Hobbit")
            val twoBooks = session.project(BookTable) { books ->
                addIn(books.name, listOf("The Hobbit", "Harry Potter"))
                project(books.name)
            }.list()
            assertThat(twoBooks).containsExactlyInAnyOrder("The Hobbit", "Harry Potter")
            val allButTwoBooks = session.project(BookTable) { books ->
                addIn(books.name, allBookNames - setOf("The Hobbit", "Harry Potter"))
                project(books.name)
            }.list()
            assertThat(allButTwoBooks).containsExactlyInAnyOrderElementsOf(
                allBookNames - setOf(
                    "The Hobbit",
                    "Harry Potter",
                ),
            )
            val allButOneBook = session.project(BookTable) { books ->
                addIn(books.name, allBookNames - "The Hobbit")
                project(books.name)
            }.list()
            assertThat(allButOneBook).containsExactlyInAnyOrderElementsOf(allBookNames - "The Hobbit")
            val allBooks = session.project(BookTable) { books ->
                addIn(books.name, allBookNames)
                project(books.name)
            }.list()
            assertThat(allBooks).containsExactlyInAnyOrderElementsOf(allBookNames)
        }
    }

    @Test
    fun `yawn not in`() {
        transactor.open { session ->
            val allBookNames = session.project(BookTable) { project(it.name) }.set()
            assertThat(allBookNames).hasSize(6)

            val allBooks = session.project(BookTable) { books ->
                addNotIn(books.name, listOf())
                project(books.name)
            }.list()
            assertThat(allBooks).containsExactlyInAnyOrderElementsOf(allBookNames)
            val allButOneBook = session.project(BookTable) { books ->
                addNotIn(books.name, listOf("The Hobbit"))
                project(books.name)
            }.list()
            assertThat(allButOneBook).containsExactlyInAnyOrderElementsOf(allBookNames - "The Hobbit")
            val allButTwoBooks = session.project(BookTable) { books ->
                addNotIn(books.name, listOf("The Hobbit", "Harry Potter"))
                project(books.name)
            }.list()
            assertThat(allButTwoBooks).containsExactlyInAnyOrderElementsOf(
                allBookNames - setOf(
                    "The Hobbit",
                    "Harry Potter",
                ),
            )

            val twoBooks = session.project(BookTable) { books ->
                addNotIn(books.name, allBookNames - setOf("The Hobbit", "Harry Potter"))
                project(books.name)
            }.list()
            assertThat(twoBooks).containsExactlyInAnyOrder("The Hobbit", "Harry Potter")
            val oneBook = session.project(BookTable) { books ->
                addNotIn(books.name, allBookNames - "The Hobbit")
                project(books.name)
            }.list()
            assertThat(oneBook).containsOnly("The Hobbit")
            val noBooks = session.project(BookTable) { books ->
                addNotIn(books.name, allBookNames)
                project(books.name)
            }.list()
            assertThat(noBooks).isEmpty()
        }
    }

    @Test
    fun `view entity`() {
        transactor.open { session ->
            val bookView = session.query(BookViewTable) { books ->
                addEq(books.name, "The Hobbit")
            }.uniqueResult()

            with(bookView!!) {
                assertThat(id).isNotNull()
                assertThat(name).isEqualTo("The Hobbit")
            }
        }
    }

    @Test
    fun `@ElementCollection queries`() {
        transactor.open { session ->
            val fairyTaleBooks = session.query(BookTable) { books ->
                val genres = join(books.genres)
                addEq(genres.elements, FAIRY_TALE)
            }.list()
            assertThat(fairyTaleBooks.map { it.author.name }.toSet()).containsOnly("Hans Christian Andersen")

            val results = session.query(BookTable) { books ->
                addLe(books.numberOfPages, 300L)

                val genres = join(books.genres)
                addIn(genres.elements, FANTASY, FAIRY_TALE)
            }.list().map { it.name }
            assertThat(results).containsExactlyInAnyOrder(
                "The Hobbit",
                "The Little Mermaid",
                "The Ugly Duckling",
                "The Emperor's New Clothes",
            )
        }
    }

    @Test
    fun `@ElementCollection of ids`() {
        transactor.open { session ->
            val lotrId = session.query(BookTable) { books ->
                addEq(books.name, "Lord of the Rings")
            }.uniqueResult()!!.id
            val publishers = session.query(PublisherTable) { publishers ->
                val publishedBooks = join(publishers.publishedBookIds)
                addEq(publishedBooks.elements, lotrId)
            }.list()
            assertThat(publishers).hasSize(1)
            assertThat(publishers.single().name).isEqualTo("HarperCollins")
        }
    }

    @Test
    fun `@ElementCollection isEmpty`() {
        transactor.open { session ->
            val allBooks = session.query(BookTable) { books ->
                addIsNotEmpty(books.genres)
            }.list()
            assertThat(allBooks).hasSize(6)

            val noBooks = session.query(BookTable) { books ->
                addIsEmpty(books.genres)
            }.list()
            assertThat(noBooks).isEmpty()
        }
    }

    @Test
    fun `yawn equal or is null`() {
        transactor.open { session ->
            val result = session.query(BookTable) { books ->
                addEqOrIsNull(books.notes, "Note for LoTR")
            }.uniqueResult()!!

            assertThat(result.name).isEqualTo("Lord of the Rings")

            val results = session.query(BookTable) { books ->
                addEqOrIsNull(books.notes, null)
            }.list()

            assertThat(results.map { it.name })
                .containsExactlyInAnyOrder("The Little Mermaid", "The Ugly Duckling", "The Emperor's New Clothes")
        }
    }

    @Test
    fun `yawn formula queries`() {
        transactor.open { session ->
            val booksWithPublisher = session.query(BookTable) { books ->
                addEq(books.hasPublisher, true)
            }.list()
            assertThat(booksWithPublisher.map { it.name })
                .containsExactlyInAnyOrder(
                    "Lord of the Rings",
                    "The Hobbit",
                    "Harry Potter",
                    "The Emperor's New Clothes",
                )

            val booksWithoutPublisher = session.query(BookTable) { books ->
                addEq(books.hasPublisher, false)
            }.list()
            assertThat(booksWithoutPublisher.map { it.name })
                .containsExactlyInAnyOrder("The Little Mermaid", "The Ugly Duckling")

            val publishersSortedByNameLength = session.query(PublisherTable) { publishers ->
                orderAsc(publishers.nameLetterCount)
            }.list()
            assertThat(publishersSortedByNameLength.map { "${it.name} (${it.nameLetterCount})" })
                .containsExactly("Penguin (7)", "Co-Owned (8)", "Random House (12)", "HarperCollins (13)")
        }
    }

    @Test
    fun `yawn order with null precedence`() {
        transactor.open { session ->
            val booksNullNotesLast = session.query(BookTable) { books ->
                order(YawnQueryOrder(books.notes, ASC, nullPrecedence = LAST))
            }.list()
            assertThat(booksNullNotesLast.map { it.notes }).containsExactly(
                "Note for LoTR",
                "Note for The Hobbit and Harry Potter",
                "Note for The Hobbit and Harry Potter",
                null,
                null,
                null,
            )
            val booksNullNotesFirst = session.query(BookTable) { books ->
                order(YawnQueryOrder(books.notes, ASC, nullPrecedence = FIRST))
            }.list()
            assertThat(booksNullNotesFirst.map { it.notes }).containsExactly(
                null,
                null,
                null,
                "Note for LoTR",
                "Note for The Hobbit and Harry Potter",
                "Note for The Hobbit and Harry Potter",
            )
        }
    }

    // notes about nullability:
    // addEq(<anything>, null) - will never compile, because it is a footgun
    // addIsNull/addIsNotNull(<anything>) - will always compile, because we don't have
    //                                      advanced nullability checks on Yawn yet
    // please check our docs for more details:
    // https://www.notion.so/faire/Nullability-Yawn-1772efb5c25a809dbb26eea1ff3100cb

    @Test
    fun `nullable queries`() {
        transactor.open { session ->
            val query1 = session.query(BookTable) { books ->
                // name is NOT nullable but could fill the role of a nullable field;
                // for example:
                val authors = join(books.author)
                addEq(nullable(authors.name), books.notes)
            }
            assertThat(query1.list()).isEmpty()

            val theHobbit = session.query(BookTable) { books ->
                addEq(books.name, "The Hobbit")
            }.uniqueResult()!!
            theHobbit.notes = "The Hobbit"
            session.save(theHobbit)

            val query2 = session.query(BookTable) { books ->
                addEq(nullable(books.name), books.notes)
            }
            assertThat(query2.list().single().name).isEqualTo("The Hobbit")
        }
    }

    @Test
    fun `query hints`() {
        transactor.open { session ->
            val result = session.query(BookTable)
                .addQueryHint("idx_name")
                .applyFilter { books -> addEq(books.name, "The Hobbit") }
                .uniqueResult()!!
            assertThat(result.name).isEqualTo("The Hobbit")

            assertThatThrownBy {
                session.query(BookTable)
                    .addQueryHint("idx_INVALID")
                    .applyFilter { books -> addEq(books.name, "The Hobbit") }
                    .uniqueResult()!!
            }
                .rootCause()
                .isInstanceOf(SQLException::class.java)
                .hasMessageStartingWith("Index \"idx_INVALID\" not found;")
        }
    }

    @Test
    fun `querying and joining with @TargetEntity annotation with distinct types`() {
        transactor.open { session ->
            val result1 = session.query(PersonTable) { people ->
                val authors = join(people.favoriteAuthor)
                addEq(authors.name, "J.K. Rowling")
            }.uniqueResult()
            with(result1!!) {
                assertThat(name).isEqualTo("John Doe")
                assertThat(favoriteAuthor).isInstanceOf(PersonInterface::class.java)
                assertThat(favoriteAuthor).isInstanceOf(Person::class.java)
                assertThat(favoriteAuthor!!.name).isEqualTo("J.K. Rowling")
            }

            val result2 = session.query(PersonTable) { people ->
                val authors = join(people.favoriteAuthor)
                addEq(authors.name, "J.R.R. Tolkien")
            }.list()
            with(result2.single { it.name == "Luan Nico" }) {
                assertThat(favoriteAuthor).isInstanceOf(PersonInterface::class.java)
                assertThat(favoriteAuthor).isInstanceOf(Person::class.java)
                assertThat(favoriteAuthor!!.name).isEqualTo("J.R.R. Tolkien")
            }
            with(result2.single { it.name == "J.K. Rowling" }) {
                assertThat(favoriteAuthor).isInstanceOf(PersonInterface::class.java)
                assertThat(favoriteAuthor).isInstanceOf(Person::class.java)
                assertThat(favoriteAuthor!!.name).isEqualTo("J.R.R. Tolkien")
            }

            val result3 = session.query(PersonTable) { people ->
                val authors = join(people.favoriteAuthor)
                addEq(authors.name, "Luan Nico")
            }.uniqueResult()
            assertThat(result3).isNull()
        }
    }

    @Test
    fun `people whose favorite book was written by their favorite author`() {
        transactor.open { session ->
            val result = session.query(PersonTable) { people ->
                val favoriteBook = join(people.favoriteBook)
                addEq(people.favoriteAuthor, favoriteBook.author)
            }.list()
            assertThat(result.map { it.name }).containsExactlyInAnyOrder("J.K. Rowling", "J.R.R. Tolkien")
        }
    }
}
