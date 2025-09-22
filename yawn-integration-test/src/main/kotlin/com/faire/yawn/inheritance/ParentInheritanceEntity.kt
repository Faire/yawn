package com.faire.yawn.inheritance

import javax.persistence.Column

internal abstract class ParentInheritanceEntity : GrandParentInheritanceEntity() {
  @Column
  var parentValue: Int = 0
}
