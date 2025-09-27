package com.faire.yawn

import com.faire.yawn.YawnTableDefParent.AssociationTableDefParent
import com.faire.yawn.YawnTableDefParent.RootTableDefParent
import com.faire.yawn.query.YawnCompilationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class YawnEntityPathTest {
    val context = YawnCompilationContext(withSubQuery = false)

    @Test
    fun `can generate path with an un-aliased root`() {
        val noParentAlias = EntityWithoutRelationsTable.create(parent = RootTableDefParent)
        assertThat(noParentAlias.token.generatePath(context)).isEqualTo("token")
    }

    @Test
    fun `can generate path with an aliased root for subqueries`() {
        val subQueryCompilationContext = YawnCompilationContext(withSubQuery = true)

        val rootAlias = EntityWithoutRelationsTable.create(parent = RootTableDefParent)
        assertThat(rootAlias.token.generatePath(subQueryCompilationContext)).isEqualTo("r.token")
    }

    @Test
    fun `join column def path contains whole chain`() {
        val withParent = EntityWithSimpleRelationsTableDef<EntityWithoutRelations>(parent = RootTableDefParent)
        val asField = withParent.nonNullOneToOneYawn
        assertThat(asField.path(context)).isEqualTo("nonNullOneToOneYawn")

        val asJoinWithAlias = withParent.nonNullOneToOneYawn.joinTableDef(AssociationTableDefParent(asField))
        assertThat(asJoinWithAlias.randomField.generatePath(context)).isEqualTo("nnotoy.randomField")
    }

    @Test
    fun `join column def path contains whole chain including root aliases`() {
        val subQueryCompilationContext = YawnCompilationContext(withSubQuery = true)
        val withParent = EntityWithSimpleRelationsTableDef<EntityWithoutRelations>(parent = RootTableDefParent)
        val asField = withParent.nonNullOneToOneYawn
        assertThat(asField.path(subQueryCompilationContext)).isEqualTo("r.nonNullOneToOneYawn")

        val asJoinWithAlias = withParent.nonNullOneToOneYawn.joinTableDef(AssociationTableDefParent(asField))
        // cSpell:ignore nnotoy - this is the alias given with the initials of "nonUllOneToOneYawn"
        assertThat(asJoinWithAlias.randomField.generatePath(subQueryCompilationContext)).isEqualTo("nnotoy.randomField")
    }
}
