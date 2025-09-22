package com.faire.yawn

import javax.persistence.Embedded

@YawnEntity
internal class EntityWithEmbeddedProperties {
  @Embedded
  lateinit var embedded: EmbeddableEntity
    private set
}
