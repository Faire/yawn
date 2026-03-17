package com.faire.yawn

import com.faire.yawn.YawnTestUtils.assertGeneratedEntity
import com.faire.yawn.inheritance.EntityWithInterfaceAndBaseClass
import org.junit.jupiter.api.Test

/**
 * Regression test for a KSP bug where `getAllProperties()` drops Java annotations when a property
 * is declared in both an interface and an abstract class.
 *
 * The `name` property is declared in [com.faire.yawn.inheritance.HasName] (no annotations) and
 * in [com.faire.yawn.inheritance.BaseEntityWithName] (with `@Column(name = "name")`).
 * KSP's `getAllProperties()` merges them and silently loses the `@Column` annotation.
 */
internal class KspGetAllPropertiesBugTest {
    @Test
    fun `property with @Column from base class is not lost when interface also declares it`() {
        assertGeneratedEntity<EntityWithInterfaceAndBaseClass> {
            hasTableColumn<EntityWithInterfaceAndBaseClass, String>("name")
        }
    }
}
