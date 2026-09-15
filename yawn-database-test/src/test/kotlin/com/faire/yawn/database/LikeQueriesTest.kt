package com.faire.yawn.database

import com.faire.yawn.query.YawnRestrictions
import com.faire.yawn.setup.custom.EmailAddress
import com.faire.yawn.setup.entities.BookTable
import com.faire.yawn.setup.entities.PersonTable
import com.faire.yawn.setup.entities.PhoneNumber
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hibernate.criterion.MatchMode
import org.junit.jupiter.api.Test

internal class LikeQueriesTest : BaseYawnDatabaseTest() {
    @Test
    fun `like - books starting with The`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                addLike(books.name, "The %")
            }.list()

            assertThat(results.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "The Emperor's New Clothes",
                "The Little Mermaid",
                "The Ugly Duckling",
            )
        }
    }

    @Test
    fun `like is case-sensitive - books starting with the`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                addLike(books.name, "the %")
            }.list()

            assertThat(results).isEmpty()
        }
    }

    @Test
    fun `iLike is case-insensitive - books starting with the`() {
        transactor.open { session ->
            val results = session.query(BookTable) { books ->
                addILike(books.name, "the %")
            }.list()

            assertThat(results.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "The Emperor's New Clothes",
                "The Little Mermaid",
                "The Ugly Duckling",
            )
        }
    }

    @Test
    fun `not like - authors that do not start with J`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                val authors = join(books.author) // people who have books
                addNotLike(authors.name, "J.%")
                project(authors.name)
            }.set()

            assertThat(results).containsExactlyInAnyOrder(
                "Hans Christian Andersen",
            )
        }
    }

    @Test
    fun `not like is case-sensitive - authors that do not start with j`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                val authors = join(books.author) // people who have books
                addNotLike(authors.name, "j.%")
                project(authors.name)
            }.set()

            assertThat(results).containsExactlyInAnyOrder(
                "Hans Christian Andersen",
                "J.K. Rowling",
                "J.R.R. Tolkien",
            )
        }
    }

    @Test
    fun `not iLike is case-insensitive - authors that do not start with j`() {
        transactor.open { session ->
            val results = session.project(BookTable) { books ->
                val authors = join(books.author) // people who have books
                addNotILike(authors.name, "j.%")
                project(authors.name)
            }.set()

            assertThat(results).containsExactlyInAnyOrder(
                "Hans Christian Andersen",
            )
        }
    }

    @Test
    fun `like on non-nullable column - authors starting with JRR`() {
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
    fun `like on nullable column - notes starting with Note for`() {
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
    fun `like with match mode on non-nullable column - authors starting with JRR`() {
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
    fun `like with match mode END - books ending in Mermaid`() {
        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                addLike(books.name, "Mermaid", MatchMode.END)
            }.list()

            assertThat(books.map { it.name }).containsExactlyInAnyOrder(
                "The Little Mermaid",
            )
        }
    }

    @Test
    fun `like with match mode on nullable column - notes starting with Note for`() {
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
    fun `iLike on non-nullable column - authors starting with JRR`() {
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
    fun `iLike on nullable column - notes starting with Note for`() {
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
    fun `iLike with match mode on non-nullable column - authors starting with JRR`() {
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
    fun `iLike with match mode on nullable column - notes starting with Note for`() {
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

    @Test
    fun `like via restriction - books starting with The`() {
        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                add(YawnRestrictions.like(books.name, "The %"))
            }.list()

            assertThat(books.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "The Emperor's New Clothes",
                "The Little Mermaid",
                "The Ugly Duckling",
            )
        }
    }

    @Test
    fun `iLike via restriction - books starting with the`() {
        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                add(YawnRestrictions.iLike(books.name, "the %"))
            }.list()

            assertThat(books.map { it.name }).containsExactlyInAnyOrder(
                "The Hobbit",
                "The Emperor's New Clothes",
                "The Little Mermaid",
                "The Ugly Duckling",
            )
        }
    }

    /**
     * A value class wrapping a String is unwrapped by its generated adapter, so the whole [MatchMode] range works.
     *
     * Note that [PhoneNumber] validates its own format, so a partial pattern cannot be expressed as a value at all;
     * [MatchMode] is the only way to pattern-match such a column.
     */
    @Test
    fun `like on a value class column - phone numbers starting with a full number`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addLike(people.phone, PhoneNumber("(555) 123-4567"), MatchMode.START)
            }.list()

            assertThat(people.map { it.name }).containsExactlyInAnyOrder("Paul Duchesne")
        }
    }

    @Test
    fun `like on a value class column - match mode ANYWHERE`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addLike(people.phone, PhoneNumber("(333) 000-1111"), MatchMode.ANYWHERE)
            }.list()

            assertThat(people.map { it.name }).containsExactlyInAnyOrder("Quinn Budan")
        }
    }

    @Test
    fun `iLike on a value class column - phone numbers`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addILike(people.phone, PhoneNumber("(555) 987-6543"))
            }.list()

            assertThat(people.map { it.name }).containsExactlyInAnyOrder("Luan Nico")
        }
    }

    @Test
    fun `not like on a value class column - phone numbers other than a full number`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addNotLike(people.phone, PhoneNumber("(555) 123-4567"), MatchMode.START)
                addIsNotNull(people.phone)
            }.list()

            assertThat(people.map { it.name }).containsExactlyInAnyOrder("Luan Nico", "Quinn Budan")
        }
    }

    /**
     * [EmailAddress] is mapped by Hibernate through an `AttributeConverter`, so the value is bound as the column's own
     * type and the wildcards have to be part of the value itself.
     */
    @Test
    fun `like on a converted column - emails on the faire domain`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addLike(people.email, EmailAddress("%@faire.com"))
            }.list()

            assertThat(people.map { it.name }).containsExactlyInAnyOrder(
                "J.R.R. Tolkien",
                "J.K. Rowling",
                "Hans Christian Andersen",
                "Paul Duchesne",
                "Luan Nico",
                "Quinn Budan",
            )
        }
    }

    @Test
    fun `like on a converted column - emails containing a local part`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addLike(people.email, EmailAddress("%duchesne%"))
            }.list()

            assertThat(people.map { it.name }).containsExactlyInAnyOrder("Paul Duchesne")
        }
    }

    @Test
    fun `not like on a converted column - emails not containing a local part`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addNotLike(people.email, EmailAddress("%duchesne%"))
            }.list()

            assertThat(people.map { it.name }).containsExactlyInAnyOrder(
                "J.R.R. Tolkien",
                "J.K. Rowling",
                "Hans Christian Andersen",
                "Luan Nico",
                "Quinn Budan",
            )
        }
    }

    @Test
    fun `like on a converted column rejects a match mode`() {
        transactor.open { session ->
            assertThatThrownBy {
                session.query(PersonTable) { people ->
                    addLike(people.email, EmailAddress("@faire.com"), MatchMode.END)
                }.list()
            }
                .isInstanceOf(UnsupportedOperationException::class.java)
                .hasMessageContaining("MatchMode.END is not supported")
                .hasMessageContaining("like(column.raw")
        }
    }

    /**
     * Dropping the `String` bound means a non-textual column type no longer fails to compile, so the guards have to
     * hold for those too: the value is typed as the column, which leaves no way to express a pattern at all.
     */
    @Test
    fun `like on a non-textual column cannot express a pattern`() {
        transactor.open { session ->
            val matched = session.query(BookTable) { books ->
                addLike(books.numberOfPages, 100L)
            }.list()
            val equal = session.query(BookTable) { books ->
                addEq(books.numberOfPages, 100L)
            }.list()

            assertThat(matched.map { it.name }).isEqualTo(equal.map { it.name })
        }
    }

    @Test
    fun `like with a match mode on a non-textual column is rejected`() {
        transactor.open { session ->
            assertThatThrownBy {
                session.query(BookTable) { books ->
                    addLike(books.numberOfPages, 1L, MatchMode.START)
                }.list()
            }
                .isInstanceOf(UnsupportedOperationException::class.java)
                .hasMessageContaining("MatchMode.START is not supported")
        }
    }

    @Test
    fun `iLike on a non-textual column is rejected`() {
        transactor.open { session ->
            assertThatThrownBy {
                session.query(BookTable) { books ->
                    addILike(books.numberOfPages, 100L)
                }.list()
            }
                .isInstanceOf(UnsupportedOperationException::class.java)
                .hasMessageContaining("iLike is not supported")
        }
    }

    /**
     * `raw` matches the column as text, which is the only way to pattern-match a converted column: the pattern is
     * bound as a String against the underlying column instead of as the property's own type.
     */
    @Test
    fun `raw like on a converted column - match mode END`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addLike(people.email.raw, "@faire.com", MatchMode.END)
            }.list()

            assertThat(people.map { it.name }).containsExactlyInAnyOrder(
                "J.R.R. Tolkien",
                "J.K. Rowling",
                "Hans Christian Andersen",
                "Paul Duchesne",
                "Luan Nico",
                "Quinn Budan",
            )
        }
    }

    /**
     * A partial pattern like this cannot be expressed as an [EmailAddress] value at all, which is what the text view
     * buys over embedding the wildcards in the value.
     */
    @Test
    fun `raw like on a converted column - partial prefix`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addLike(people.email.raw, "luan", MatchMode.START)
            }.list()

            assertThat(people.map { it.name }).containsExactlyInAnyOrder("Luan Nico")
        }
    }

    @Test
    fun `raw iLike on a converted column is case-insensitive`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addILike(people.email.raw, "@FAIRE.COM", MatchMode.END)
            }.list()

            assertThat(people.map { it.name }).hasSize(6)
        }
    }

    @Test
    fun `raw like is case-sensitive`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addLike(people.email.raw, "@FAIRE.COM", MatchMode.END)
            }.list()

            assertThat(people).isEmpty()
        }
    }

    @Test
    fun `raw not like on a converted column`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addNotLike(people.email.raw, "luan", MatchMode.START)
            }.list()

            assertThat(people.map { it.name }).containsExactlyInAnyOrder(
                "J.R.R. Tolkien",
                "J.K. Rowling",
                "Hans Christian Andersen",
                "Paul Duchesne",
                "Quinn Budan",
            )
        }
    }

    @Test
    fun `raw like on a value class column - partial prefix`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addLike(people.phone.raw, "(555)", MatchMode.START)
            }.list()

            assertThat(people.map { it.name }).containsExactlyInAnyOrder("Paul Duchesne", "Luan Nico")
        }
    }

    @Test
    fun `raw like resolves a joined alias`() {
        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                val authors = join(books.author)
                addLike(authors.email.raw, "tolkien", MatchMode.START)
            }.list()

            assertThat(books.map { it.name }).containsExactlyInAnyOrder("The Hobbit", "Lord of the Rings")
        }
    }

    @Test
    fun `raw not iLike on a converted column`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addNotILike(people.email.raw, "LUAN", MatchMode.START)
            }.list()

            assertThat(people.map { it.name }).containsExactlyInAnyOrder(
                "J.R.R. Tolkien",
                "J.K. Rowling",
                "Hans Christian Andersen",
                "Paul Duchesne",
                "Quinn Budan",
            )
        }
    }

    /**
     * A single `or` mixing a plain column with a converted one, which previously forced callers to either split the
     * query in two or fall back to filtering in Kotlin.
     */
    @Test
    fun `like on a converted column combined with other restrictions in a single or`() {
        transactor.open { session ->
            val books = session.query(BookTable) { books ->
                val authors = join(books.author)
                add(
                    YawnRestrictions.or(
                        YawnRestrictions.`in`(books.name, listOf("Harry Potter")),
                        YawnRestrictions.like(authors.email, EmailAddress("%tolkien%")),
                    ),
                )
            }.list()

            assertThat(books.map { it.name }).containsExactlyInAnyOrder(
                "Harry Potter",
                "The Hobbit",
                "Lord of the Rings",
            )
        }
    }

    @Test
    fun `iLike on a converted column is rejected`() {
        transactor.open { session ->
            assertThatThrownBy {
                session.query(PersonTable) { people ->
                    addILike(people.email, EmailAddress("%@FAIRE.COM"))
                }.list()
            }
                .isInstanceOf(UnsupportedOperationException::class.java)
                .hasMessageContaining("iLike is not supported")
                .hasMessageContaining("iLike(column.raw")
        }
    }
}
