package com.faire.yawn

import com.faire.yawn.YawnTestUtils.assertGeneratedFile
import com.faire.yawn.criteria.query.EntityYawnQueryScope
import com.faire.yawn.criteria.query.JoinYawnQueryScope
import com.faire.yawn.criteria.query.ProjectedYawnQueryScope
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
    fun `generates type aliases for EntityYawnQueryScope`() {
        assertThat(typeOf<EntityWithElementCollectionEntityQueryScope>()).isEqualTo(
            typeOf<
                EntityYawnQueryScope<
                    EntityWithElementCollection,
                    EntityWithElementCollectionTableDef<EntityWithElementCollection>,
                    >,
                >(),
        )
    }

    @Test
    fun `generates type aliases for JoinYawnQueryScope`() {
        assertThat(typeOf<EntityWithElementCollectionJoinQueryScope>()).isEqualTo(
            typeOf<
                JoinYawnQueryScope<
                    *,
                    EntityWithElementCollection,
                    *,
                    >,
                >(),
        )
    }

    @Test
    fun `generates type aliases for ProjectedYawnQueryScope`() {
        assertThat(typeOf<EntityWithElementCollectionProjectedQueryScope<String>>()).isEqualTo(
            typeOf<ProjectedYawnQueryScope<*, EntityWithElementCollection, *, String>>(),
        )

        assertThat(typeOf<EntityWithElementCollectionProjectedQueryScope<Boolean>>()).isEqualTo(
            typeOf<ProjectedYawnQueryScope<*, EntityWithElementCollection, *, Boolean>>(),
        )
    }

    @Test
    fun `visibility of type aliases is correct`() {
        assertGeneratedFile<InternalEmptyEntityTable> {
            containsTypeAlias("InternalEmptyEntityTableDefType", KVisibility.INTERNAL)
            containsTypeAlias("InternalEmptyEntityEntityQueryScope", KVisibility.INTERNAL)
            containsTypeAlias("InternalEmptyEntityJoinQueryScope", KVisibility.INTERNAL)
            containsTypeAlias("InternalEmptyEntityProjectedQueryScope", KVisibility.INTERNAL)
        }

        assertGeneratedFile<PublicEmptyEntityTable> {
            containsTypeAlias("PublicEmptyEntityTableDefType", KVisibility.PUBLIC)
            containsTypeAlias("PublicEmptyEntityEntityQueryScope", KVisibility.PUBLIC)
            containsTypeAlias("PublicEmptyEntityJoinQueryScope", KVisibility.PUBLIC)
            containsTypeAlias("PublicEmptyEntityProjectedQueryScope", KVisibility.PUBLIC)
        }
    }
}
