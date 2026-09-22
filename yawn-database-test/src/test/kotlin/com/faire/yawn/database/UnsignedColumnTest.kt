package com.faire.yawn.database

import com.faire.yawn.setup.entities.PersonTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * End-to-end tests for columns typed as an unsigned Kotlin type ([ULong]) going through Hibernate.
 * See [com.faire.yawn.setup.entities.Person.externalId].
 */
internal class UnsignedColumnTest : BaseYawnDatabaseTest() {
    @Test
    fun `can fetch and read back a value above the signed range`() {
        transactor.open { session ->
            val person = session.query(PersonTable) { people ->
                addEq(people.name, "Luan Nico")
            }.uniqueResult()!!
            assertThat(person.externalId).isEqualTo(ULong.MAX_VALUE)
        }
    }

    @Test
    fun `query with addEq`() {
        transactor.open { session ->
            val person = session.query(PersonTable) { people ->
                addEq(people.externalId, 2_002uL)
            }.uniqueResult()!!
            assertThat(person.name).isEqualTo("Quinn Budan")
        }
    }

    @Test
    fun `query with addEq above the signed range`() {
        transactor.open { session ->
            val person = session.query(PersonTable) { people ->
                addEq(people.externalId, ULong.MAX_VALUE)
            }.uniqueResult()!!
            assertThat(person.name).isEqualTo("Luan Nico")
        }
    }

    @Test
    fun `query with addNotEq`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addNotEq(people.externalId, 0uL)
            }.list()
            assertThat(people.map { it.name }).containsExactlyInAnyOrder("Paul Duchesne", "Luan Nico", "Quinn Budan")
        }
    }

    @Test
    fun `query with addIn`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addIn(people.externalId, listOf(1_001uL, ULong.MAX_VALUE))
            }.list()
            assertThat(people.map { it.name }).containsExactlyInAnyOrder("Paul Duchesne", "Luan Nico")
        }
    }

    @Test
    fun `query with addBetween`() {
        transactor.open { session ->
            val people = session.query(PersonTable) { people ->
                addBetween(people.externalId, 1_000uL, 3_000uL)
            }.list()
            assertThat(people.map { it.name }).containsExactlyInAnyOrder("Paul Duchesne", "Quinn Budan")
        }
    }

    /**
     * The column is a signed BIGINT, so SQL compares the signed representation:
     * [ULong.MAX_VALUE] is stored as -1 and sorts *below* every small positive value.
     */
    @Test
    fun `ordering comparisons use the signed representation`() {
        transactor.open { session ->
            val greaterThanThousand = session.query(PersonTable) { people ->
                addGt(people.externalId, 1_000uL)
            }.list()
            assertThat(greaterThanThousand.map { it.name }).containsExactlyInAnyOrder("Paul Duchesne", "Quinn Budan")

            val lessThanZero = session.query(PersonTable) { people ->
                addLt(people.externalId, 0uL)
            }.list()
            assertThat(lessThanZero.map { it.name }).containsExactly("Luan Nico")
        }
    }
}
