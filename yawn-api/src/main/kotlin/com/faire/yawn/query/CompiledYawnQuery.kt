package com.faire.yawn.query

/**
 * A compiled query that can be executed to retrieve results.
 */
interface CompiledYawnQuery<T> {
  fun list(): List<T>
  fun uniqueResult(): T?
}
