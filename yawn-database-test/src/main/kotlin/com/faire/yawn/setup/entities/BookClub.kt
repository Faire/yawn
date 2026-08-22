package com.faire.yawn.setup.entities

import com.faire.yawn.YawnEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.persistence.Version

/**
 * A dedicated entity for exercising pagination against an eager `@OneToMany` collection
 * ([BookClubMember]), without affecting the behavior of pagination tests against [Book] et al -
 * none of the fixture entities have an eager one-to-many relationship, so a new one was needed
 * (unlike [Publisher.books]/[Book.genres], which are `@OneToMany`/`@ElementCollection` but lazy).
 *
 * See [YawnPaginationQueriesTest] for why this matters: an eager collection causes Hibernate to
 * LEFT OUTER JOIN it in whenever a [BookClub] entity is hydrated, fanning a single [BookClub] row
 * out into one row per [BookClubMember]. If a real page size is applied directly on top of that
 * join, the LIMIT/OFFSET truncates at the fanned SQL row level rather than the distinct-entity level.
 */
@Entity
@Table(name = "book_clubs")
@YawnEntity
internal class BookClub : TimestampedEntity<BookClub>() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override lateinit var id: YawnId<BookClub>
        protected set

    @Column
    @Version
    var version: Long = 0

    @Column
    lateinit var name: String

    @OneToMany(mappedBy = "bookClub", fetch = FetchType.EAGER)
    var members: List<BookClubMember> = listOf()
}
