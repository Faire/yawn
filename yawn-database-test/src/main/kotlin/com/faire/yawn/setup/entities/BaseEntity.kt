package com.faire.yawn.setup.entities

internal interface BaseEntity<T : Any> {
  val id: YawnId<T>
}
