package com.faire.yawn

import com.faire.yawn.pagination.PageNumber
import javax.persistence.Column
import javax.persistence.Id

/**
 * Test entity that uses a [PageNumber] field — a [@JvmInline value class][JvmInline] defined in
 * the [yawn-api] module, which is a binary dependency of this module.
 *
 * This entity exists to verify that Yawn's KSP processor correctly generates value-class adapters
 * for cross-module value-class types (i.e. types that are not in the current compilation unit).
 */
@YawnEntity
internal class EntityWithCrossModuleValueClass {
    @Id
    var id: Long = 0
        protected set

    /**
     * Cross-module @JvmInline value class from yawn-api.
     * The KSP processor must generate an adapter that unwraps [PageNumber] to its underlying [Int]
     * before Hibernate binds the parameter.
     */
    @Column
    var pageNumber: PageNumber? = null
}
