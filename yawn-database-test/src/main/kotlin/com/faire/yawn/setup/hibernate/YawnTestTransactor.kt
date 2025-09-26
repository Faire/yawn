package com.faire.yawn.setup.hibernate

import com.faire.yawn.setup.custom.CustomHibernateConfigurer
import com.faire.yawn.setup.entities.BookFixtures

internal class YawnTestTransactor {
    private val sessionFactory by lazy {
        CustomHibernateConfigurer.createSessionFactory(
            entities = BookFixtures.entities,
        )
    }

    fun <T> open(lambda: (YawnTestSession) -> T): T {
        val session = sessionFactory.openSession()
        return session.use { hibernateSession ->
            val transaction = hibernateSession.beginTransaction()
            try {
                val result = lambda(YawnTestSession(hibernateSession))
                transaction.commit()
                result
            } catch (t: Throwable) {
                transaction.rollback()
                throw t
            }
        }
    }
}
