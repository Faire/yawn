package com.faire.yawn.database

import com.faire.yawn.Yawn.Companion.createProjectedDetachedCriteria
import com.faire.yawn.project.YawnProjections
import com.faire.yawn.setup.entities.BookTable
import com.faire.yawn.setup.entities.PersonTable
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.sql.JoinType
import org.junit.jupiter.api.Test

internal class YawnSubQueryTest : BaseYawnDatabaseTest() {
    @Test
    fun `yawn query with a sub query using detached criteria`() {
        val detachedCriteria = createProjectedDetachedCriteria(PersonTable) { person ->
            addEq(person.name, "J.R.R. Tolkien")

            project(person.name)
        }

        transactor.open { session ->
            val book = session.query(BookTable) { books ->
                val authors = join(books.author)
                addEq(authors.name, detachedCriteria)

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
        val detachedCriteria = createProjectedDetachedCriteria(PersonTable) { person ->
            addEq(person.name, "J.R.R. Tolkien")
            project(person.id)
        }

        transactor.open { session ->
            val book = session.query(BookTable) { books ->
                addEq(books.author, detachedCriteria)
                addLt(books.numberOfPages, 500)
            }.uniqueResult()!!

            with(book) {
                assertThat(name).isEqualTo("The Hobbit")
                assertThat(author.name).isEqualTo("J.R.R. Tolkien")
            }
        }
    }

    @Test
    fun `yawn query with a sub query using detached criteria projected to a collection`() {
        val detachedCriteria = createProjectedDetachedCriteria(PersonTable) { person ->
            addLike(person.name, "J.%")
            project(YawnProjections.distinct(person.name))
        }

        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                val authors = join(books.author)
                addIn(authors.name, detachedCriteria)
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
        val detachedCriteria = createProjectedDetachedCriteria(BookTable) { books ->
            val publishers = join(books.publisher, joinType = JoinType.LEFT_OUTER_JOIN)
            addIsNull(publishers.id)
            project(books.author.foreignKey)
        }

        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addIn(people.id, detachedCriteria)
            }.list()

            assertThat(people).hasSize(1)
            assertThat(people.single().name).isEqualTo("Hans Christian Andersen")
        }
    }

    @Test
    fun `yawn query with a sub query using detached criteria with join criteria`() {
        val detachedCriteria = createProjectedDetachedCriteria(BookTable) { books ->
            val publishers = join(books.publisher, joinType = JoinType.LEFT_OUTER_JOIN) { publisher ->
                addNotLike(publisher.name, "%-%")
                addNotLike(publisher.name, "% %")
            }
            addIsNull(publishers.id)
            project(books.author.foreignKey)
        }

        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addIn(people.id, detachedCriteria)
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
                val subQuery = createProjectedSubQuery(BookTable.forSubQuery()) { books ->
                    addEq(books.author.foreignKey, people.id)
                    addGt(books.numberOfPages, 500)
                    project(books.author.foreignKey)
                }

                val secondSubQuery = createProjectedSubQuery(BookTable.forSubQuery()) { books ->
                    addEq(books.author.foreignKey, people.id)
                    addNotEq(books.name, "Fake book")
                    project(books.author.foreignKey)
                }

                addExists(subQuery)
                addExists(secondSubQuery)
            }.list()

            assertThat(people.single().name).isEqualTo("J.R.R. Tolkien")
        }
    }
}
