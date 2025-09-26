package com.faire.yawn.setup.entities

import com.faire.yawn.YawnEntity
import org.hibernate.annotations.Formula
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "publishers")
@YawnEntity
internal class Publisher : TimestampedEntity<Publisher>() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override lateinit var id: YawnId<Publisher>
        protected set

    @Column
    @Version
    var version: Long = 0

    @Column
    lateinit var name: String

    @OneToMany(mappedBy = "publisher", fetch = FetchType.LAZY, targetEntity = Book::class)
    var books: List<Book> = listOf()

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "publisher_published_books",
        joinColumns = [JoinColumn(name = "publisher_id", referencedColumnName = "id")],
    )
    @Column(name = "book_id")
    var publishedBookIds: MutableSet<YawnId<Book>> = mutableSetOf()

    @ManyToMany(fetch = FetchType.LAZY, targetEntity = Person::class)
    @JoinTable(
        name = "publisher_owners",
        joinColumns = [JoinColumn(name = "publisher_id")],
        inverseJoinColumns = [JoinColumn(name = "person_id")],
    )
    var owners: List<Person> = listOf()

    @Formula("LENGTH(name)")
    var nameLetterCount: Int = 0
        protected set
}
