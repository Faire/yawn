package com.faire.yawn

import com.faire.yawn.anotherPackage.NonYawnEntityInAnotherPackage
import com.faire.yawn.anotherPackage.YawnEntityInAnotherPackage
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.OneToMany

@YawnEntity
internal class EntityWithXtoManyRelations {
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "name", referencedColumnName = "reference")
    var oneToManyNonYawn: List<NonYawnEntityInAnotherPackage> = listOf()

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "name", referencedColumnName = "reference")
    var oneToManyYawn: List<YawnEntityInAnotherPackage> = listOf()

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "join_table_name",
        joinColumns = [JoinColumn(name = "join_column")],
        inverseJoinColumns = [JoinColumn(name = "inverse_join_column")],
    )
    var manyToManyYawn: List<YawnEntityInAnotherPackage> = listOf()

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "join_table_name",
        joinColumns = [JoinColumn(name = "join_column")],
        inverseJoinColumns = [JoinColumn(name = "inverse_join_column")],
    )
    var manyToManyNonYawn: List<NonYawnEntityInAnotherPackage> = listOf()
}
