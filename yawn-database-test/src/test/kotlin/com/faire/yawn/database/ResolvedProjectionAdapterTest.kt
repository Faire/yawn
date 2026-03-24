package com.faire.yawn.database

import com.faire.yawn.project.AggregateKind.AVG
import com.faire.yawn.project.AggregateKind.COUNT
import com.faire.yawn.project.AggregateKind.COUNT_DISTINCT
import com.faire.yawn.project.AggregateKind.GROUP_BY
import com.faire.yawn.project.AggregateKind.MAX
import com.faire.yawn.project.AggregateKind.MIN
import com.faire.yawn.project.AggregateKind.SUM
import com.faire.yawn.project.ModifierKind.DISTINCT
import com.faire.yawn.project.ProjectionLeaf
import com.faire.yawn.project.ProjectionNode
import com.faire.yawn.project.ProjectorResolver
import com.faire.yawn.project.ResolvedProjectionAdapter
import com.faire.yawn.project.YawnProjector
import com.faire.yawn.project.YawnValueProjector
import com.faire.yawn.query.YawnQueryOrder
import com.faire.yawn.setup.entities.Book
import com.faire.yawn.setup.entities.Book.Language.ENGLISH
import com.faire.yawn.setup.entities.BookTable
import com.faire.yawn.setup.entities.PublisherTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Integration tests for [ResolvedProjectionAdapter], verifying that the new projection system
 * produces correct SQL and result mapping when bridged into the existing Hibernate pipeline.
 */
internal class ResolvedProjectionAdapterTest : BaseYawnDatabaseTest() {
    @Test
    fun `single property projection`() {
        transactor.open { session ->
            val languages = session.project(BookTable) { books ->
                addEq(books.name, "The Hobbit")
                project(adapt(YawnValueProjector { ProjectionNode.property(books.originalLanguage) }))
            }.list()

            assertThat(languages).containsOnly(ENGLISH)
        }
    }

