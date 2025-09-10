package com.faire.yawn.query

import com.faire.yawn.criteria.query.YawnAliasManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class YawnAliasManagerTest {
  val aliasManager = YawnAliasManager()

  @Test
  fun `verify correct prefix for table`() {
    val tablePrefix = aliasManager.computePrefix("table")
    assertThat(tablePrefix).isEqualTo("t")
  }

  @Test
  fun `verify correct prefix for camelCase table`() {
    val tablePrefix = aliasManager.computePrefix("camelCaseTable")
    assertThat(tablePrefix).isEqualTo("cct")
  }

  @Test
  fun `verify correct prefix for association path`() {
    val tablePrefix = aliasManager.computePrefix("book.author")
    assertThat(tablePrefix).isEqualTo("a")
  }

  @Test
  fun `verify alias generation for repeated paths`() {
    val alias1 = aliasManager.generate("table")
    val alias2 = aliasManager.generate("table")
    assertThat(alias1).isEqualTo("t")
    assertThat(alias2).isEqualTo("t2")
  }

  @Test
  fun `nested paths use the last part to generate a prefix`() {
    val barAlias = aliasManager.generate("foo.clyde.bar")
    val bazAlias = aliasManager.generate("foo.clyde.baz")
    assertThat(barAlias).isEqualTo("b")
    assertThat(bazAlias).isEqualTo("b2")
  }

  @Test
  fun `verify alias generation for different paths does not use numbers until necessary`() {
    val appleAlias = aliasManager.generate("apples")
    val bananaAlias = aliasManager.generate("bananas")
    val cherryAlias = aliasManager.generate("cherries")

    val appleAlias2 = aliasManager.generate("apples")
    val bananaAlias2 = aliasManager.generate("bananas")
    val cherryAlias2 = aliasManager.generate("cherries")

    assertThat(appleAlias).isEqualTo("a")
    assertThat(appleAlias2).isEqualTo("a2")
    assertThat(bananaAlias).isEqualTo("b")
    assertThat(bananaAlias2).isEqualTo("b2")
    assertThat(cherryAlias).isEqualTo("c")
    assertThat(cherryAlias2).isEqualTo("c2")
  }
}
