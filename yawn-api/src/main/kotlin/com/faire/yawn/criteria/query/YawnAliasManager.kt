package com.faire.yawn.criteria.query

import com.faire.yawn.YawnTableDefParent
import com.faire.yawn.query.YawnCompilationContext
import java.util.concurrent.ConcurrentHashMap

// table names don't change, thus we can use a static in-memory cache of these prefixes
private val tableNamePrefixMap = ConcurrentHashMap<String, String>()

/**
 * Generates aliases via [generate] that utilize the name of the path and maintains uniqueness within the lifetime of
 * the instance.
 *
 * In addition to maintaining uniqueness, it maintains a cache of parents to aliases so that repeated calls with the
 * same parent return the same alias.
 *
 * For example:
 * - `customer` -> `c`
 * - `brandOwner` -> `bo`
 * - `brandOwner` (repeated) -> `bo2`
 * - `customer` (repeated) -> `c2`
 */
internal class YawnAliasManager {
  private val aliasCounters = mutableMapOf<String, Int>()
  private val utilizedAliases = mutableSetOf<String>()
  private val aliasesByParent = mutableMapOf<YawnTableDefParent, String?>()

  fun generate(parent: YawnTableDefParent, context: YawnCompilationContext): String? {
    return aliasesByParent.getOrPut(parent) {
      parent.getAliasBaseString(context)?.let { generate(it) }
    }
  }

  internal fun generate(path: String): String {
    val prefix = computePrefix(path)

    var counter = aliasCounters.getOrPut(prefix) { 2 }
    var alias = prefix
    while (!utilizedAliases.add(alias)) {
      alias = "${prefix}${counter++}"
    }

    aliasCounters[prefix] = counter

    return alias
  }

  internal fun computePrefix(path: String): String {
    val tableName = path.split('.').last()

    return tableNamePrefixMap.computeIfAbsent(tableName) { _ ->
      (
          sequenceOf(tableName.first()) + tableName.asSequence()
          .drop(1)
          .filter { it.isUpperCase() }
          .map { it.lowercaseChar() }
      )
          .joinToString("")
    }
  }
}
