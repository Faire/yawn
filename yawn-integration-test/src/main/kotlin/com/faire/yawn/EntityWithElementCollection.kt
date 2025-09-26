package com.faire.yawn

import javax.persistence.ElementCollection
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.JoinTable

@YawnEntity
internal class EntityWithElementCollection {
    @ElementCollection
    @JoinTable
    var strings = mutableListOf<String>()

    @ElementCollection(targetClass = Enums::class)
    @JoinTable
    @Enumerated(EnumType.STRING)
    var enums = listOf<Enums>()
}

internal enum class Enums {
    HELLO,
    THERE,
}
