package com.faire.yawn.database

import com.faire.yawn.setup.entities.PersonTable
import com.faire.yawn.setup.entities.PhoneNumber
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ValueClassTest : BaseYawnDatabaseTest() {
    @Test
    fun `can fetch and unwrap`() {
        transactor.open { session ->
            val person = session.query(PersonTable) { people ->
                addEq(people.name, "Luan Nico")
            }.uniqueResult()!!
            with(person.phone!!) {
                assertThat(this).isEqualTo(PhoneNumber("(555) 987-6543"))
                assertThat(areaCode).isEqualTo("555")
                assertThat(centralOfficeCode).isEqualTo("987")
                assertThat(lineNumber).isEqualTo("6543")
            }
        }
    }

    @Test
    fun `query with eq`() {
        transactor.open { session ->
            val person = session.query(PersonTable) { people ->
                addEq(people.phone, PhoneNumber("(333) 000-1111"))
            }.uniqueResult()!!
            assertThat(person.name).isEqualTo("Quinn Budan")
        }
    }

    @Test
    fun `query with addNotEq`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addNotEq(people.phone, PhoneNumber("(555) 987-6543"))
                addIsNotNull(people.phone)
            }.list()
            assertThat(people.map { it.name }).containsExactlyInAnyOrder("Paul Duchesne", "Quinn Budan")
        }
    }

    @Test
    fun `query with addGt`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addGt(people.phone, PhoneNumber("(555) 000-0000"))
            }.list()
            assertThat(people.map { it.name }).containsExactlyInAnyOrder("Luan Nico", "Paul Duchesne")
        }
    }

    @Test
    fun `query with addGe`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addGe(people.phone, PhoneNumber("(555) 987-6543"))
            }.list()
            assertThat(people.single().name).isEqualTo("Luan Nico")
        }
    }

    @Test
    fun `query with addLt`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addLt(people.phone, PhoneNumber("(555) 123-4567"))
                addIsNotNull(people.phone)
            }.list()
            assertThat(people.single().name).isEqualTo("Quinn Budan")
        }
    }

    @Test
    fun `query with addLe`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addLe(people.phone, PhoneNumber("(555) 123-4567"))
                addIsNotNull(people.phone)
            }.list()
            assertThat(people.map { it.name }).containsExactlyInAnyOrder("Paul Duchesne", "Quinn Budan")
        }
    }

    @Test
    fun `query with addBetween`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addBetween(people.phone, PhoneNumber("(555) 100-0000"), PhoneNumber("(555) 900-0000"))
            }.list()
            assertThat(people.single().name).isEqualTo("Paul Duchesne")
        }
    }

    @Test
    fun `query with addEqOrIsNull`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addEqOrIsNull(people.phone, PhoneNumber("(555) 987-6543"))
            }.list()
            assertThat(people.map { it.name }).contains("Luan Nico")
        }
    }

    @Test
    fun `query with addIn`() {
        transactor.open { session ->
            val phoneNumbers = listOf(
                PhoneNumber("(555) 123-4567"),
                PhoneNumber("(555) 987-6543"),
            )
            val people = session.query(PersonTable) { people ->
                addIn(people.phone, phoneNumbers)
            }.list()
            assertThat(people.map { it.name }).containsExactlyInAnyOrder("Luan Nico", "Paul Duchesne")
        }
    }

    @Test
    fun `query with addNotIn`() {
        transactor.open { session ->
            val phoneNumbers = listOf(PhoneNumber("(555) 987-6543"), PhoneNumber("(555) 123-4567"))
            val people = session.query(PersonTable) { people ->
                addNotIn(people.phone, phoneNumbers)
                addIsNotNull(people.phone)
            }.list()
            assertThat(people.single().name).isEqualTo("Quinn Budan")
        }
    }
}
