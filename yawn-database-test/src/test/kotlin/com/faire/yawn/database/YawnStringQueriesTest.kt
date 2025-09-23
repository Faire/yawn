package com.faire.yawn.database

import com.faire.yawn.setup.entities.BookTable
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.criterion.MatchMode
import org.junit.jupiter.api.Test

internal class YawnStringQueriesTest : BaseYawnDatabaseTest() {
  @Test
  fun `like on non-nullable column`() {
    transactor.open { session ->
      val books = session.query(BookTable) { books ->
        val authors = join(books.author)
        addLike(authors.name, "J.R.R%")
      }.list()

      assertThat(books.map { it.name }).containsExactlyInAnyOrder(
          "The Hobbit",
          "Lord of the Rings",
      )
    }
  }

  @Test
  fun `like on nullable column`() {
    transactor.open { session ->
      val books = session.query(BookTable) { books ->
        addLike(books.notes, "Note for%")
      }.list()

      assertThat(books.map { it.name }).containsExactlyInAnyOrder(
          "The Hobbit",
          "Lord of the Rings",
          "Harry Potter",
      )
    }
  }

  @Test
  fun `like with match mode on non-nullable column`() {
    transactor.open { session ->
      val books = session.query(BookTable) { books ->
        val authors = join(books.author)
        addLike(authors.name, "J.R.R", MatchMode.START)
      }.list()

      assertThat(books.map { it.name }).containsExactlyInAnyOrder(
          "The Hobbit",
          "Lord of the Rings",
      )
    }
  }

  @Test
  fun `like with match mode on nullable column`() {
    transactor.open { session ->
      val books = session.query(BookTable) { books ->
        addLike(books.notes, "Note for", MatchMode.START)
      }.list()

      assertThat(books.map { it.name }).containsExactlyInAnyOrder(
          "The Hobbit",
          "Lord of the Rings",
          "Harry Potter",
      )
    }
  }

  @Test
  fun `ilike on non-nullable column`() {
    transactor.open { session ->
      val books = session.query(BookTable) { books ->
        val authors = join(books.author)
        addILike(authors.name, "j.r.r%")
      }.list()

      assertThat(books.map { it.name }).containsExactlyInAnyOrder(
          "The Hobbit",
          "Lord of the Rings",
      )
    }
  }

  @Test
  fun `ilike on nullable column`() {
    transactor.open { session ->
      val books = session.query(BookTable) { books ->
        addILike(books.notes, "nOtE FoR%")
      }.list()

      assertThat(books.map { it.name }).containsExactlyInAnyOrder(
          "The Hobbit",
          "Lord of the Rings",
          "Harry Potter",
      )
    }
  }

  @Test
  fun `ilike with match mode on non-nullable column`() {
    transactor.open { session ->
      val books = session.query(BookTable) { books ->
        val authors = join(books.author)
        addILike(authors.name, "j.r.r", MatchMode.START)
      }.list()

      assertThat(books.map { it.name }).containsExactlyInAnyOrder(
          "The Hobbit",
          "Lord of the Rings",
      )
    }
  }

  @Test
  fun `ilike with match mode on nullable column`() {
    transactor.open { session ->
      val books = session.query(BookTable) { books ->
        addILike(books.notes, "nOtE fOr", MatchMode.START)
      }.list()

      assertThat(books.map { it.name }).containsExactlyInAnyOrder(
          "The Hobbit",
          "Lord of the Rings",
          "Harry Potter",
      )
    }
  }
}
