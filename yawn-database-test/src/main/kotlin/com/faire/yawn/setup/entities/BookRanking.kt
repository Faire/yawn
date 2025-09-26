package com.faire.yawn.setup.entities

import com.faire.yawn.YawnEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "book_rankings")
@YawnEntity
internal class BookRanking : TimestampedEntity<BookRanking>() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override lateinit var id: YawnId<BookRanking>
        protected set

    @Column
    @Version
    var version: Long = 0

    @Column
    var ratingYear: Int = 0

    @Column
    var ratingMonth: Int = 0

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn
    lateinit var bestSeller: Book
}
