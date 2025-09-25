package com.faire.yawn.inheritance

import javax.persistence.Column

internal open class GrandParentInheritanceEntity {
    @Column
    val grandParentValue: Boolean? = null
}
