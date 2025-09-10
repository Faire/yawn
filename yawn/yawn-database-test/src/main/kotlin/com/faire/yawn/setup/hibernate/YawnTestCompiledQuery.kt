package com.faire.yawn.setup.hibernate

import com.faire.yawn.query.CompiledYawnQuery
import org.hibernate.Criteria

internal class YawnTestCompiledQuery<T>(
    private val rawQuery: Criteria,
) : CompiledYawnQuery<T> {
  override fun list(): List<T> {
    @Suppress("UNCHECKED_CAST")
    return rawQuery.list() as List<T>
  }

  override fun uniqueResult(): T? {
    @Suppress("UNCHECKED_CAST")
    return rawQuery.uniqueResult() as T?
  }
}
