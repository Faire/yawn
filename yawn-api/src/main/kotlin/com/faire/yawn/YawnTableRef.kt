package com.faire.yawn

/**
 * The user-facing class representing a table.
 * It allows Yawn to create table definitions [DEF] representing the given entity [T].
 */
interface YawnTableRef<T : Any, DEF : YawnTableDef<T, T>> : YawnRef<T, DEF> {
    fun create(parent: YawnTableDefParent): DEF

    fun <PARENT_SOURCE : Any> forSubQuery(): YawnTableDef<PARENT_SOURCE, T>
}
