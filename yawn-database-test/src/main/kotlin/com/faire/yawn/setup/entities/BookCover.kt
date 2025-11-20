package com.faire.yawn.setup.entities

import com.faire.yawn.YawnEntity
import org.hibernate.annotations.Immutable
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Embeddable
internal data class BookCoverCompositeId(
    @Column(updatable = false, nullable = false, name = "book_id")
    var bookId: YawnId<Book> = YawnId(0), // Default value since Hibernate needs a no-arg constructor

    @Column(updatable = false, nullable = false, name = "owner_id")
    var ownerId: YawnId<Person> = YawnId(0), // Default value since Hibernate needs a no-arg constructor
): Serializable

@Immutable
@Entity
@Table(name = "book_covers")
@YawnEntity
internal class BookCover : TimestampedEntity<BookCover>() {
    @javax.persistence.Transient
    override val id: YawnId<BookCover> = YawnId(0)

    @EmbeddedId
    lateinit var cid: BookCoverCompositeId

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", insertable = false, updatable = false)
    lateinit var book: Book

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(insertable = false, updatable = false)
    lateinit var owner: Person

    @Column
    lateinit var material: Material

    @Column
    var inscription: String? = null

    enum class Material {
        PAPER_BAG,
        CLOTH,
    }
}
