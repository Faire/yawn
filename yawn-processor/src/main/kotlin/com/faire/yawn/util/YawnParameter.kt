package com.faire.yawn.util

/**
 * This is an abstraction to simplify passing parameters as a list of arguments to Kotlin Poet.
 * See [com.faire.yawn.generators.property.YawnPropertyGenerator.generatePropertySpec] for more details.
 */
internal data class YawnParameter(
    val format: String, // %N, %S, %T, etc., or combinations thereof
    val arguments: List<Any> = listOf(),
) {
  companion object {
    fun simple(format: String, value: Any): YawnParameter {
      return YawnParameter(format, listOf(value))
    }

    fun literal(value: String): YawnParameter {
      return simple("%N", value)
    }

    fun string(value: String): YawnParameter {
      return simple("%S", value)
    }
  }
}
