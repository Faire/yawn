package com.faire.yawn.inheritance

import javax.persistence.Column

internal abstract class BaseEntityWithName {
    @Column(name = "name")
    lateinit var name: String
}
