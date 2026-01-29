package com.faire.yawn.database

import com.faire.yawn.YawnTableDef
import com.faire.yawn.criteria.builder.TypeSafeCriteriaBuilder
import com.faire.yawn.query.YawnLockMode
import com.faire.yawn.setup.entities.BaseEntity
import com.faire.yawn.setup.entities.BookTable
import com.faire.yawn.setup.hibernate.YawnTestCompiledQuery
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.LockMode
import org.hibernate.internal.CriteriaImpl
import org.junit.jupiter.api.Test

internal class YawnLockModeTest : BaseYawnDatabaseTest() {
    @Test
    fun `forUpdate sets PESSIMISTIC_WRITE lock mode`() {
        transactor.open { session ->
            val compiledQuery = getCompiledQuery {
                session.query(BookTable) { books ->
                    addEq(books.name, "The Hobbit")
                }.forUpdate()
            }

            assertThat(compiledQuery.hibernateCriteriaImpl.lockModes.values).containsOnly(LockMode.PESSIMISTIC_WRITE)
            assertThat(compiledQuery.list().single().name).isEqualTo("The Hobbit")
        }
    }

    @Test
    fun `forShare sets PESSIMISTIC_READ lock mode`() {
        transactor.open { session ->
            val compiledQuery = getCompiledQuery {
                session.query(BookTable) { books ->
                    addEq(books.name, "The Hobbit")
                }.forShare()
            }

            assertThat(compiledQuery.hibernateCriteriaImpl.lockModes.values).containsOnly(LockMode.PESSIMISTIC_READ)
            assertThat(compiledQuery.list().single().name).isEqualTo("The Hobbit")
        }
    }

    @Test
    fun `setLockMode with PESSIMISTIC_WRITE`() {
        transactor.open { session ->
            val compiledQuery = getCompiledQuery {
                session.query(BookTable) { books ->
                    addEq(books.name, "The Hobbit")
                }.setLockMode(YawnLockMode.PESSIMISTIC_WRITE)
            }

            assertThat(compiledQuery.hibernateCriteriaImpl.lockModes.values).containsOnly(LockMode.PESSIMISTIC_WRITE)
            assertThat(compiledQuery.list().single().name).isEqualTo("The Hobbit")
        }
    }

    @Test
    fun `setLockMode with PESSIMISTIC_READ`() {
        transactor.open { session ->
            val compiledQuery = getCompiledQuery {
                session.query(BookTable) { books ->
                    addEq(books.name, "The Hobbit")
                }.setLockMode(YawnLockMode.PESSIMISTIC_READ)
            }

            assertThat(compiledQuery.hibernateCriteriaImpl.lockModes.values).containsOnly(LockMode.PESSIMISTIC_READ)
            assertThat(compiledQuery.list().single().name).isEqualTo("The Hobbit")
        }
    }

    @Test
    fun `setLockMode with NONE explicitly set`() {
        transactor.open { session ->
            val compiledQuery = getCompiledQuery {
                session.query(BookTable) { books ->
                    addEq(books.name, "The Hobbit")
                }.setLockMode(YawnLockMode.NONE)
            }

            assertThat(compiledQuery.hibernateCriteriaImpl.lockModes.values).containsOnly(LockMode.NONE)
            assertThat(compiledQuery.list().single().name).isEqualTo("The Hobbit")
        }
    }

    @Test
    fun `forUpdate can be combined with maxResults and offset`() {
        transactor.open { session ->
            val compiledQuery = getCompiledQuery {
                session.query(BookTable) { books ->
                    orderAsc(books.name)
                }
                    .forUpdate()
                    .maxResults(2)
                    .offset(1)
            }

            assertThat(compiledQuery.hibernateCriteriaImpl.lockModes.values).containsOnly(LockMode.PESSIMISTIC_WRITE)
            assertThat(compiledQuery.list().map { it.name })
                .containsExactly("Lord of the Rings", "The Emperor's New Clothes")
        }
    }

    private fun <T : BaseEntity<T>, DEF : YawnTableDef<T, T>> getCompiledQuery(
        queryProvider: () -> TypeSafeCriteriaBuilder<T, DEF>,
    ): YawnTestCompiledQuery<T> {
        return queryProvider().compile() as YawnTestCompiledQuery<T>
    }
}

private val YawnTestCompiledQuery<*>.hibernateCriteriaImpl: CriteriaImpl
    get() = rawQuery as CriteriaImpl
