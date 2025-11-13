# Joins

## Joining a Table

In order to join to another table, you can use the `join` function provided within the Query DSL. Provide as a parameter the column you want to use to join, and
you will get in return an object representing the joined table, from which you can access the new columns (and do more joins!).

Supposed we want to get all the books from a publisher HarperCollins:

```kotlin
val results = yawn.query(BookTable) { books ->
    val publishers = join(books.publisher)
    addEq(publishers.name, "HarperCollins")
}.list()
```

1. Since we want to fetch a list of books, we start a criteria against the `Book` table
2. We join publisher table with books
3. Then we add a select for publishers with the name HarperCollins

## Nested/Multiple Joins

Get all the publishers for all the books written by J.R.R. Tolkien:

```kotlin
val results = yawn.query(PublisherTable) { publishers ->
    val books = join(publishers.books)
    val authors = join(books.author)
    addEq(authors.name, "J.R.R. Tolkien")
}.list()
```

1. Since we want to fetch all the publishers, we start a criteria against the `Publisher` table
2. Then we join publisher with books
3. Then we join books with authors
4. At this point, we have this relation publisher ↔ books ↔ authors; now we can select against the author name.

## Relationship Types

### OneToOne

One Book has one BookRanking, and one BookRanking has one Book.

To fetch the BookRanking for "The Hobbit", you can do a join against the `@OneToOne` relationship and filter against the Book.

```kotlin
val bookRanking = yawn.query(BookRankingTable) { bookRankings ->
    val books = join(bookRankings.book)
    addEq(books.name, "The Hobbit")
}.uniqueResult()
```

### ManyToOne and OneToMany

One book has one publisher, but a publisher can publish many books.

If you want to get all books by a given publisher:

```kotlin
val results = yawn.query(BookTable) { books ->
    val publishers = join(books.publisher)
    addEq(publishers.name, "HarperCollins")
}.list()
```

Or if you want to get the publisher for a book:

```kotlin
val result = yawn.query(PublisherTable) { publishers -> 
    val books = join(publishers.books)
    addEq(books.name, "The Hobbit")
}.uniqueResult()
```

### ManyToMany

One person can own many publishers, and a publisher can be owned by many people.

If you want to get all the publishers owned by a person:

```kotlin
val results = yawn.query(PublisherTable) { publishers ->
    val owners = join(publishers.owners)
    addEq(owners.name, "Jane Doe")
}.list()
```

## Advanced

### Further refining

Alongside the foreign key constraint, you can provide additional criteria to be applied to the `ON` clause of your join:

```kotlin
val publishers = join(publishers.books) { books ->
    addEq(books.genres, Genre.FANTASY)
}
```

### Multiple joins to the same table

Each time you call the `.join` function, a new join is added to the query.

For example, say you want the intersection of publishers owned by John and Jane Doe:

> [!NOTE]
> ⚠️ This is NOT supported by the Hibernate Criteria API, so it will fail at runtime if you are using that
> as your backing implementation for Yawn.

```kotlin
val results = session.query(PublisherTable) { publishers ->
    val owners1 = join(publishers.owners)
    val owners2 = join(publishers.owners)
    addEq(owners1.name, "John Doe")
    addEq(owners2.name, "Jane Doe")
}.list()
```

### Join references

In order to re-use a join in a different block, you can save a reference to it using the `.joinRef()` method:

```kotlin
val criteria = session.query(BookTable)
val authorsRef = criteria.joinRef { author }

criteria.applyFilter { books ->
    val authors = authorsRef.get(books)
    addLike(authors.name, "J.%")
}

criteria.applyFilter { books ->
    val authors = authorsRef.get(books)
    addLike(authors.name, "%n")
}

val results = criteria.list() // The Hobbit, Lord of the Rings
```

Note that join references are for rare cases in which you want to pass a query around to be build piecemeal.
Ideally, you can put all your query within a single lambda.

### Join Types

You can specify the `joinType` if you want anything other than `INNER_JOIN`:

```kotlin
val publishers = join(books.publisher, joinType = LEFT_OUTER_JOIN)
```

The complete options for `JoinType` are: `INNER_JOIN`, `LEFT_OUTER_JOIN`, `RIGHT_OUTER_JOIN`, `FULL_JOIN`.
