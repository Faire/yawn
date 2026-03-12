package com.faire.yawn.database

import com.faire.yawn.project.YawnProjection
import com.faire.yawn.project.YawnProjections
import com.faire.yawn.setup.entities.BookTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class YawnProjectionMappingTest : BaseYawnDatabaseTest() {
    @Test
    fun `yawn query with projection`() {
        transactor.open { session ->
            val hobbit = session.project(BookTable) { books ->
                addEq(books.name, "The Hobbit")
                val authors = join(books.author)
                project(
                    YawnProjectionMappingTest_BookNameAndNotesProjection.create(
                        uppercaseTitle = YawnProjections.mapping(books.name) { it.uppercase() },
                        authorNotes = YawnProjections.mapping(authors.name, books.notes) { author, notes ->
                            "$author says: $notes"
                        },
                    ),
                )
            }.uniqueResult()

            with(hobbit!!) {
                assertThat(uppercaseTitle).isEqualTo("THE HOBBIT")
                assertThat(authorNotes).isEqualTo("J.R.R. Tolkien says: J.R.R. Tolkien")
            }
        }
    }

    @YawnProjection
    internal data class BookNameAndNotes(
        val uppercaseTitle: String,
        val authorNotes: String,
    )
}
