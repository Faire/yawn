package com.faire.yawn.setup.entities

import java.time.Instant
import javax.persistence.Column
import javax.persistence.MappedSuperclass

@MappedSuperclass
internal abstract class TimestampedEntity<T : Any> : BaseEntity<T> {
  @Column(updatable = false)
  lateinit var createdAt: Instant

  @Column
  lateinit var updatedAt: Instant

  internal fun isCreatedAtInitialized(): Boolean = this::createdAt.isInitialized
}
