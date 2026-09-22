package com.faire.yawn.database

import com.faire.yawn.query.YawnRestrictions
import com.faire.yawn.setup.custom.EmailAddress
import com.faire.yawn.setup.entities.BookTable
import com.faire.yawn.setup.entities.PersonTable
import com.faire.yawn.setup.entities.PhoneNumber
import org.assertj.core.api.Assertions.assertThat
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
     * [PhoneNumber] validates its own format, so a partial pattern cannot be expressed as a value at all and
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
     * [EmailAddress] is mapped by Hibernate through an `AttributeConverter`; wildcards can be written into the value
     * itself and matched with the default [MatchMode.EXACT].
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
    fun `like on a converted column with a match mode`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addLike(people.email, EmailAddress("@faire.com"), MatchMode.END)
            }.list()

            assertThat(people.map { it.name }).hasSize(6)
        }
    }

    @Test
    fun `like on a converted column with a partial pattern`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addLike(people.email, EmailAddress("luan"), MatchMode.START)
            }.list()

            assertThat(people.map { it.name }).containsExactlyInAnyOrder("Luan Nico")
        }
    }

    @Test
    fun `iLike on a converted column is case-insensitive`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addILike(people.email, EmailAddress("@FAIRE.COM"), MatchMode.END)
            }.list()

            assertThat(people.map { it.name }).hasSize(6)
        }
    }

    @Test
    fun `like on a converted column stays case-sensitive`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addLike(people.email, EmailAddress("@FAIRE.COM"), MatchMode.END)
            }.list()

            assertThat(people).isEmpty()
        }
    }

    @Test
    fun `not iLike on a converted column`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addNotILike(people.email, EmailAddress("LUAN"), MatchMode.START)
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
}
