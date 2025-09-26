package com.faire.yawn.setup.custom

import javax.persistence.PrePersist
import javax.persistence.PreRemove
import javax.persistence.PreUpdate

/**
 * An entity can be designated as read-only by adding `@EntityListeners(ReadOnlyEntity::class)` as an annotation
 * to the class. This is a little different from @Immutable, which allows inserts and also will not throw exceptions on
 * any attempt to update or delete.
 */
internal class ReadOnlyEntity {
    @PrePersist
    fun onPrePersist(entity: Any?) {
        error("Attempting to persist a read-only entity of type ${entity?.javaClass}.")
    }

    @PreUpdate
    fun onPreUpdate(entity: Any?) {
        error("Attempting to update a read-only entity of type ${entity?.javaClass}.")
    }

    @PreRemove
    fun onPreRemove(entity: Any?) {
        error("Attempting to remove a read-only entity of type ${entity?.javaClass}.")
    }
}
