package com.faire.yawn

import com.faire.yawn.YawnTestUtils.assertGeneratedProjection
import com.faire.yawn.project.YawnProjection
import org.junit.jupiter.api.Test

internal class YawnProjectionProcessorTest {
  @YawnProjection
  data class SimpleProjection(
      val string: String,
      val int: Int,
      val boolean: Boolean,
  )

  @Test
  fun `simple projection is generated`() {
    assertGeneratedProjection<SimpleProjection> {
      hasProjectionColumn<SimpleProjection, String>("string")
      hasProjectionColumn<SimpleProjection, Int>("int")
      hasProjectionColumn<SimpleProjection, Boolean>("boolean")

      hasCompanionObjectWithCreateFunction()
    }
  }
}
