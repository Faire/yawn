package com.faire.yawn.setup.entities

import com.faire.yawn.setup.custom.EmailAddress
import com.faire.yawn.setup.entities.Book.BookMetadata
import com.faire.yawn.setup.entities.Book.Genre.ADVENTURE
import com.faire.yawn.setup.entities.Book.Genre.FAIRY_TALE
import com.faire.yawn.setup.entities.Book.Genre.FANTASY
import com.faire.yawn.setup.entities.Book.Language.DANISH
import com.faire.yawn.setup.entities.Book.Language.ENGLISH
import com.faire.yawn.setup.hibernate.YawnTestSession
import com.faire.yawn.setup.hibernate.YawnTestTransactor
import kotlin.reflect.KClass

internal class BookFixtures(
    private val transactor: YawnTestTransactor,
) {
    fun setup() {
        fixtures {
            val tolkien = createPerson {
                name = "J.R.R. Tolkien"
                email = EmailAddress("tolkien@faire.com")
            }
            val rowling = createPerson {
                name = "J.K. Rowling"
                email = EmailAddress("rowling@faire.com")
            }
            val andersen = createPerson {
                name = "Hans Christian Andersen"
                email = EmailAddress("andersen@faire.com")
            }

            val penguin = createPublisher { name = "Penguin" }
            val harperCollins = createPublisher { name = "HarperCollins" }
            val randomHouse = createPublisher { name = "Random House" }
            val coOwned = createPublisher { name = "Co-Owned" }

            val lordOfTheRings = createBook(tolkien) {
                name = "Lord of the Rings"
                genres = setOf(FANTASY, ADVENTURE)
                originalLanguage = ENGLISH

                publisher = harperCollins
                numberOfPages = 1_000
                rating = 10
                sales = BookSales(
                    paperBacksSold = 1_500_000,
                    hardBacksSold = 1_499_999,
                    eBooksSold = 700_000,
                    countryWithMostCopiesSold = "UK",
                )
                bookMetadata = BookMetadata(
                    publicationYear = 1954,
                    isbn = "978-3-16-148410-0",
                )

                notes = "Note for Lord of the Rings"
            }
            createBook(tolkien) {
                name = "The Hobbit"
                genres = setOf(FANTASY, ADVENTURE)
                originalLanguage = ENGLISH

                publisher = randomHouse
                numberOfPages = 300
                rating = 9
                sales = BookSales(
                    paperBacksSold = 2_000_000,
                    hardBacksSold = 1_999_999,
                    eBooksSold = 900_000,
                    countryWithMostCopiesSold = "UK",
                )
                bookMetadata = BookMetadata(
                    publicationYear = 1937,
                    isbn = "978-0-261-10221-7",
                )

                notes = "Note for The Hobbit and Harry Potter"
            }

            val hp = createBook(rowling) {
                name = "Harry Potter"
                genres = setOf(FANTASY)
                originalLanguage = ENGLISH

                publisher = penguin
                numberOfPages = 500
                sales = BookSales(
                    paperBacksSold = 1_000_000,
                    hardBacksSold = 999_999,
                    eBooksSold = 500_000,
                    countryWithMostCopiesSold = "UK",
                )

                notes = "Note for The Hobbit and Harry Potter"
            }

            val littleMermaid = createBook(andersen) {
                name = "The Little Mermaid"
                genres = setOf(FAIRY_TALE)
                originalLanguage = DANISH

                numberOfPages = 100
                sales = BookSales(
                    paperBacksSold = 600,
                    hardBacksSold = 300,
                    eBooksSold = 100,
                    countryWithMostCopiesSold = "NL",
                )
            }
            createBook(andersen) {
                name = "The Ugly Duckling"
                genres = setOf(FAIRY_TALE)
                originalLanguage = DANISH

                numberOfPages = 110
                sales = BookSales(
                    paperBacksSold = 600_000,
                    hardBacksSold = 300_000,
                    eBooksSold = 200_000,
                    countryWithMostCopiesSold = "US",
                )
            }
            val emperorsNewClothes = createBook(andersen) {
                name = "The Emperor's New Clothes"
                genres = setOf(FAIRY_TALE)
                originalLanguage = DANISH

                publisher = penguin
                numberOfPages = 120
                sales = BookSales(
                    paperBacksSold = 600_000,
                    hardBacksSold = 300_000,
                    eBooksSold = 200_000,
                    countryWithMostCopiesSold = "CA",
                )
            }

            createPerson {
                name = "Paul Duchesne"
                email = EmailAddress("paul.duchesne@faire.com")
                favoriteBook = lordOfTheRings
                favoriteAuthor = andersen
            }
            createPerson {
                name = "Luan Nico"
                email = EmailAddress("luan@faire.com")
                favoriteBook = hp
                favoriteAuthor = tolkien
            }
            update(rowling) {
                favoriteBook = lordOfTheRings
                favoriteAuthor = tolkien
            }
            update(tolkien) {
                favoriteBook = littleMermaid
                favoriteAuthor = andersen
            }

            val john = createOwner(penguin, randomHouse) {
                name = "John Doe"
                favoriteAuthor = rowling
            }
            val jane = createOwner(harperCollins) {
                name = "Jane Doe"
                favoriteBook = emperorsNewClothes
            }
            update(coOwned) {
                owners = listOf(john, jane)
            }

            createBookRanking {
                ratingYear = 2007
                ratingMonth = 1
                bestSeller = hp
            }
        }
    }

    private fun fixtures(setup: Context.() -> Unit) {
        transactor.open { session -> Context(session).setup() }
    }

    private class Context(
        private val session: YawnTestSession,
    ) {
        fun createPerson(setup: Person.() -> Unit): Person {
            return update(Person(), setup)
        }

        fun createPublisher(setup: Publisher.() -> Unit): Publisher {
            return update(Publisher(), setup)
        }

        fun createBookRanking(setup: BookRanking.() -> Unit): BookRanking {
            return update(BookRanking(), setup)
        }

        fun createBook(
            author: Person,
            setup: Book.() -> Unit,
        ): Book {
            val book = update(Book()) {
                this.author = author
                setup()
            }

            val publisher = book.publisher
            if (publisher != null) {
                publisher.publishedBookIds.add(book.id)
                session.save(publisher)
            }

            return book
        }

        fun createOwner(vararg publishers: Publisher, setup: Person.() -> Unit): Person {
            val owner = createPerson {
                ownedPublishers = publishers.toList()
                setup()
            }
            for (publisher in publishers) {
                update(publisher) {
                    owners += owner
                }
            }
            return owner
        }

        fun <T : BaseEntity<T>> update(
            entity: T,
            setup: T.() -> Unit,
        ): T {
            return session.save(entity.apply(setup))
        }
    }

    companion object {
        val entities = setOf<KClass<out BaseEntity<*>>>(
            Book::class,
            BookRanking::class,
            BookView::class,
            Person::class,
            Publisher::class,
        )
    }
}
