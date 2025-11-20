package com.faire.yawn.setup.entities

import com.faire.yawn.YawnEntity
import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Immutable
@Entity
@Table(name = "book_reviews")
@YawnEntity
internal class BookReview : TimestampedEntity<BookReview>() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override lateinit var id: YawnId<BookReview>
        protected set

    @Column
    var reviewText: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    lateinit var reviewer: Person

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_number", referencedColumnName = "call_number")
    lateinit var book: Book
}
