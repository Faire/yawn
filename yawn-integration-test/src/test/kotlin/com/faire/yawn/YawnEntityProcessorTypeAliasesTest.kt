package com.faire.yawn

import com.faire.yawn.YawnTestUtils.assertGeneratedFile
import com.faire.yawn.criteria.query.JoinTypeSafeCriteriaQuery
import com.faire.yawn.criteria.query.ProjectedTypeSafeCriteriaQuery
import com.faire.yawn.criteria.query.TypeSafeCriteriaQuery
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.reflect.KVisibility
import kotlin.reflect.typeOf

internal class YawnEntityProcessorTypeAliasesTest {
    @Test
    fun `generates type aliases for TableDef`() {
        assertThat(typeOf<EntityWithElementCollectionTableDefType>()).isEqualTo(
            typeOf<EntityWithElementCollectionTableDef<EntityWithElementCollection>>(),
        )
    }

    @Test
    fun `generates type aliases for TypeSafeCriteriaQuery`() {
        assertThat(typeOf<EntityWithElementCollectionCriteriaQuery>()).isEqualTo(
            typeOf<
                TypeSafeCriteriaQuery<
                    EntityWithElementCollection,
                    EntityWithElementCollectionTableDef<EntityWithElementCollection>,
                    >,
                >(),
        )
    }

    @Test
    fun `generates type aliases for JoinTypeSafeCriteriaQuery`() {
        assertThat(typeOf<EntityWithElementCollectionJoinCriteriaQuery>()).isEqualTo(
            typeOf<
                JoinTypeSafeCriteriaQuery<
                    EntityWithElementCollection,
                    EntityWithElementCollection,
                    EntityWithElementCollectionTableDef<EntityWithElementCollection>,
                    >,
                >(),
        )
    }

    @Test
    fun `generates type aliases for ProjectedTypeSafeCriteriaQuery`() {
        assertThat(typeOf<EntityWithElementCollectionProjectedCriteriaQuery<String>>()).isEqualTo(
            typeOf<
                ProjectedTypeSafeCriteriaQuery<
                    EntityWithElementCollection,
                    EntityWithElementCollection,
                    EntityWithElementCollectionTableDef<EntityWithElementCollection>,
                    String,
                    >,
                >(),
        )

        assertThat(typeOf<EntityWithElementCollectionProjectedCriteriaQuery<Boolean>>()).isEqualTo(
            typeOf<
                ProjectedTypeSafeCriteriaQuery<
                    EntityWithElementCollection,
                    EntityWithElementCollection,
                    EntityWithElementCollectionTableDef<EntityWithElementCollection>,
                    Boolean,
                    >,
                >(),
        )
    }

    @Test
    fun `visibility of type aliases is correct`() {
        assertGeneratedFile<InternalEmptyEntityTable> {
            containsTypeAlias("InternalEmptyEntityTableDefType", KVisibility.INTERNAL)
            containsTypeAlias("InternalEmptyEntityCriteriaQuery", KVisibility.INTERNAL)
            containsTypeAlias("InternalEmptyEntityJoinCriteriaQuery", KVisibility.INTERNAL)
            containsTypeAlias("InternalEmptyEntityProjectedCriteriaQuery", KVisibility.INTERNAL)
        }

        assertGeneratedFile<PublicEmptyEntityTable> {
            containsTypeAlias("PublicEmptyEntityTableDefType", KVisibility.PUBLIC)
            containsTypeAlias("PublicEmptyEntityCriteriaQuery", KVisibility.PUBLIC)
            containsTypeAlias("PublicEmptyEntityJoinCriteriaQuery", KVisibility.PUBLIC)
            containsTypeAlias("PublicEmptyEntityProjectedCriteriaQuery", KVisibility.PUBLIC)
        }
    }
}
