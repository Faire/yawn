package com.faire.yawn.database

import com.faire.yawn.Yawn
import com.faire.yawn.query.YawnSubQueryRestrictions
import com.faire.yawn.setup.entities.BookTable
import com.faire.yawn.setup.entities.PersonTable
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.sql.JoinType
import org.junit.jupiter.api.Test

internal class YawnSubQueryTest : BaseYawnDatabaseTest() {
    @Test
    fun `yawn query with a sub query using detached criteria`() {
        val selectTolkienNames = Yawn.createProjectedDetachedCriteria(PersonTable) { people ->
            addEq(people.name, "J.R.R. Tolkien")

            project(people.name)
        }

        transactor.open { session ->
            val book = session.query(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, selectTolkienNames)

                addLt(books.numberOfPages, 500)
            }.uniqueResult()!!

            with(book) {
                assertThat(name).isEqualTo("The Hobbit")
                assertThat(author.name).isEqualTo("J.R.R. Tolkien")
            }
        }
    }

    @Test
    fun `yawn query with a sub query using detached criteria with join`() {
        val selectTolkienIds = Yawn.createProjectedDetachedCriteria(PersonTable) { people ->
            addEq(people.name, "J.R.R. Tolkien")
            project(people.id)
        }

        transactor.open { session ->
            val book = session.query(BookTable) { books ->
                addEq(books.author, selectTolkienIds)
                addLt(books.numberOfPages, 500)
            }.uniqueResult()!!

            with(book) {
                assertThat(name).isEqualTo("The Hobbit")
                assertThat(author.name).isEqualTo("J.R.R. Tolkien")
            }
        }
    }

    @Test
    fun `yawn query with a sub query using detached criteria with query-level distinct`() {
        val selectAuthorsStartingWithJ = Yawn.createProjectedDetachedCriteria(PersonTable) { people ->
            addLike(people.name, "J.%")
            project(people.name)
        }.distinct()

        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                val authors = join(books.author)
                addIn(authors.name, selectAuthorsStartingWithJ)
                addLt(books.numberOfPages, 500)
            }.list()

            assertThat(books).hasSize(1)
            with(books.single()) {
                assertThat(name).isEqualTo("The Hobbit")
                assertThat(author.name).isEqualTo("J.R.R. Tolkien")
            }
        }
    }

    @Test
    fun `yawn query with a sub query using detached criteria with left join`() {
        val selectAuthorsWithoutPublisher = Yawn.createProjectedDetachedCriteria(BookTable) { books ->
            val publishers = join(books.publisher, joinType = JoinType.LEFT_OUTER_JOIN)
            addIsNull(publishers.id)
            project(books.author.foreignKey)
        }

        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addIn(people.id, selectAuthorsWithoutPublisher)
            }.list()

            assertThat(people).hasSize(1)
            assertThat(people.single().name).isEqualTo("Hans Christian Andersen")
        }
    }

    @Test
    fun `yawn query with a sub query using detached criteria with join criteria`() {
        val selectAuthorsWithoutSimplePublisher = Yawn.createProjectedDetachedCriteria(BookTable) { books ->
            val publishers = join(books.publisher, joinType = JoinType.LEFT_OUTER_JOIN) { publishers ->
                addNotLike(publishers.name, "%-%")
                addNotLike(publishers.name, "% %")
            }
            addIsNull(publishers.id)
            project(books.author.foreignKey)
        }

        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addIn(people.id, selectAuthorsWithoutSimplePublisher)
            }.list()

            assertThat(people).hasSize(2)
            assertThat(people.map { it.name }).containsExactlyInAnyOrder("Hans Christian Andersen", "J.R.R. Tolkien")
        }
    }

    @Test
    fun `can find authors of large books using a correlated subquery`() {
        transactor.open { session ->
            // Authors who have written a 500+ page book
            val people = session.query(PersonTable) { people ->
                val selectAuthorsOfLargeBooks = createProjectedSubQuery(BookTable.forSubQuery()) { books ->
                    addEq(books.author.foreignKey, people.id)
                    addGt(books.numberOfPages, 500)
                    project(books.author.foreignKey)
                }

                val selectAuthorsOfRealBooks = createProjectedSubQuery(BookTable.forSubQuery()) { books ->
                    addEq(books.author.foreignKey, people.id)
                    addNotEq(books.name, "Fake book")
                    project(books.author.foreignKey)
                }

                addExists(selectAuthorsOfLargeBooks)
                addExists(selectAuthorsOfRealBooks)
            }.list()

            assertThat(people.single().name).isEqualTo("J.R.R. Tolkien")
        }
    }

    @Test
    fun `can use subquery restrictions within another restriction`() {
        transactor.open { session ->
            // Authors who have written a 500+ page book or who have written a book < 100 pages
            val people = session.query(PersonTable) { people ->
                val selectAuthorsOfLargeBooks = createProjectedSubQuery(BookTable.forSubQuery()) { books ->
                    addEq(books.author.foreignKey, people.id)
                    addGt(books.numberOfPages, 500)
                    project(books.author.foreignKey)
                }

                val selectAuthorsOfShortBooks = createProjectedSubQuery(BookTable.forSubQuery()) { books ->
                    addEq(books.author.foreignKey, people.id)
                    addLe(books.numberOfPages, 100)
                    project(books.author.foreignKey)
                }

                addOr(
                    YawnSubQueryRestrictions.exists(selectAuthorsOfLargeBooks),
                    YawnSubQueryRestrictions.exists(selectAuthorsOfShortBooks),
                )
            }.list()

            assertThat(people.map { it.name }).containsExactlyInAnyOrder("J.R.R. Tolkien", "Hans Christian Andersen")
        }
    }
}
