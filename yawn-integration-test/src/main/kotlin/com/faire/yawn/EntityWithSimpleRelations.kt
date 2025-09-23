package com.faire.yawn

import com.faire.yawn.anotherPackage.NonYawnEntityInAnotherPackage
import com.faire.yawn.anotherPackage.YawnEntityInAnotherPackage
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne

@YawnEntity
internal class EntityWithSimpleRelations {
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn
  var nullableOneToOneYawn: YawnEntityInAnotherPackage? = null
    protected set

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn
  lateinit var nonNullOneToOneYawn: YawnEntityInAnotherPackage
    protected set

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn
  var oneToOneNonYawn: NonYawnEntityInAnotherPackage? = null
    protected set

  @OneToOne(fetch = FetchType.LAZY, targetEntity = YawnEntityInAnotherPackage::class)
  @JoinColumn
  var oneToOneYawnWithTargetEntity: Any? = null
    protected set

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  lateinit var manyToOneYawn: YawnEntityInAnotherPackage

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  lateinit var manyToOneNonYawn: NonYawnEntityInAnotherPackage

  @ManyToOne(fetch = FetchType.LAZY, targetEntity = YawnEntityInAnotherPackage::class)
  @JoinColumn
  lateinit var manyToOneYawnWithTargetEntity: Any
}
