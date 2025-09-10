package com.faire.yawn

import com.faire.yawn.utils.FakeToken
import javax.persistence.Column
import javax.persistence.Id

@YawnEntity
internal class EntityWithoutRelations {
  @Id
  var id: Long = 0
    protected set

  @Column
  var version: Long = 0
    protected set

  @Column
  var name: String = ""
    protected set

  // make sure classes are properly imported in the generated file
  @Column
  var token: FakeToken<String> = FakeToken("")
    protected set
}
