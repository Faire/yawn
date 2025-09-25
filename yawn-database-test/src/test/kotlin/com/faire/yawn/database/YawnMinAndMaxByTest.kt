package com.faire.yawn.database

import com.faire.yawn.setup.entities.BookTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class YawnMinAndMaxByTest : BaseYawnDatabaseTest() {
    @Test
    fun `yawn maxBy works`() {
        transactor.open { session ->
            assertThat(session.query(BookTable).maxBy { createdAt }!!.name).isEqualTo("The Emperor's New Clothes")
            assertThat(session.query(BookTable).maxBy { name }!!.name).isEqualTo("The Ugly Duckling")
            assertThat(session.query(BookTable).maxBy { numberOfPages }!!.name).isEqualTo("Lord of the Rings")
        }
    }

    @Test
    fun `yawn minBy works`() {
        transactor.open { session ->
            assertThat(session.query(BookTable).minBy { createdAt }!!.name).isEqualTo("Lord of the Rings")
            assertThat(session.query(BookTable).minBy { name }!!.name).isEqualTo("Harry Potter")
            assertThat(session.query(BookTable).minBy { numberOfPages }!!.name).isEqualTo("The Little Mermaid")
        }
    }

    @Test
    fun `yawn maxByMultiple works`() {
        transactor.open { session ->
            // Max by originalLanguage is "ENGLISH" (Tolkien, Rowling)
            // Max of "Lord of the Rings", "The Hobbit", and "Harry Potter" is "The Hobbit"
            val maxBy = session.query(BookTable).maxBy({ originalLanguage }, { name })!!
            assertThat(maxBy.name).isEqualTo("The Hobbit")
        }
    }

    @Test
    fun `yawn minByMultiple works`() {
        transactor.open { session ->
            // Min by originalLanguage is "DANISH" (Andersen)
            // Min of "The Little Mermaid", "The Ugly Duckling", and "The Emperor's New Clothes"
            val minBy = session.query(BookTable).minBy({ originalLanguage }, { name })!!
            assertThat(minBy.name).isEqualTo("The Emperor's New Clothes")
        }
    }
}
