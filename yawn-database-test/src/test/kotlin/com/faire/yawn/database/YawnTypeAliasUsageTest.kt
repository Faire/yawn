package com.faire.yawn.database

import com.faire.yawn.Yawn
import com.faire.yawn.setup.entities.BookEntityQueryScope
import com.faire.yawn.setup.entities.BookJoinQueryScope
import com.faire.yawn.setup.entities.BookProjectedQueryScope
import com.faire.yawn.setup.entities.BookTable
import com.faire.yawn.setup.entities.BookTableDefType
import com.faire.yawn.setup.entities.PersonTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class YawnTypeAliasUsageTest : BaseYawnDatabaseTest() {
    @Test
    fun `generated type aliases work in query extension helpers`() {
        transactor.open { session ->
            val hobbit = session.query(BookTable) { books ->
                filterShortBooks(books)
                addEq(books.name, "The Hobbit")
            }.uniqueResult()!!

            assertThat(hobbit.name).isEqualTo("The Hobbit")
            assertThat(hobbit.numberOfPages).isLessThan(500)
        }

        transactor.open { session ->
            val paul = session.query(PersonTable) { people ->
                addEq(people.name, "Paul Duchesne")
                join(people.favoriteBook) { book ->
                    inJoinQueryScope()
                    addGt(book.numberOfPages, 500)
                }
            }.uniqueResult()!!

            assertThat(paul.name).isEqualTo("Paul Duchesne")
            assertThat(paul.favoriteBook!!.name).isEqualTo("Lord of the Rings")
            assertThat(paul.favoriteBook!!.numberOfPages).isGreaterThan(500)
        }

        transactor.open { session ->
            val name = session.project(BookTable) { books ->
                inProjectedQueryScope()
                addLt(books.numberOfPages, 500)
                addEq(books.name, "The Hobbit")
                project(books.name)
            }.uniqueResult()

            assertThat(name).isEqualTo("The Hobbit")
        }

        val detachedCriteria = Yawn.createProjectedDetachedCriteria(BookTable) { books ->
            inProjectedQueryScope()
            addEq(books.name, "The Hobbit")
            addLt(books.numberOfPages, 500)
            project(books.name)
        }

        transactor.open { session ->
            val book = session.query(BookTable) { books ->
                addEq(books.name, detachedCriteria)
            }.uniqueResult()!!

            assertThat(book.name).isEqualTo("The Hobbit")
            assertThat(book.numberOfPages).isLessThan(500)
        }
    }

    private fun BookEntityQueryScope.filterShortBooks(books: BookTableDefType) {
        addLt(books.numberOfPages, 500)
    }

    private fun BookJoinQueryScope.inJoinQueryScope() = Unit

    private fun BookProjectedQueryScope<*>.inProjectedQueryScope() = Unit
}
