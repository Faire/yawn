package com.faire.yawn.setup.custom

import com.google.gson.Gson
import com.google.inject.TypeLiteral
import org.hibernate.HibernateException
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.usertype.UserType
import java.io.Serializable
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types.VARCHAR

/** Marshals Objects as JSON string.  */
internal class JsonMessageType(
    private val gson: Gson,
    private val columnTypeLiteral: TypeLiteral<*>,
) : UserType {
    override fun sqlTypes(): IntArray {
        return SQL_TYPES
    }

    override fun returnedClass(): Class<*> {
        return columnTypeLiteral.rawType
    }

    @Throws(HibernateException::class)
    override fun equals(x: Any?, y: Any?): Boolean {
        val xJson = gson.toJson(x, columnTypeLiteral.type)
        val yJson = gson.toJson(y, columnTypeLiteral.type)
        return if (xJson == null) yJson == null else yJson != null && xJson == yJson
    }

    @Throws(HibernateException::class)
    override fun hashCode(value: Any): Int {
        val json = gson.toJson(value, columnTypeLiteral.type)
        return json.hashCode()
    }

    @Throws(HibernateException::class, SQLException::class)
    override fun nullSafeGet(
        rs: ResultSet,
        names: Array<String>,
        session: SharedSessionContractImplementor,
        owner: Any?,
    ): Any? {
        val jsonData = rs.getString(names[0])
        return if (rs.wasNull() || jsonData == null) {
            null
        } else {
            gson.fromJson<Any>(jsonData, columnTypeLiteral.type)
        }
    }

    @Throws(HibernateException::class, SQLException::class)
    override fun nullSafeSet(
        st: PreparedStatement,
        value: Any?,
        index: Int,
        session: SharedSessionContractImplementor,
    ) {
        if (value == null) {
            st.setNull(index, VARCHAR)
            return
        }

        st.setString(index, gson.toJson(value, columnTypeLiteral.type))
    }

    @Throws(HibernateException::class)
    override fun deepCopy(value: Any?): Any? {
        val json = gson.toJson(value, columnTypeLiteral.type)
        return gson.fromJson<Any>(json, columnTypeLiteral.type)
    }

    override fun isMutable(): Boolean {
        return true
    }

    @Throws(HibernateException::class)
    override fun disassemble(value: Any?): Serializable? {
        return if (value == null) null else deepCopy(value) as Serializable?
    }

    @Throws(HibernateException::class)
    override fun assemble(cached: Serializable, owner: Any): Any? {
        return gson.fromJson<Any>(cached as String, columnTypeLiteral.type)
    }

    @Throws(HibernateException::class)
    override fun replace(original: Any, target: Any, owner: Any): Any? {
        return deepCopy(original)
    }

    companion object {
        private val SQL_TYPES = intArrayOf(VARCHAR)
    }
}
