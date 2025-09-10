package com.faire.yawn

import com.faire.yawn.YawnTestUtils.YawnTestAssertContext.SOURCE
import com.faire.yawn.YawnTestUtils.assertGeneratedEntity
import com.faire.yawn.anotherPackage.NonYawnEntityInAnotherPackage
import com.faire.yawn.anotherPackage.YawnEntityInAnotherPackage
import com.faire.yawn.anotherPackage.YawnEntityInAnotherPackageTableDef
import org.junit.jupiter.api.Test

internal class YawnEntityProcessorJoinsTest {
  @Test
  fun `can generate column definition for one-to-one and many-to-one relations`() {
    assertGeneratedEntity<EntityWithSimpleRelations> {
      // one-to-one
      hasField<YawnTableDef<SOURCE, EntityWithSimpleRelations>.ColumnDef<NonYawnEntityInAnotherPackage?>>("oneToOneNonYawn")
      hasField<YawnTableDef<SOURCE, EntityWithSimpleRelations>.JoinColumnDef<YawnEntityInAnotherPackage, YawnEntityInAnotherPackageTableDef<SOURCE>>>(
          "nonNullOneToOneYawn",
      )
      // NOTE: we are erasing the nullability information here since we leverage a naive join implementation
      hasField<YawnTableDef<SOURCE, EntityWithSimpleRelations>.JoinColumnDef<YawnEntityInAnotherPackage, YawnEntityInAnotherPackageTableDef<SOURCE>>>(
          "nullableOneToOneYawn",
      )
      hasField<YawnTableDef<SOURCE, EntityWithSimpleRelations>.JoinColumnDef<YawnEntityInAnotherPackage, YawnEntityInAnotherPackageTableDef<SOURCE>>>(
          "oneToOneYawnWithTargetEntity",
      )

      // many-to-one
      hasField<YawnTableDef<SOURCE, EntityWithSimpleRelations>.JoinColumnDef<YawnEntityInAnotherPackage, YawnEntityInAnotherPackageTableDef<SOURCE>>>(
          "manyToOneYawn",
      )
      hasField<YawnTableDef<SOURCE, EntityWithSimpleRelations>.JoinColumnDef<YawnEntityInAnotherPackage, YawnEntityInAnotherPackageTableDef<SOURCE>>>(
          "manyToOneYawnWithTargetEntity",
      )
      hasField<YawnTableDef<SOURCE, EntityWithSimpleRelations>.ColumnDef<NonYawnEntityInAnotherPackage>>("manyToOneNonYawn")
    }
  }

  @Test
  fun `can generate column definition for one-to-many and many-to-many relations`() {
    assertGeneratedEntity<EntityWithXtoManyRelations> {
      hasField<YawnTableDef<SOURCE, EntityWithXtoManyRelations>.CollectionJoinColumnDef<YawnEntityInAnotherPackage, YawnEntityInAnotherPackageTableDef<SOURCE>>>(
          "oneToManyYawn",
      )
      hasField<YawnTableDef<SOURCE, EntityWithXtoManyRelations>.CollectionJoinColumnDef<YawnEntityInAnotherPackage, YawnEntityInAnotherPackageTableDef<SOURCE>>>(
          "manyToManyYawn",
      )

      // don't generate for non yawn entity
      hasNoField("oneToManyNonYawn")
      hasNoField("manyToManyNonYawn")
    }
  }
}
