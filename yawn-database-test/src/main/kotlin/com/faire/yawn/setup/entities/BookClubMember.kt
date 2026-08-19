package com.faire.yawn.setup.entities

import com.faire.yawn.YawnEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "book_club_members")
@YawnEntity
internal class BookClubMember : TimestampedEntity<BookClubMember>() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override lateinit var id: YawnId<BookClubMember>
        protected set

    @Column
    lateinit var name: String

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_club_id")
    lateinit var bookClub: BookClub
}
