package com.faire.yawn

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
internal class EmbeddableEntity {
    @Column
    lateinit var foo: String
        private set

    @Column
    var bar: Int = 0
        private set
}
