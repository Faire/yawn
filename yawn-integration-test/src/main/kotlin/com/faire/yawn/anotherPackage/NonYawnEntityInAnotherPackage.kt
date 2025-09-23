package com.faire.yawn.anotherPackage

import javax.persistence.Entity
import javax.persistence.Table

@Entity(name = "not_yawning")
@Table(name = "not_yawning")
@Suppress("EntityClassMustBeYawnEntity") // intentional for testing purposes
internal class NonYawnEntityInAnotherPackage {
    @javax.persistence.Id
    var id: Long = 0L
}
