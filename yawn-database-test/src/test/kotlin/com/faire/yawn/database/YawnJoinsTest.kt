package com.faire.yawn.database

import com.faire.yawn.Yawn
import com.faire.yawn.project.YawnProjection
import com.faire.yawn.project.YawnProjections
import com.faire.yawn.setup.entities.Book.Language.ENGLISH
import com.faire.yawn.setup.entities.BookRankingTable
import com.faire.yawn.setup.entities.BookReviewTable
import com.faire.yawn.setup.entities.BookTable
import com.faire.yawn.setup.entities.PersonTable
import com.faire.yawn.setup.entities.PublisherTable
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.sql.JoinType
import org.junit.jupiter.api.Test

internal class YawnJoinsTest : BaseYawnDatabaseTest() {
    @Test
    fun `yawn one-to-one inner join`() {
        transactor.open { session ->
            val results1 = session.query(BookRankingTable) { ranking ->
                val books = join(ranking.bestSeller)
                addEq(books.name, "The Hobbit")
            }.list()
            assertThat(results1).isEmpty()

            val results2 = session.query(BookRankingTable) { ranking ->
                val books = join(ranking.bestSeller)
                addEq(books.name, "Harry Potter")
            }.list()
            assertThat(results2.single().bestSeller.name).isEqualTo("Harry Potter")
        }
    }

    @Test
    fun `join with criterion`() {
        transactor.open { session ->
            val results1 = session.project(PersonTable) { people ->
                val books = join(people.favoriteBook, joinType = JoinType.LEFT_OUTER_JOIN)
                addGe(books.numberOfPages, 500)

                project(YawnProjections.pair(people.name, books.name))
            }.list()
            assertThat(results1).containsExactlyInAnyOrder(
                "J.K. Rowling" to "Lord of the Rings",
                "Paul Duchesne" to "Lord of the Rings",
                "Luan Nico" to "Harry Potter",
            )

            val results2 = session.project(PersonTable) { people ->
                val books = join(people.favoriteBook, joinType = JoinType.LEFT_OUTER_JOIN) { books ->
                    addGe(books.numberOfPages, 500)
                }

                project(YawnProjections.pair(people.name, nullable(books.name)))
            }.list()
            assertThat(results2).containsExactlyInAnyOrder(
                "J.K. Rowling" to "Lord of the Rings",
                "Paul Duchesne" to "Lord of the Rings",
                "Luan Nico" to "Harry Potter",
                "Jane Doe" to null,
                "John Doe" to null,
                "Hans Christian Andersen" to null,
                "J.R.R. Tolkien" to null,
            )
        }
    }

