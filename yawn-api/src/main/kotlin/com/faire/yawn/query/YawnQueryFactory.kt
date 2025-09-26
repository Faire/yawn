package com.faire.yawn.query

import com.faire.yawn.YawnTableDef

/**
 * A factory to abstract Yawn over an underlying ORM of choice.
 */
interface YawnQueryFactory {
    fun <T : Any> compile(query: YawnQuery<*, T>, tableDef: YawnTableDef<*, *>): CompiledYawnQuery<T>
}
