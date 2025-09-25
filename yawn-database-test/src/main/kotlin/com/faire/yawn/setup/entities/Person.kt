package com.faire.yawn.setup.entities

import com.faire.yawn.YawnEntity
import com.faire.yawn.setup.custom.EmailAddress
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "people")
@YawnEntity
internal class Person : TimestampedEntity<Person>(), PersonInterface {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override lateinit var id: YawnId<Person>
        protected set

    @Column
    @Version
    var version: Long = 0

    @Column
    override lateinit var name: String

    /**
     * Test for custom adapter via [com.faire.yawn.setup.custom.EmailAddressConverter]
     */
    @Column
    lateinit var email: EmailAddress

    @ManyToOne(fetch = FetchType.LAZY)
    var favoriteBook: Book? = null

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Person::class)
    @JoinColumn
    var favoriteAuthor: PersonInterface? = null

    @ManyToMany(mappedBy = "owners", fetch = FetchType.LAZY, targetEntity = Publisher::class)
    var ownedPublishers: List<Publisher> = listOf()
}
