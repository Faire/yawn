package com.faire.yawn.setup.entities

import com.faire.yawn.YawnEntity
import com.faire.yawn.setup.custom.SerializeAsJson
import org.hibernate.annotations.Formula
import org.hibernate.annotations.NaturalId
import java.io.Serializable
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(
    name = "books",
    indexes = [
        Index(name = "idx_name", columnList = "name"),
        Index(name = "idx_call_number", columnList = "call_number"),
    ],
)
@YawnEntity
// Only need Serializable until Hibernate is upgraded to 6+ due to https://hibernate.atlassian.net/browse/HHH-7668
internal class Book : TimestampedEntity<Book>(), Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override lateinit var id: YawnId<Book>
        protected set

    @Column
    @Version
    var version: Long = 0

    @Column
    lateinit var name: String

    @NaturalId
    @Column(name = "call_number")
    var callNumber: String? = null

    @ElementCollection(targetClass = Genre::class)
    @CollectionTable(name = "book_genres", joinColumns = [JoinColumn(name = "book_id")])
    @Column(name = "genre")
    @Enumerated(EnumType.STRING)
    var genres = setOf<Genre>()

    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var author: Person

    @Column
    lateinit var originalLanguage: Language

    @Column
    var numberOfPages: Long = 0

    @Column
    var rating: Int? = null

    @Column
    var notes: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    var publisher: Publisher? = null

    @Formula("publisher_id IS NOT NULL")
    var hasPublisher: Boolean = false
        protected set

    @Embedded
    lateinit var sales: BookSales

    @Column
    @SerializeAsJson
    var bookMetadata: BookMetadata? = null

    data class BookMetadata(
        val publicationYear: Int,
        val isbn: String,
    )

    enum class Language {
        DANISH,
        ENGLISH,
    }

    enum class Genre {
        FANTASY,
        FAIRY_TALE,
        ADVENTURE,
    }
}