    @Test
    fun `aggregate - sum`() {
        transactor.open { session ->
            val sum = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")
                project(adapt(YawnValueProjector { ProjectionNode.aggregate(SUM, books.numberOfPages) }))
            }.uniqueResult()!!

            assertThat(sum).isEqualTo(1_300L)
        }
    }

    @Test
    fun `aggregate - count`() {
        transactor.open { session ->
            val count = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "Hans Christian Andersen")
                project(adapt(YawnValueProjector<Book, Long> { ProjectionNode.aggregateAs(COUNT, books.id) }))
            }.uniqueResult()!!

            assertThat(count).isEqualTo(3L)
        }
    }

    @Test
    fun `aggregate - count distinct`() {
        transactor.open { session ->
            val count = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "Hans Christian Andersen")
                project(
                    adapt(YawnValueProjector<Book, Long> { ProjectionNode.aggregateAs(COUNT_DISTINCT, authors.name) }),
                )
            }.uniqueResult()!!

            assertThat(count).isEqualTo(1L)
        }
    }

    @Test
    fun `aggregate - avg`() {
        transactor.open { session ->
            val avg = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")
                project(
                    adapt(YawnValueProjector<Book, Double> { ProjectionNode.aggregateAs(AVG, books.numberOfPages) }),
                )
            }.uniqueResult()!!

            assertThat(avg).isEqualTo(650.0)
        }
    }

    @Test
    fun `aggregate - min and max`() {
        transactor.open { session ->
            val min = session.project(BookTable) { books ->
                project(adapt(YawnValueProjector { ProjectionNode.aggregate(MIN, books.numberOfPages) }))
            }.uniqueResult()!!
            assertThat(min).isEqualTo(100L)

            val max = session.project(BookTable) { books ->
                project(adapt(YawnValueProjector { ProjectionNode.aggregate(MAX, books.numberOfPages) }))
            }.uniqueResult()!!
            assertThat(max).isEqualTo(1_000L)
        }
    }

    @Test
    fun `row count`() {
        transactor.open { session ->
            val count = session.project(BookTable) {
                project(adapt(YawnValueProjector { ProjectionNode.rowCount() }))
            }.uniqueResult()!!

            assertThat(count).isEqualTo(6L)
        }
    }

    @Test
    fun `distinct modifier`() {
        transactor.open { session ->
            val authors = session.project(BookTable) { books ->
                val authors = join(books.author)
                project(
                    adapt(
                        YawnValueProjector<Book, String> {
                            ProjectionNode.Value(
                                ProjectionLeaf.Modifier(DISTINCT, ProjectionLeaf.Property(authors.name)),
                            )
                        },
                    ),
                )
            }.list()

            assertThat(authors).containsExactlyInAnyOrder(
                "J.R.R. Tolkien",
                "J.K. Rowling",
                "Hans Christian Andersen",
            )
        }
    }

    @Test
    fun `composite pair projection`() {
        transactor.open { session ->
            val result = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.K. Rowling")
                project(
                    adapt {
                        ProjectionNode.composite(
                            YawnValueProjector { ProjectionNode.property(authors.name) },
                            YawnValueProjector { ProjectionNode.aggregate(SUM, books.numberOfPages) },
                        ) { a, b -> a to b }
                    },
                )
            }.uniqueResult()!!

            assertThat(result).isEqualTo("J.K. Rowling" to 500L)
        }
    }

    @Test
    fun `composite with group by`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                val authors = join(books.author)
                project(
                    adapt {
                        ProjectionNode.composite(
                            YawnValueProjector { ProjectionNode.aggregate(GROUP_BY, authors.name) },
                            YawnValueProjector<Book, Long> { ProjectionNode.aggregateAs(COUNT, books.name) },
                        ) { author, count -> author to count }
                    },
                )
            }.list()

            assertThat(results).containsExactlyInAnyOrder(
                "J.R.R. Tolkien" to 2L,
                "J.K. Rowling" to 1L,
                "Hans Christian Andersen" to 3L,
            )
        }
    }

    @Test
    fun `composite with constant and mapped`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                addEq(books.name, "The Hobbit")
                project(
                    adapt {
                        ProjectionNode.composite(
                            YawnValueProjector { ProjectionNode.property(books.name) },
                            { ProjectionNode.constant("hardcoded") },
                            {
                                ProjectionNode.mapped(
                                    YawnValueProjector { ProjectionNode.property(books.numberOfPages) },
                                ) { pages -> "pages=$pages" }
                            },
                        ) { a, b, c -> Triple(a, b, c) }
                    },
                )
            }.list()

            assertThat(results).containsOnly(Triple("The Hobbit", "hardcoded", "pages=300"))
        }
    }

    @Test
    fun `deduplication shares result slot`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")
                project(
                    adapt {
                        ProjectionNode.composite(
                            YawnValueProjector { ProjectionNode.aggregate(SUM, books.numberOfPages) },
                            YawnValueProjector { ProjectionNode.aggregate(SUM, books.numberOfPages) },
                        ) { a, b -> a to b }
                    },
                )
            }.uniqueResult()!!

            assertThat(results).isEqualTo(1_300L to 1_300L)
        }
    }

    @Test
    fun `sql leaf projection`() {
        transactor.open { session ->
            val results = session.project(BookTable) {
                project(
                    adapt(
                        YawnValueProjector<Book, Long> {
                            ProjectionNode.sql(
                                sqlExpression = "COUNT(*) AS total",
                                aliases = listOf("total"),
                                resultTypes = listOf(Long::class),
                            )
                        },
                    ),
                )
            }.uniqueResult()!!

            assertThat(results).isEqualTo(6L)
        }
    }

    @Test
    fun `triple composite projection`() {
        transactor.open { session ->
            val result = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.K. Rowling")
                project(
                    adapt {
                        ProjectionNode.composite(
                            YawnValueProjector { ProjectionNode.property(books.name) },
                            YawnValueProjector { ProjectionNode.property(authors.name) },
                            YawnValueProjector { ProjectionNode.aggregate(SUM, books.numberOfPages) },
                        ) { a, b, c -> Triple(a, b, c) }
                    },
                )
            }.uniqueResult()!!

            assertThat(result).isEqualTo(Triple("Harry Potter", "J.K. Rowling", 500L))
        }
    }

    @Test
    fun `property on joined table`() {
        transactor.open { session ->
            val results1 = session.project(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, "J.R.R. Tolkien")
                project(adapt(YawnValueProjector { ProjectionNode.property(authors.name) }))
            }.set()
            assertThat(results1).containsExactlyInAnyOrder("J.R.R. Tolkien")

            val results2 = session.project(BookTable) { books ->
                addLike(books.name, "The %")
                val authors = join(books.author)
                project(adapt(YawnValueProjector { ProjectionNode.property(authors.name) }))
            }.set()
            assertThat(results2).containsExactlyInAnyOrder("J.R.R. Tolkien", "Hans Christian Andersen")
        }
    }

    @Test
    fun `projection with ordering`() {
        transactor.open { session ->
            val resultsAsc = session.project(BookTable) { books ->
                orderAsc(books.name)
                project(adapt(YawnValueProjector { ProjectionNode.property(books.name) }))
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
                project(adapt(YawnValueProjector { ProjectionNode.property(books.name) }))
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
                project(
                    adapt {
                        ProjectionNode.composite(
                            YawnValueProjector { ProjectionNode.property(authors.name) },
                            YawnValueProjector { ProjectionNode.property(books.name) },
                        ) { author, book -> "$author - $book" }
                    },
                )
            }.list()

            assertThat(resultMultiple).containsExactly(
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
    fun `foreign key projection`() {
        transactor.open { session ->
            val publisherIdMap = session.project(PublisherTable) { publishers ->
                project(
                    adapt {
                        ProjectionNode.composite(
                            YawnValueProjector { ProjectionNode.property(publishers.name) },
                            YawnValueProjector { ProjectionNode.property(publishers.id) },
                        ) { name, id -> name to id }
                    },
                )
            }.set().toMap()

            val publisherWithThe = session.project(BookTable) { books ->
                addLike(books.name, "The %")
                addIsNotNull(books.publisher.foreignKey)
                project(adapt(YawnValueProjector { ProjectionNode.property(books.publisher.foreignKey) }))
            }.set()
            assertThat(publisherWithThe)
                .containsExactlyInAnyOrder(
                    publisherIdMap.getValue("Penguin"),
                    publisherIdMap.getValue("Random House"),
                )
        }
    }

    @Test
    fun `sum with nullable columns`() {
        transactor.open { session ->
            fun getSumRatingsByLanguage(vararg authorNames: String): Map<Book.Language, Any?> {
                return session.project(BookTable) { books ->
                    val authors = join(books.author)
                    addIn(authors.name, *authorNames)
                    project(
                        adapt {
                            ProjectionNode.composite(
                                YawnValueProjector { ProjectionNode.aggregate(GROUP_BY, books.originalLanguage) },
                                YawnValueProjector<Book, Long?> { ProjectionNode.aggregateAs(SUM, books.rating) },
                            ) { lang, sum -> lang to sum }
                        },
                    )
                }.list().associate { it.first to it.second }
            }

            // All null ratings
            assertThat(
                getSumRatingsByLanguage("J.K. Rowling"),
            ).containsExactlyInAnyOrderEntriesOf(mapOf(ENGLISH to null))

            // Mixed null and non-null, nulls are ignored
            assertThat(getSumRatingsByLanguage("J.K. Rowling", "J.R.R. Tolkien"))
                .containsExactlyInAnyOrderEntriesOf(mapOf(ENGLISH to 19L))

            // All non-null
            assertThat(
                getSumRatingsByLanguage("J.R.R. Tolkien"),
            ).containsExactlyInAnyOrderEntriesOf(mapOf(ENGLISH to 19L))
        }
    }

    @Test
    fun `avg with nullable columns`() {
        transactor.open { session ->
            fun getAvgRatingsByLanguage(vararg authorNames: String): Map<Book.Language, Any?> {
                return session.project(BookTable) { books ->
                    val authors = join(books.author)
                    addIn(authors.name, *authorNames)
                    project(
                        adapt {
                            ProjectionNode.composite(
                                YawnValueProjector { ProjectionNode.aggregate(GROUP_BY, books.originalLanguage) },
                                YawnValueProjector<Book, Double?> { ProjectionNode.aggregateAs(AVG, books.rating) },
                            ) { lang, avg -> lang to avg }
                        },
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
    fun `null constant in composite`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                addIn(books.name, setOf("The Hobbit", "The Little Mermaid"))
                val authors = join(books.author)
                project(
                    adapt {
                        ProjectionNode.composite(
                            YawnValueProjector { ProjectionNode.property(books.name) },
                            { ProjectionNode.constant<Book, Long?>(null) },
                            YawnValueProjector { ProjectionNode.property(authors.name) },
                        ) { name, nullLong, author -> Triple(name, nullLong, author) }
                    },
                )
            }.list()

            assertThat(results).containsExactlyInAnyOrder(
                Triple("The Hobbit", null, "J.R.R. Tolkien"),
                Triple("The Little Mermaid", null, "Hans Christian Andersen"),
            )
        }
    }

    @Test
    fun `coalesce via mapped transform`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                addIn(books.name, setOf("The Hobbit", "The Little Mermaid"))
                orderAsc(books.name)
                project(
                    adapt {
                        ProjectionNode.composite(
                            YawnValueProjector { ProjectionNode.property(books.name) },
                            {
                                ProjectionNode.mapped(
                                    YawnValueProjector { ProjectionNode.property(books.notes) },
                                ) { it ?: "fallback" }
                            },
                        ) { name, notes -> name to notes }
                    },
                )
            }.list()

            assertThat(results).containsExactly(
                "The Hobbit" to "Note for The Hobbit and Harry Potter",
                "The Little Mermaid" to "fallback",
            )
        }
    }

    @Test
    fun `apply filter on projected query`() {
        transactor.open { session ->
            val tolkienStats = session.query(BookTable)
                .applyProjection { books ->
                    project(
                        adapt {
                            ProjectionNode.composite(
                                YawnValueProjector<Book, Long> { ProjectionNode.aggregateAs(COUNT, books.id) },
                                YawnValueProjector { ProjectionNode.aggregate(SUM, books.numberOfPages) },
                            ) { totalBooks, totalPages -> totalBooks to totalPages }
                        },
                    )
                }
                .applyFilter { books ->
                    val authors = join(books.author)
                    addEq(authors.name, "J.R.R. Tolkien")
                }
                .uniqueResult()!!

            assertThat(tolkienStats.first).isEqualTo(2L)
            assertThat(tolkienStats.second).isEqualTo(1_300L)

            val multipleAuthorsStats = session.query(BookTable)
                .applyProjection { books ->
                    project(
                        adapt {
                            ProjectionNode.composite(
                                YawnValueProjector<Book, Long> { ProjectionNode.aggregateAs(COUNT, books.id) },
                                YawnValueProjector { ProjectionNode.aggregate(SUM, books.numberOfPages) },
                            ) { totalBooks, totalPages -> totalBooks to totalPages }
                        },
                    )
                }
                .applyFilter { books ->
                    val authors = join(books.author)
                    addIn(authors.name, "J.R.R. Tolkien", "Hans Christian Andersen")
                }
                .uniqueResult()!!

            assertThat(multipleAuthorsStats.first).isEqualTo(5L)
            assertThat(multipleAuthorsStats.second).isEqualTo(1_630L)
        }
    }

    @Test
    fun `apply multiple filters on projected query`() {
        transactor.open { session ->
            val stats = session.query(BookTable)
                .applyProjection { books ->
                    project(
                        adapt {
                            ProjectionNode.composite(
                                YawnValueProjector<Book, Long> { ProjectionNode.aggregateAs(COUNT, books.id) },
                                YawnValueProjector { ProjectionNode.aggregate(SUM, books.numberOfPages) },
                            ) { totalBooks, totalPages -> totalBooks to totalPages }
                        },
                    )
                }
                .applyFilter { books ->
                    val authors = join(books.author)
                    addIn(authors.name, "J.R.R. Tolkien", "Hans Christian Andersen")
                }
                .applyFilter { books ->
                    addGt(books.numberOfPages, 110L)
                }
                .uniqueResult()!!

            assertThat(stats.first).isEqualTo(3L)
            assertThat(stats.second).isEqualTo(1_420L)
        }
    }

    @Test
    fun `nested composites - proving flattening works`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                val authors = join(books.author)
                project(
                    adapt {
                        ProjectionNode.composite(
                            // outer level: author name (group by)
                            YawnValueProjector { ProjectionNode.aggregate(GROUP_BY, authors.name) },
                            // nested composite: page stats
                            {
                                ProjectionNode.composite(
                                    YawnValueProjector<Book, Long> { ProjectionNode.aggregateAs(COUNT, books.id) },
                                    YawnValueProjector { ProjectionNode.aggregate(SUM, books.numberOfPages) },
                                ) { count, sum -> count to sum }
                            },
                        ) { author, stats -> author to stats }
                    },
                )
            }.list()

            assertThat(results).containsExactlyInAnyOrder(
                "J.R.R. Tolkien" to (2L to 1_300L),
                "J.K. Rowling" to (1L to 500L),
                "Hans Christian Andersen" to (3L to 330L),
            )
        }
    }

    @Test
    fun `deeply nested composites`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                val authors = join(books.author)
                project(
                    adapt {
                        ProjectionNode.composite(
                            YawnValueProjector { ProjectionNode.aggregate(GROUP_BY, authors.name) },
                            {
                                ProjectionNode.composite(
                                    // first inner: count + sum
                                    {
                                        ProjectionNode.composite(
                                            YawnValueProjector<Book, Long> {
                                                ProjectionNode.aggregateAs(
                                                    COUNT,
                                                    books.id,
                                                )
                                            },
                                            YawnValueProjector { ProjectionNode.aggregate(SUM, books.numberOfPages) },
                                        ) { count, sum -> count to sum }
                                    },
                                    // second inner: min + max
                                    {
                                        ProjectionNode.composite(
                                            YawnValueProjector { ProjectionNode.aggregate(MIN, books.numberOfPages) },
                                            YawnValueProjector { ProjectionNode.aggregate(MAX, books.numberOfPages) },
                                        ) { min, max -> min to max }
                                    },
                                ) { countSum, minMax -> countSum to minMax }
                            },
                        ) { author, stats -> author to stats }
                    },
                )
            }.list()

            assertThat(results).containsExactlyInAnyOrder(
                "J.R.R. Tolkien" to Pair(2L to 1_300L, 300L to 1_000L),
                "J.K. Rowling" to Pair(1L to 500L, 500L to 500L),
                "Hans Christian Andersen" to Pair(3L to 330L, 100L to 120L),
            )
        }
    }

    private fun <SOURCE : Any, TO> adapt(
        projector: YawnProjector<SOURCE, TO>,
    ): ResolvedProjectionAdapter<SOURCE, TO> {
        val resolved = ProjectorResolver<SOURCE>().resolve(projector)
        return ResolvedProjectionAdapter(resolved)
    }
}
