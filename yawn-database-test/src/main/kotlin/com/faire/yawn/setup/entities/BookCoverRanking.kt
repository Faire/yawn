package com.faire.yawn.setup.entities

import com.faire.yawn.YawnEntity
import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@Immutable
@Entity
@Table(name = "book_cover_ranking")
@YawnEntity
internal class BookCoverRanking : TimestampedEntity<BookCoverRanking>() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override lateinit var id: YawnId<BookCoverRanking>
    
    @Column
    var ranking: Int = 0

    @Column
    var judgesComments: String? = null
    
    @OneToOne
    @JoinColumn(name = "owner_id")
    @JoinColumn(name = "book_id")
    lateinit var bookCover: BookCover
}
