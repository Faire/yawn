package com.faire.yawn.database

import com.faire.yawn.setup.entities.BookFixtures
import com.faire.yawn.setup.hibernate.YawnTestTransactor
import org.junit.jupiter.api.BeforeEach

internal open class BaseYawnDatabaseTest {
    protected lateinit var transactor: YawnTestTransactor

    @BeforeEach
    fun setup() {
        transactor = YawnTestTransactor()
        BookFixtures(transactor).setup()
    }
}
