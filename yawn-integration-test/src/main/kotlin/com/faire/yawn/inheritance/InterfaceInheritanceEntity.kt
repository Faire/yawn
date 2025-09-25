package com.faire.yawn.inheritance

import com.faire.yawn.utils.FakeToken

internal interface InterfaceInheritanceEntity {
    val token: FakeToken<out InterfaceInheritanceEntity>
}
