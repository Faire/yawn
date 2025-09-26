package com.faire.yawn.setup.hibernate

import com.faire.yawn.setup.entities.BaseEntity
import com.faire.yawn.setup.entities.YawnId
import org.hibernate.HibernateException
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.ResultSetIdentifierConsumer
import org.hibernate.usertype.UserType
import java.io.Serializable
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

internal class YawnIdType : UserType, ResultSetIdentifierConsumer {
    override fun sqlTypes(): IntArray {
        return TYPE
    }

    override fun returnedClass(): Class<*> {
        return YawnId::class.java
    }

    @Throws(HibernateException::class)
    override fun equals(x: Any?, y: Any?): Boolean {
        return x == y
    }

    @Throws(HibernateException::class)
    override fun hashCode(x: Any): Int {
        return x.hashCode()
    }

    @Throws(HibernateException::class, SQLException::class)
    override fun nullSafeGet(
        resultSet: ResultSet,
        names: Array<String>,
        session: SharedSessionContractImplementor,
        owner: Any?,
    ): Any? {
        val value = resultSet.getLong(names[0])
        return if (resultSet.wasNull()) null else YawnId<BaseEntity<*>>(value)
    }

    @Throws(HibernateException::class, SQLException::class)
    override fun nullSafeSet(
        statement: PreparedStatement,
        value: Any?,
        index: Int,
        session: SharedSessionContractImplementor,
    ) {
        if (value != null) {
            statement.setLong(index, (value as YawnId<*>).id)
        } else {
            statement.setNull(index, sqlTypes()[0])
        }
    }

    @Throws(HibernateException::class)
    override fun deepCopy(value: Any?): Any? {
        return value
    }

    override fun isMutable(): Boolean {
        return false
    }

    @Throws(HibernateException::class)
    override fun disassemble(value: Any): Serializable {
        return value as Serializable
    }

    @Throws(HibernateException::class)
    override fun assemble(cached: Serializable, owner: Any): Any {
        return cached
    }

    @Throws(HibernateException::class)
    override fun replace(original: Any, target: Any, owner: Any): Any {
        return original
    }

    override fun consumeIdentifier(resultSet: ResultSet): Serializable {
        return YawnId<BaseEntity<*>>(resultSet.getLong(1))
    }

    companion object {
        private val TYPE = intArrayOf(Types.BIGINT)
    }
}
