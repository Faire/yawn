package com.faire.yawn.inheritance

import com.faire.yawn.YawnEntity
import com.faire.yawn.utils.FakeToken
import javax.persistence.Column

@YawnEntity
internal class ChildInheritanceEntity : ParentInheritanceEntity(), InterfaceInheritanceEntity {
    @Column
    var childValue: String = ""

    @Column
    override var token: FakeToken<ChildInheritanceEntity> = FakeToken.generate()
}
