package com.faire.yawn.setup.hibernate

import org.hibernate.resource.jdbc.spi.StatementInspector

/**
 * Records the SQL Hibernate prepares while [capture] runs, so tests can assert on the shape of generated queries.
 */
internal object CapturingStatementInspector : StatementInspector {
    private val captured = ThreadLocal<MutableList<String>?>()

    override fun inspect(sql: String): String {
        captured.get()?.add(sql)
        return sql
    }

    fun capture(block: () -> Unit): List<String> {
        val statements = mutableListOf<String>()
        captured.set(statements)
        try {
            block()
        } finally {
            captured.remove()
        }
        return statements
    }
}
