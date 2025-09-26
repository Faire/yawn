package com.faire.yawn

import com.faire.yawn.YawnTestUtils.YawnTestAssertContext.SOURCE
import com.faire.yawn.YawnTestUtils.assertGeneratedEntity
import com.faire.yawn.inheritance.ChildInheritanceEntity
import com.faire.yawn.utils.FakeToken
import org.junit.jupiter.api.Test
import javax.persistence.Transient
import kotlin.reflect.KVisibility

internal class YawnEntityProcessorTest {
    @Test
    fun `generate a tableDef that inherit from a generic YawnTableDef abstract class`() {
        assertGeneratedEntity<PublicEmptyEntity>()
    }

    @YawnEntity
    class PublicInInternal {
        @YawnEntity
        class PublicInPublicInInternal

        @YawnEntity
        internal class InternalInPublicInInternal
    }

    @YawnEntity
    internal class InternalInInternal
    // NOTE: private classes are not supported by Yawn

    @Test
    fun `match visibility of the original entity`() {
        assertGeneratedEntity<PublicEmptyEntity>(expectedVisibility = KVisibility.PUBLIC)
        assertGeneratedEntity<InternalEmptyEntity>(expectedVisibility = KVisibility.INTERNAL)

        // NOTE: local classes are not supported by Yawn
        assertGeneratedEntity<PublicInInternal>(expectedVisibility = KVisibility.INTERNAL)
        assertGeneratedEntity<InternalInInternal>(expectedVisibility = KVisibility.INTERNAL)
        assertGeneratedEntity<PublicInInternal.PublicInPublicInInternal>(expectedVisibility = KVisibility.INTERNAL)
        assertGeneratedEntity<PublicInInternal.InternalInPublicInInternal>(expectedVisibility = KVisibility.INTERNAL)
        assertGeneratedEntity<PublicEmptyEntity.InternalInsidePublic>(expectedVisibility = KVisibility.INTERNAL)
    }

    @YawnEntity
    class DbParent {
        @YawnEntity
        class DbChildInDbParent
    }

    @Test
    fun `nested class naming`() {
        assertGeneratedEntity<DbParent>()
        assertGeneratedEntity<DbParent.DbChildInDbParent>()
    }

    @YawnEntity
    class DbColumnLess {
        @Transient
        val transientField: String = ""
    }

    @Test
    fun `ignore transient fields`() {
        assertGeneratedEntity<DbColumnLess> {
            hasNoField("transientField")
        }
    }

    @Test
    fun `can generate column definition for non-relations`() {
        assertGeneratedEntity<EntityWithoutRelations> {
            hasTableColumn<EntityWithoutRelations, Long>("id")
            hasTableColumn<EntityWithoutRelations, Long>("version")
            hasTableColumn<EntityWithoutRelations, String>("name")
            hasTableColumn<EntityWithoutRelations, FakeToken<String>>("token")
        }
    }

    @Test
    fun `can generate column definition for inheritance`() {
        assertGeneratedEntity<ChildInheritanceEntity> {
            // self values
            hasTableColumn<ChildInheritanceEntity, String>("childValue")

            // overridden interface
            hasTableColumn<ChildInheritanceEntity, FakeToken<ChildInheritanceEntity>>("token")

            // from parent
            hasTableColumn<ChildInheritanceEntity, Int>("parentValue")

            // from grandparent
            hasTableColumn<ChildInheritanceEntity, Boolean?>("grandParentValue")
        }
    }

    @Test
    fun `can generate embedded property definitions and types`() {
        assertGeneratedEntity<EntityWithEmbeddedProperties> {
            hasEmbeddedProperty<EntityWithEmbeddedProperties, EmbeddableEntity>("embedded") { embeddedContext ->
                embeddedContext.hasEmbeddedTableColumn<EntityWithEmbeddedProperties, String>("foo")
                embeddedContext.hasEmbeddedTableColumn<EntityWithEmbeddedProperties, Int>("bar")
            }
        }
    }

    @Test
    fun `can generate column definition for ElementCollection`() {
        assertGeneratedEntity<EntityWithElementCollection> {
            hasField<
                YawnTableDef<SOURCE, EntityWithElementCollection>.JoinColumnDef<
                    String,
                    YawnTableDef<SOURCE, EntityWithElementCollection>.ElementCollectionDef<String>,
                    >,
                >("strings")
            hasField<
                YawnTableDef<SOURCE, EntityWithElementCollection>.JoinColumnDef<
                    Enums,
                    YawnTableDef<SOURCE, EntityWithElementCollection>.ElementCollectionDef<Enums>,
                    >,
                >("enums")
        }
    }
}
