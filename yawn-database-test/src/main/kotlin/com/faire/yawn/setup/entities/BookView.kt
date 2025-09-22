package com.faire.yawn.setup.entities

import com.faire.yawn.YawnEntity
import com.faire.yawn.setup.custom.ReadOnlyEntity
import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity(name = "book_views")
@Table(name = "books")
@EntityListeners(ReadOnlyEntity::class)
@YawnEntity
@Immutable
internal class BookView : BaseEntity<BookView> {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  final override lateinit var id: YawnId<BookView>
    private set

  @Column
  final lateinit var name: String
    private set
}
