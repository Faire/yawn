package com.faire.yawn.database

import com.faire.yawn.project.YawnProjection
import com.faire.yawn.project.YawnProjections
import com.faire.yawn.query.YawnQueryOrder
import com.faire.yawn.setup.entities.Book
import com.faire.yawn.setup.entities.Book.Language.ENGLISH
import com.faire.yawn.setup.entities.BookTable
import com.faire.yawn.setup.entities.PublisherTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class YawnProjectionTest : BaseYawnDatabaseTest() {
    @Test
    fun `yawn query with projection`() {
        transactor.open { session ->
            val languages = session.project(BookTable) { books ->
                addEq(books.name, "The Hobbit")
                project(books.originalLanguage)
            }.list()

            val language = languages.single()
            assertThat(language).isEqualTo(ENGLISH)
        }
    }

    @Test
    fun `yawn query with post projection`() {
        transactor.open { session ->
            val languages = session.query(BookTable) { books ->
                addEq(books.name, "The Hobbit")
            }
                .applyProjection { project(it.originalLanguage) }
                .list()

            val language = languages.single()
            assertThat(language).isEqualTo(ENGLISH)
        }
    }

    @Test
    fun `yawn query with nullable post projection`() {
        transactor.open { session ->
            val metadata1 = session.query(BookTable) { books ->
                addEq(books.name, "The Hobbit")
            }
                .applyProjection { project(it.bookMetadata) }
                .uniqueResult()!!

            assertThat(metadata1.isbn).isEqualTo("978-0-261-10221-7")

            val metadata2 = session.query(BookTable) { books ->
                addEq(books.name, "Harry Potter")
            }
                .applyProjection { project(it.bookMetadata) }
                .uniqueResult()

            assertThat(metadata2).isNull()
        }
    }

    @Test
    fun `yawn query with projection - full entity`() {
        transactor.open { session ->
            val publishers = session.project(BookTable) { books ->
                addLike(books.name, "The %")

                // TODO(yawn): publisher should be nullable
                addIsNotNull(nullable(books.publisher.foreignKey))

                project(books.publisher)
            }.set()
            assertThat(publishers.map { it.name }).containsExactlyInAnyOrder(
                "Random House",
                "Penguin",
            )

            val publisher = session.project(BookTable) { books ->
                addEq(books.name, "Harry Potter")
                project(books.publisher)
            }.uniqueResult()!!
            assertThat(publisher.name).isEqualTo("Penguin")
        }
    }

    @Test
    fun `yawn query with projection - foreign key`() {
        transactor.open { session ->
            val publisherIdMap = session.project(PublisherTable) { publishers ->
                project(YawnProjections.pair(publishers.name, publishers.id))
            }.set().toMap()

            val publisherWithThe = session.project(BookTable) { books ->
                addLike(books.name, "The %")
                addIsNotNull(books.publisher.foreignKey)

                // note that no join is necessary when using the `foreignKey` helper!
                project(books.publisher.foreignKey)
            }.set()
            assertThat(publisherWithThe)
                .containsExactlyInAnyOrder(
                    publisherIdMap.getValue("Penguin"),
                    publisherIdMap.getValue("Random House"),
                )

            val publishersWithJ = session.project(BookTable) { books ->
                val authors = join(books.author)
                addLike(authors.name, "J.%")
                addIsNotNull(books.publisher.foreignKey)

                project(books.publisher.foreignKey)
            }.set()
            assertThat(publishersWithJ)
                .containsExactlyInAnyOrder(
                    publisherIdMap.getValue("HarperCollins"),
                    publisherIdMap.getValue("Penguin"),
                    publisherIdMap.getValue("Random House"),
                )
        }
    }

    @Test
    fun `yawn query with projection - foreign key via join`() {
        transactor.open { session ->
            val publisherIdMap = session.project(PublisherTable) { publishers ->
                project(YawnProjections.pair(publishers.name, publishers.id))
            }.set().toMap()

            val results = session.project(BookTable) { books ->
                addLike(books.name, "The %")

                // do not confuse!
                // addEq("parent.id", 123L) => addEq(children.parent.foreignKey, 123L)
                // createAlias("parent", "parent").addEq("parent.id", 123L) => addEq(join(children.parent).id, 123L)

                // though no join is necessary when using the `foreignKey` helper, we can def do one if desired!
                project(join(books.publisher).id) // is equivalent to `children.parent.foreignKey`
            }.set()
            assertThat(results)
                .containsExactlyInAnyOrder(
                    publisherIdMap.getValue("Random House"),
                    publisherIdMap.getValue("Penguin"),
                )
        }
    }

    @Test
    fun `yawn query with projection function - count`() {
        transactor.open { session ->
            val count = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "Hans Christian Andersen")
                project(YawnProjections.count(books.id))
            }.uniqueResult()!!
            assertThat(count).isEqualTo(3L)
        }
    }

    @Test
    fun `yawn query with projection function - count distinct`() {
        transactor.open { session ->
            val countToken = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "Hans Christian Andersen")
                project(YawnProjections.countDistinct(books.id))
            }.uniqueResult()!!
            assertThat(countToken).isEqualTo(3L)

            val countAuthor = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "Hans Christian Andersen")
                project(YawnProjections.countDistinct(authors.name))
            }.uniqueResult()!!
            assertThat(countAuthor).isEqualTo(1)
        }
    }

    @Test
    fun `yawn query with projection function - distinct`() {
        transactor.open { session ->
            val authors = session.project(BookTable) { books ->
                val authors = join(books.author)
                project(YawnProjections.distinct(authors.name))
            }.list()
            assertThat(authors).containsExactlyInAnyOrder(
                "J.R.R. Tolkien",
                "J.K. Rowling",
                "Hans Christian Andersen",
            )
        }
    }

    @Test
    fun `yawn query with projection function - sum`() {
        transactor.open { session ->
            val sum = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")
                project(YawnProjections.sum(books.numberOfPages))
            }.uniqueResult()!!
            assertThat(sum).isEqualTo(1_300L)
        }
    }

    @Test
    fun `yawn query with projection function - sum with nullable columns`() {
        transactor.open { session ->
            fun getSumRatingsByLanguage(vararg authorNames: String): Map<Book.Language, Long?> {
                return session.project(BookTable) { books ->
                    val authors = join(books.author)
                    addIn(authors.name, *authorNames)
                    project(
                        YawnProjections.pair(
                            YawnProjections.groupBy(books.originalLanguage),
                            YawnProjections.sum(books.rating),
                        ),
                    )
                }.list().associate { it.first to it.second }
            }

            // All null
            assertThat(
                getSumRatingsByLanguage("J.K. Rowling"),
            ).containsExactlyInAnyOrderEntriesOf(mapOf(ENGLISH to null))

            // Mixed null and non-null, nulls are ignored
            assertThat(getSumRatingsByLanguage("J.K. Rowling", "J.R.R. Tolkien"))
                .containsExactlyInAnyOrderEntriesOf(mapOf(ENGLISH to 19))

            // All non-null
            assertThat(
                getSumRatingsByLanguage("J.R.R. Tolkien"),
            ).containsExactlyInAnyOrderEntriesOf(mapOf(ENGLISH to 19))
        }
    }

    @Test
    fun `yawn query with projection function - avg`() {
        transactor.open { session ->
            val tolkienAvg = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")

                project(YawnProjections.avg(books.numberOfPages))
            }.uniqueResult()!!
            assertThat(tolkienAvg).isEqualTo(650.0)

            val andersenAvg = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "Hans Christian Andersen")

                project(YawnProjections.avg(books.numberOfPages))
            }.uniqueResult()!!
            assertThat(andersenAvg).isEqualTo(110.0)

            val totalAvg = session.project(BookTable) { books ->
                project(YawnProjections.avg(books.numberOfPages))
            }.uniqueResult()!!
            assertThat(totalAvg).isEqualTo(355.0)
        }
    }

    @Test
    fun `yawn query with projection function - avg with nullable columns`() {
        transactor.open { session ->
            fun getAvgRatingsByLanguage(vararg authorNames: String): Map<Book.Language, Double?> {
                return session.project(BookTable) { books ->
                    val authors = join(books.author)
                    addIn(authors.name, *authorNames)
                    project(
                        YawnProjections.pair(
                            YawnProjections.groupBy(books.originalLanguage),
                            YawnProjections.avg(books.rating),
                        ),
                    )
                }.list().associate { it.first to it.second }
            }

            // All null
            assertThat(
                getAvgRatingsByLanguage("J.K. Rowling"),
            ).containsExactlyInAnyOrderEntriesOf(mapOf(ENGLISH to null))

            // Mixed null and non-null, nulls are ignored
            assertThat(getAvgRatingsByLanguage("J.K. Rowling", "J.R.R. Tolkien"))
                .containsExactlyInAnyOrderEntriesOf(mapOf(ENGLISH to 9.5))

            // All non-null
            assertThat(
                getAvgRatingsByLanguage("J.R.R. Tolkien"),
            ).containsExactlyInAnyOrderEntriesOf(mapOf(ENGLISH to 9.5))
        }
    }

    @Test
    fun `yawn query with projection function - min & max`() {
        transactor.open { session ->
            val minAuthor = session.project(BookTable) { books ->
                val authors = join(books.author)
                project(YawnProjections.min(authors.name))
            }.uniqueResult()!!
            assertThat(minAuthor).isEqualTo("Hans Christian Andersen")

            val maxAuthor = session.project(BookTable) { books ->
                val authors = join(books.author)
                project(YawnProjections.max(authors.name))
            }.uniqueResult()!!
            assertThat(maxAuthor).isEqualTo("J.R.R. Tolkien")

            val minPages = session.project(BookTable) { books ->
                project(YawnProjections.min(books.numberOfPages))
            }.uniqueResult()!!
            assertThat(minPages).isEqualTo(100L)

            val maxRowlingPages = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.K. Rowling")
                project(YawnProjections.max(books.numberOfPages))
            }.uniqueResult()!!
            assertThat(maxRowlingPages).isEqualTo(500L)

            val distinctNotes = session.project(BookTable) { books ->
                project(YawnProjections.countDistinct(books.notes))
            }.uniqueResult()!!
            assertThat(distinctNotes).isEqualTo(2)
        }
    }

    @Test
    fun `yawn query with rowCount projection`() {
        transactor.open { session ->
            val allAuthors = session.query(BookTable).rowCount()
            assertThat(allAuthors).isEqualTo(6)
            val jaysWithDupes = session.query(BookTable) { books ->
                val authors = join(books.author)
                addLike(authors.name, "J.%")
            }.rowCount()
            assertThat(jaysWithDupes).isEqualTo(3)
            val uniqueJays = session.project(BookTable) { books ->
                val authors = join(books.author)
                addLike(authors.name, "J.%")
                project(YawnProjections.countDistinct(authors.name))
            }.uniqueResult()!!
            assertThat(uniqueJays).isEqualTo(2)
        }
    }

    @Test
    fun `yawn query with exists projection`() {
        transactor.open { session ->
            val existsMany = session.query(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")
            }.exists()
            assertThat(existsMany).isTrue()

            val existsOne = session.query(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")
                addEq(books.name, "The Hobbit")
            }.exists()
            assertThat(existsOne).isTrue()

            val existsZero = session.query(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")
                addEq(books.name, "The Hobbit 2 - Reloaded")
            }.exists()
            assertThat(existsZero).isFalse()
        }
    }

    @Test
    fun `yawn query with pair projection`() {
        transactor.open { session ->
            val uniqueResult = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.K. Rowling")
                project(YawnProjections.pair(authors.name, books.numberOfPages))
            }.uniqueResult()!!
            assertThat(uniqueResult.first).isEqualTo("J.K. Rowling")
            assertThat(uniqueResult.second).isEqualTo(500L)

            val list = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "Hans Christian Andersen")
                project(YawnProjections.pair(authors.name, books.numberOfPages))
            }.list()

            assertThat(list).containsExactlyInAnyOrder(
                "Hans Christian Andersen" to 100,
                "Hans Christian Andersen" to 110,
                "Hans Christian Andersen" to 120,
            )
        }
    }

    @Test
    fun `yawn query with triple projection`() {
        transactor.open { session ->
            val uniqueResult = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.K. Rowling")

                project(
                    YawnProjections.triple(books.name, authors.name, books.numberOfPages),
                )
            }.uniqueResult()!!
            assertThat(uniqueResult.first).isEqualTo("Harry Potter")
            assertThat(uniqueResult.second).isEqualTo("J.K. Rowling")
            assertThat(uniqueResult.third).isEqualTo(500L)

            val list = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "Hans Christian Andersen")
                orderAsc(books.numberOfPages)

                project(YawnProjections.triple(books.name, authors.name, books.numberOfPages))
            }.list()

            assertThat(list).containsExactly(
                Triple("The Little Mermaid", "Hans Christian Andersen", 100),
                Triple("The Ugly Duckling", "Hans Christian Andersen", 110),
                Triple("The Emperor's New Clothes", "Hans Christian Andersen", 120),
            )
        }
    }

    @YawnProjection
    internal data class SimpleBook(
        val author: String,
        val numberOfPages: Long,
    )

    @Test
    fun `yawn query with data class projection`() {
        transactor.open { session ->
            val uniqueResult = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.K. Rowling")
                project(
                    YawnProjectionTest_SimpleBookProjection.create(
                        author = authors.name,
                        numberOfPages = books.numberOfPages,
                    ),
                )
            }.uniqueResult()!!
            assertThat(uniqueResult.author).isEqualTo("J.K. Rowling")
            assertThat(uniqueResult.numberOfPages).isEqualTo(500L)

            val list = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "Hans Christian Andersen")

                project(
                    YawnProjectionTest_SimpleBookProjection.create(
                        author = authors.name,
                        numberOfPages = books.numberOfPages,
                    ),
                )
            }.list()

            assertThat(list).containsExactlyInAnyOrder(
                SimpleBook("Hans Christian Andersen", 100),
                SimpleBook("Hans Christian Andersen", 110),
                SimpleBook("Hans Christian Andersen", 120),
            )
        }
    }

    @YawnProjection
    internal data class AuthorAndBooks(
        val author: String,
        val numberOfBooks: Long,
    )

    @Test
    fun `yawn query with group by`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                val authors = join(books.author)
                project(
                    YawnProjectionTest_AuthorAndBooksProjection.create(
                        author = YawnProjections.groupBy(authors.name),
                        numberOfBooks = YawnProjections.count(books.name),
                    ),
                )
            }.list()

            assertThat(results).containsExactlyInAnyOrder(
                AuthorAndBooks("J.R.R. Tolkien", 2),
                AuthorAndBooks("J.K. Rowling", 1),
                AuthorAndBooks("Hans Christian Andersen", 3),
            )
        }
    }

    @Test
    fun `yawn query with projection and join`() {
        transactor.open { session ->
            val results1 = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")

                project(authors.name)
                // TODO(luan): calling list().toSet() here triggers some obscure detekt bug!
            }.set()
            assertThat(results1).containsExactlyInAnyOrder("J.R.R. Tolkien")

            val results2 = session.project(BookTable) { books ->
                addLike(books.name, "The %")

                val authors = join(books.author)
                project(authors.name)
            }.set()
            assertThat(results2).containsExactlyInAnyOrder("J.R.R. Tolkien", "Hans Christian Andersen")
        }
    }

    @YawnProjection
    internal data class AuthorAndBookName(
        val authorName: String,
        val bookName: String,
    )

    @Test
    fun `yawn query with projection and order`() {
        transactor.open { session ->
            val resultsAsc = session.project(BookTable) { books ->
                orderAsc(books.name)
                project(books.name)
            }.list()

            assertThat(resultsAsc).containsExactly(
                "Harry Potter",
                "Lord of the Rings",
                "The Emperor's New Clothes",
                "The Hobbit",
                "The Little Mermaid",
                "The Ugly Duckling",
            )

            val resultsDesc = session.project(BookTable) { books ->
                orderDesc(books.name)
                project(books.name)
            }.list()

            assertThat(resultsDesc).containsExactly(
                "The Ugly Duckling",
                "The Little Mermaid",
                "The Hobbit",
                "The Emperor's New Clothes",
                "Lord of the Rings",
                "Harry Potter",
            )

            val resultMultiple = session.project(BookTable) { books ->
                val authors = join(books.author)
                order(YawnQueryOrder.asc(authors.name), YawnQueryOrder.desc(books.name))
                project(YawnProjectionTest_AuthorAndBookNameProjection.create(authors.name, books.name))
            }.list()

            assertThat(resultMultiple.map { "${it.authorName} - ${it.bookName}" }).containsExactly(
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
    fun `yawn query with maxValueOf and minValueOf`() {
        transactor.open { session ->
            assertThat(session.query(BookTable).maxValueOf { name }).isEqualTo("The Ugly Duckling")
            assertThat(session.query(BookTable).minValueOf { name }).isEqualTo("Harry Potter")
        }
    }

    @YawnProjection
    data class NullabilityAllowance(
        val name: String,
        val aLong: Long,
        val aNullableLong: Long?,
        val aString: String,
        val aNullableString: String?,
    )

    @Test
    fun `non-null values can be coerced into null values in projections`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                addIn(books.name, setOf("The Hobbit", "The Little Mermaid"))
                project(
                    YawnProjectionTest_NullabilityAllowanceProjection.create(
                        name = books.name,
                        aLong = books.numberOfPages,
                        aNullableLong = books.numberOfPages,
                        // TODO(yawn): extended coalesce syntax
                        aString = YawnProjections.coalesce(books.notes, "fallback"),
                        aNullableString = books.notes,
                    ),
                )
            }.list()

            with(results.single { it.name == "The Hobbit" }) {
                assertThat(aLong).isEqualTo(300L)
                assertThat(aNullableLong).isEqualTo(300L)
                assertThat(aString).isEqualTo("Note for The Hobbit and Harry Potter")
                assertThat(aNullableString).isEqualTo("Note for The Hobbit and Harry Potter")
            }

            with(results.single { it.name == "The Little Mermaid" }) {
                assertThat(aLong).isEqualTo(100L)
                assertThat(aNullableLong).isEqualTo(100L)
                assertThat(aString).isEqualTo("fallback")
                assertThat(aNullableString).isNull()
            }
        }
    }

    @Test
    fun `use constant projection`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                addIn(books.name, setOf("The Hobbit", "The Little Mermaid"))

                val authors = join(books.author)
                project(
                    YawnProjectionTest_NullabilityAllowanceProjection.create(
                        name = books.name,
                        // TODO(yawn): support selectConstant for non-String types and multiple different values
                        aLong = books.numberOfPages,
                        aNullableLong = YawnProjections.`null`(),
                        aString = authors.name,
                        aNullableString = YawnProjections.`null`(),
                    ),
                )
            }.list()

            assertThat(results).containsExactlyInAnyOrder(
                NullabilityAllowance(
                    name = "The Hobbit",
                    aLong = 300,
                    aNullableLong = null,
                    aString = "J.R.R. Tolkien",
                    aNullableString = null,
                ),
                NullabilityAllowance(
                    name = "The Little Mermaid",
                    aLong = 100,
                    aNullableLong = null,
                    aString = "Hans Christian Andersen",
                    aNullableString = null,
                ),
            )
        }
    }
}
