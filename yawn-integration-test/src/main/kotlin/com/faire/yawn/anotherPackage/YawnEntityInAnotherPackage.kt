package com.faire.yawn.anotherPackage

import com.faire.yawn.YawnEntity
import javax.persistence.Column

@YawnEntity
internal class YawnEntityInAnotherPackage {
    @Column
    var randomField: String = ""
}