    @Test
    fun `yawn many-to-one inner join`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")
            }.list()
            assertThat(results.map { it.name }).containsExactlyInAnyOrder("The Hobbit", "Lord of the Rings")
        }
    }

    @Test
    fun `yawn one-to-many inner join`() {
        transactor.open { session ->
            val results1 = session.query(PublisherTable) { publishers ->
                val books = join(publishers.books)
                val authors = join(books.author)
                addIn(authors.name, setOf("J.R.R. Tolkien", "J.K. Rowling"))
            }.set()
            assertThat(results1.map { it.name }).containsExactlyInAnyOrder("Penguin", "HarperCollins", "Random House")

            val results2 = session.query(PublisherTable) { publishers ->
                val books = join(publishers.books)
                val authors = join(books.author)
                addEq(authors.name, "Hans Christian Andersen")
            }.set()
            assertThat(results2.map { it.name }).containsExactlyInAnyOrder("Penguin")
        }
    }

    @Test
    fun `yawn many-to-many inner join`() {
        transactor.open { session ->
            val results1 = session.query(PublisherTable) { publishers ->
                val owners = join(publishers.owners)
                addEq(owners.name, "John Doe")
            }.list()
            assertThat(results1.map { it.name }).containsExactlyInAnyOrder("Penguin", "Random House", "Co-Owned")

            val results2 = session.query(PublisherTable) { publishers ->
                val owners = join(publishers.owners)
                addEq(owners.name, "Jane Doe")
            }.list()
            assertThat(results2.map { it.name }).containsExactlyInAnyOrder("HarperCollins", "Co-Owned")

            val results3 = session.query(PersonTable) { people ->
                val publishers = join(people.ownedPublishers)
                addIn(publishers.name, setOf("Penguin", "HarperCollins"))
            }.list()
            assertThat(results3.map { it.name }).containsExactlyInAnyOrder("John Doe", "Jane Doe")
        }
    }

    @Test
    fun `yawn left join - books with publishers`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                val publishers = join(books.publisher, joinType = JoinType.LEFT_OUTER_JOIN)
                addIsNotNull(publishers.id)
            }.list()
            assertThat(results.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "Lord of the Rings",
                "Harry Potter",
                "The Emperor's New Clothes",
            )
        }
    }

    @Test
    fun `yawn left join - books without publishers`() {
        transactor.open { session ->
            val results2 = session.query(BookTable) { books ->
                val publishers = join(books.publisher, joinType = JoinType.LEFT_OUTER_JOIN)
                addIsNull(publishers.id)
            }.list()
            assertThat(results2.map { it.name }).containsExactlyInAnyOrder("The Little Mermaid", "The Ugly Duckling")
        }
    }

    @Test
    fun `yawn right join - books without publisher`() {
        transactor.open { session ->
            val results = session.project(PublisherTable) { publishers ->
                val books = join(publishers.books, joinType = JoinType.RIGHT_OUTER_JOIN)
                addIsNull(publishers.id)
                project(books.name)
            }.list()
            assertThat(results).containsExactlyInAnyOrder("The Little Mermaid", "The Ugly Duckling")
        }
    }

    @Test
    fun `yawn right join - publisher without books`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                val publishers = join(books.publisher, joinType = JoinType.RIGHT_OUTER_JOIN)
                addIsNull(books.id)

                project(publishers.name)
            }.list()
            assertThat(results).containsExactlyInAnyOrder("Co-Owned")
        }
    }

    @Test
    fun `yawn foreign key after join`() {
        transactor.open { session ->
            val tolkien = session.query(PersonTable) { people ->
                addEq(people.name, "J.R.R. Tolkien")
            }.uniqueResult()!!
            val results = session.project(PublisherTable) { publishers ->
                val books = join(publishers.books)
                addEq(books.author, tolkien.id)

                project(publishers.name)
            }.set()
            assertThat(results).containsExactlyInAnyOrder("HarperCollins", "Random House")
        }
    }

    @Test
    fun `yawn is empty - publishers without books`() {
        transactor.open { session ->
            val results = session.query(PublisherTable) { publishers ->
                addIsEmpty(publishers.books)
            }.list().map { it.name }
            assertThat(results).containsExactlyInAnyOrder("Co-Owned")
        }
    }

    @Test
    fun `yawn not is empty - publishers with books`() {
        transactor.open { session ->
            val results = session.query(PublisherTable) { publishers ->
                addIsNotEmpty(publishers.books)
            }.list().map { it.name }
            assertThat(results).containsExactlyInAnyOrder("Penguin", "HarperCollins", "Random House")
        }
    }

    // NOTE: mysql does not support full outer joins

    @Test
    fun `yawn deeply-nested join structure - anyone that published Tolkien`() {
        transactor.open { session ->
            val results = session.query(PersonTable) { people ->
                val publishers = join(people.ownedPublishers)
                val books = join(publishers.books)
                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")
            }.set()
            assertThat(results.map { it.name }).containsExactlyInAnyOrder("Jane Doe", "John Doe")
        }
    }

    @YawnProjection
    data class PublishersWithPagesPublished(val name: String, val pagesPublished: Long)

    @Test
    fun `yawn deeply-nested join structure - publishers with pages published`() {
        transactor.open { session ->
            val results = session.project(PublisherTable) { publishers ->
                val books = join(publishers.books)
                project(
                    YawnJoinsTest_PublishersWithPagesPublishedProjection.create(
                        name = YawnProjections.groupBy(publishers.name),
                        pagesPublished = YawnProjections.sum(books.numberOfPages),
                    ),
                )
            }.list()

            assertThat(results).containsExactlyInAnyOrder(
                PublishersWithPagesPublished("HarperCollins", 1_000),
                PublishersWithPagesPublished("Penguin", 620),
                PublishersWithPagesPublished("Random House", 300),
            )
        }
    }

    @YawnProjection
    data class PublishersWithBooksPublished(val name: String, val booksPublished: Long)

    @Test
    fun `yawn deeply-nested join structure - publishers with books published`() {
        transactor.open { session ->
            val results = session.project(PublisherTable) { publishers ->
                val books = join(publishers.books)
                project(
                    YawnJoinsTest_PublishersWithBooksPublishedProjection.create(
                        name = YawnProjections.groupBy(publishers.name),
                        booksPublished = YawnProjections.countDistinct(books.id),
                    ),
                )
            }.list()

            assertThat(results).containsExactlyInAnyOrder(
                PublishersWithBooksPublished("HarperCollins", 1),
                PublishersWithBooksPublished("Penguin", 2),
                PublishersWithBooksPublished("Random House", 1),
            )
        }
    }

    @Test
    fun `yawn deeply-nested join structure - publishers with books published with left join`() {
        transactor.open { session ->
            val results = session.project(PublisherTable) { publishers ->
                val books = join(publishers.books, joinType = JoinType.LEFT_OUTER_JOIN)
                project(
                    YawnJoinsTest_PublishersWithBooksPublishedProjection.create(
                        name = YawnProjections.groupBy(publishers.name),
                        booksPublished = YawnProjections.countDistinct(books.id),
                    ),
                )
            }.list()

            assertThat(results).containsExactlyInAnyOrder(
                PublishersWithBooksPublished("HarperCollins", 1),
                PublishersWithBooksPublished("Penguin", 2),
                PublishersWithBooksPublished("Random House", 1),
                PublishersWithBooksPublished("Co-Owned", 0),
            )
        }
    }

    @Test
    fun `yawn deeply-nested join structure - people who's favorite books are not their own writing`() {
        transactor.open { session ->
            val results = session.query(PersonTable) { people ->
                val favoriteBooks = join(people.favoriteBook)
                val authors = join(favoriteBooks.author)
                addNotEq(people.name, authors.name)
            }.list()
            assertThat(results.map { it.name }).containsExactlyInAnyOrder(
                "J.R.R. Tolkien",
                "J.K. Rowling",
                "Luan Nico",
                "Paul Duchesne",
                "Jane Doe",
            )
        }
    }

    @Test
    fun `yawn deeply-nested join structure - authors who's favorite books are not their own writing'`() {
        val authors = Yawn.createProjectedDetachedCriteria(BookTable) { books ->
            val author = join(books.author)
            project(author.name)
        }

        val results = transactor.open { session ->
            session.query(PersonTable) { people ->
                val favoriteBooks = join(people.favoriteBook)
                val favoriteBooksAuthors = join(favoriteBooks.author)
                addIn(people.name, authors)
                addNotEq(people.name, favoriteBooksAuthors.name)
            }.list()
        }
        assertThat(results.map { it.name }).containsExactlyInAnyOrder(
            "J.R.R. Tolkien",
            "J.K. Rowling",
        )
    }

    @Test
    fun `joins to the same table - book's authors favorite books`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                val authors = join(books.author)

                val favoriteBooks = join(authors.favoriteBook)
                addIsNotNull(favoriteBooks.id)

                project(
                    YawnProjections.pair(
                        books.name,
                        favoriteBooks.name,
                    ),
                )
            }.list()
            assertThat(results).containsExactlyInAnyOrder(
                "Harry Potter" to "Lord of the Rings",
                "Lord of the Rings" to "The Little Mermaid",
                "The Hobbit" to "The Little Mermaid",
            )
        }
    }

    @Test
    fun `multiple joins to the same table - book's authors favorite authors`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                val authors = join(books.author)

                val favoriteBooks = join(authors.favoriteBook)
                addIsNotNull(favoriteBooks.id)

                val favoriteAuthors = join(favoriteBooks.author)
                addIsNotNull(favoriteAuthors.id)

                project(
                    YawnProjections.pair(
                        books.name,
                        favoriteAuthors.name,
                    ),
                )
            }.list()

            assertThat(results).containsExactlyInAnyOrder(
                "Harry Potter" to "J.R.R. Tolkien",
                "Lord of the Rings" to "Hans Christian Andersen",
                "The Hobbit" to "Hans Christian Andersen",
            )
        }
    }

    @Test
    fun `simplest join reference`() {
        transactor.open { session ->
            val criteria = session.query(BookTable)
            val authorsRef = criteria.joinRef { author }

            criteria.applyFilter { books ->
                val authors = authorsRef.get(books)
                addLike(authors.name, "J.%")
            }

            criteria.applyFilter { books ->
                val authors = authorsRef.get(books)
                addLike(authors.name, "%n")
            }

            val results = criteria.list()
            assertThat(results.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "Lord of the Rings",
            )
        }
    }

    @Test
    fun `multiple joins to the same table using join references - book's authors favorite authors`() {
        transactor.open { session ->
            val criteria = session.query(BookTable)

            // join refs
            val authorsRef = criteria.joinRef { author }
            val favoriteBooksRef = criteria.joinRef {
                val authors = authorsRef.get(this)
                authors.favoriteBook
            }
            val favoriteAuthorsRef = criteria.joinRef {
                val favoriteBooks = favoriteBooksRef.get(this)
                favoriteBooks.author
            }

            // these can be done
            criteria.applyFilter { books ->
                val favoriteBooks = favoriteBooksRef.get(books)
                addIsNotNull(favoriteBooks.id)
            }
            // in multiple places
            criteria.applyFilter { books ->
                val favoriteAuthors = favoriteAuthorsRef.get(books)
                addIsNotNull(favoriteAuthors.id)
            }
            // over multiple functions
            criteria.applyFilter { books ->
                val authors = authorsRef.get(books)
                addEq(authors.name, "J.R.R. Tolkien")
            }

            val results = criteria.applyProjection { books ->
                val favoriteAuthors = favoriteAuthorsRef.get(books)
                project(
                    YawnProjections.pair(
                        books.name,
                        favoriteAuthors.name,
                    ),
                )
            }
                .list()

            assertThat(results).containsExactlyInAnyOrder(
                "Lord of the Rings" to "Hans Christian Andersen",
                "The Hobbit" to "Hans Christian Andersen",
            )
        }
    }

    @Test
    fun `applyJoinRef equivalence test`() {
        transactor.open { session ->
            val criteria1 = session.query(BookTable)
            val authorsRef1 = criteria1.joinRef { author }

            criteria1.applyJoinRef(authorsRef1) { authors ->
                addLike(authors.name, "J.%")
            }
            val results1 = criteria1.list()

            val criteria2 = session.query(BookTable)
            val authorsRef2 = criteria2.joinRef { author }

            criteria2.applyFilter { books ->
                val authors = authorsRef2.get(books)
                addLike(authors.name, "J.%")
            }
            val results2 = criteria2.list()

            assertThat(results1.map { it.name }).isEqualTo(results2.map { it.name })
            assertThat(results1.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "Lord of the Rings",
                "Harry Potter",
            )
        }
    }

    @Test
    fun `applyJoinRefs with multiple references test - equivalent to multiple applyFilter calls`() {
        transactor.open { session ->
            // Test applyJoinRefs approach
            val criteria1 = session.query(BookTable)
            val authorsRef1 = criteria1.joinRef { author }
            val publishersRef1 = criteria1.joinRef { publisher }

            criteria1.applyJoinRefs(authorsRef1, publishersRef1) { authors, publisher ->
                addLike(authors.name, "J.%")
                addEq(publisher.name, "HarperCollins")
            }
            val results1 = criteria1.list()

            // Test equivalent multiple applyFilter approach
            val criteria2 = session.query(BookTable)
            val authorsRef2 = criteria2.joinRef { author }
            val publishersRef2 = criteria2.joinRef { publisher }

            criteria2.applyFilter { books ->
                val authors = authorsRef2.get(books)
                addLike(authors.name, "J.%")
            }

            criteria2.applyFilter { books ->
                val publisher = publishersRef2.get(books)
                addEq(publisher.name, "HarperCollins")
            }
            val results2 = criteria2.list()

            // Verify both approaches produce the same results
            assertThat(results1.map { it.name }).isEqualTo(results2.map { it.name })
            assertThat(results1.map { it.name }).containsExactlyInAnyOrder(
                "Lord of the Rings",
            )
        }
    }

    @Test
    fun `applyJoinRefs usage demonstration`() {
        transactor.open { session ->
            val criteria = session.query(BookTable)
            val authorsRef = criteria.joinRef { author }
            val publishersRef = criteria.joinRef { publisher }

            criteria.applyJoinRefs(authorsRef, publishersRef) { authors, publisher ->
                addLike(authors.name, "J.%")
                addEq(publisher.name, "HarperCollins")
            }

            val results = criteria.list()
            assertThat(results.map { it.name }).containsExactlyInAnyOrder(
                "Lord of the Rings",
            )
        }
    }

    @Test
    fun `applyJoinRefs with 2 references test, one is from a multiple join - reviewer and publisher`() {
        transactor.open { session ->
            val criteria = session.query(BookReviewTable)
            val reviewerRef = criteria.joinRef { reviewer }
            val bookRef = criteria.joinRef { book }
            val publisherRef = criteria.joinRef {
                val book = bookRef.get(this)
                book.publisher
            }

            criteria.applyJoinRefs(reviewerRef, publisherRef) { reviewer, publisher ->
                addLike(reviewer.name, "%Doe%")
                addEq(publisher.name, "HarperCollins")
            }

            val results = criteria.list()
            assertThat(results.map { it.reviewText }).containsExactlyInAnyOrder(
                "Frodo was pretty cool.",
            )
        }
    }

    @Test
    fun `applyJoinRefs with 3 references test - reviewer, book, and publisher`() {
        transactor.open { session ->
            val criteria = session.query(BookReviewTable)
            val reviewerRef = criteria.joinRef { reviewer }
            val bookRef = criteria.joinRef { book }
            val publisherRef = criteria.joinRef {
                val book = bookRef.get(this)
                book.publisher
            }

            criteria.applyJoinRefs(reviewerRef, bookRef, publisherRef) { reviewer, book, publisher ->
                addLike(reviewer.name, "%Doe%")
                addEq(book.originalLanguage, ENGLISH)
                addEq(publisher.name, "HarperCollins")
            }

            val results = criteria.list()
            assertThat(results.map { it.reviewText }).containsExactlyInAnyOrder(
                "Frodo was pretty cool.",
            )
        }
    }
}
