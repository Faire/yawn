# Projections

How do Projections work on **Yawn**? As with our other APIs, it is actually heavily inspired by Hibernate, but comes with type-safety on top.

## Basics

The first step is to call `yawn.project` (instead of `yawn.query`). That will do two things:

- tell **Yawn** to expect your lambda to return a `YawnQueryProjection`
- give access to the `project` method, which returns the `YawnQueryProjection`

So the expectation is that within the `yawn.project` lambda, the last instruction should be a call to `project`, which will satisfy the return type and tell
**Yawn** which type is being projected to (and how to map the projection).

This is what it looks like:

```kotlin
yawn.project(BookTable) { books ->
    // ... (normal query stuff)

    // we must return a projection!
    project(...)
}
```

Each projected **Yawn** lambda *must* return a projection, and can only call `project` once.

## Types of Projections

Now you have to choose what to project to. There are **4 kinds of projections** supported by Yawn/Hibernate. All 4 are done by calling the `project` method with
a different type of argument.

### Project to Column

The simplest kind is to just project to a single column. So instead of returning an entire entity, the values of a specific single column are returned.

To do that, just provide the column you wish to project to to the `project` method:

```kotlin
project(books.author)
```

Note that this is fully type-safe! The query now returns `String` instead of `Book` because **Yawn** knows that `author` is a String.

### Project to Entity

You can also project an entire entity instead of an individual column.
For example, the following query returns the publishers of books whose names start with `The`:

```kotlin
val publishers = yawn.project(BookTable) { books ->
    addLike(books.name, "The %")
    addIsNotNull(books.publisher)

    project(books.publisher)
}.set()
```

This returns `Publisher` entities rather than `Book` entities. If you only need the identifier of the referenced entity, project
`books.publisher.foreignKey` instead.

### Project to Function

Sometimes you want to project to a derived value, like a count or sum. To do that, we take a page off of Hibernate’s book, but instead of using `Projections`,
use our version `YawnProjections`. For example:

```kotlin
project(YawnProjections.count(books.id))
```

or

```kotlin
project(YawnProjections.sum(books.numberOfPages))
```

Again, **Yawn** knows that `sum` must take a numerical type, and that both `count` and `sum` return `Long`.

Other projection functions include the usual suspects such as `countDistinct`, `avg`, `min` and `max`. You can see all currently supported
projection functions on [the `YawnProjections` file][yawn-projections-file].

Note that `countDistinct` is an *aggregate-level* distinct, i.e. `COUNT(DISTINCT column)`. That is a different thing from a query-level `SELECT DISTINCT`,
which applies to the whole projection and is requested on the builder instead:

```kotlin
val authorNames = yawn.project(BookTable) { books ->
    val authors = join(books.author)
    project(authors.name)
}.distinct().list()
```

`distinct()` is only available on projected queries, since Hibernate only supports `DISTINCT` through projections.

> [!NOTE]
> 🥱 If something isn’t support by **Yawn**, you can also create your own custom `YawnQueryProjection` by implementing the interface. However, in that case you
> will need to guarantee the type-safety of your implementation!

#### Ordering by an aggregate

A plain column can be ordered directly (`orderAsc`/`orderDesc`, or `YawnQueryOrder.asc`/`.desc`), but an aggregate or
other projected expression has no property name of its own to order by. `orderAscBy`/`orderDescBy` handle this for
you: pass them the projection, and use the *returned* value in `project(...)`:

```kotlin
yawn.project(VisitTable) { visits ->
    val mostRecentVisit = orderDescBy(YawnProjections.max(visits.createdAt))
    project(YawnProjections.pair(YawnProjections.groupBy(visits.brandId), mostRecentVisit))
}.list()
```

This groups visits by brand and returns each brand's most recent visit time, sorted with the most-recently-visited
brands first - a "top-N per group" query expressed as a single SQL-side query instead of fetching every row and
sorting in Kotlin. The value returned by `orderDescBy`/`orderAscBy` must be passed to `project(...)` (nesting it
inside a `pair`/`triple`/`@YawnProjection` data class is fine): the expression can only be ordered by once it's also
selected, so if the returned value isn't projected, resolving the order will fail at query time.

### Project to Data Class

Sometimes you want to return more than a single field. For that, you can project to a data class with any assortment of columns you desire, built off of other
types of projections.

To do that, first design your data class and annotate it with `@YawnProjection`. That is equivalent to `YawnEntity` but for mapped projections, and will
generate all the necessary boilerplate with KSP to make **Yawn** work in a type-safe and efficient manner (no reflection!).

```kotlin
@YawnProjection
internal data class SimpleBook(
    val author: String,
    val numberOfPages: Long,
)
```

This will generate a `SimpleBookProjectionDef` representing your projection, analogous to an entity table definition. Then, you will want to use
`SimpleBookProjection.create` to power the `project` method:

```kotlin
val result = yawn.project(BookTable) { books ->
    addEq(books.author, "J.K. Rowling")

    project(
        SimpleBookProjection.create(
            author = books.author,
            numberOfPages = books.numberOfPages,
        ),
    )
}.uniqueResult()!!

// result _is_ a SimpleBook!
```

Just like magic! No more manual mapping and unsafe queries!

> [!NOTE]
> 🥱 One important thing to note is that **Yawn** currently only support `internal` (or `public`) types. So your data class cannot be `private` nor defined
> inside the scope of a function definition.

Note that the projected fields can be functions as well! For example, if you want to `GROUP BY`:

```kotlin
@YawnProjection
internal data class AuthorAndBooks(
  val author: String,
  val numberOfBooks: Long,
)

// later:
yawn.project(BookTable) {
  project(
    AuthorAndBooksProjection.create(
      author = YawnProjections.groupBy(books.author),
      numberOfBooks = YawnProjections.count(books.name),
    ),
  )
}
```

### Project to Custom SQL

When the value you want isn’t expressible with the built-in functions, project a single raw SQL expression with `sqlValue`, instead of implementing
`YawnQueryProjection` by hand:

```kotlin
project(sqlValue<Long> { "SUM(${books.numberOfPages.sql} * ${books.sales.paperBacksSold.sql})" })
```

Reference columns through `.sql`, which **Yawn** substitutes with the physical column backing that property, already qualified by its table’s alias. This works
for joined tables and embedded types too. Prefer it over writing column names by hand: a property’s name and its column’s name coincide only until someone maps
one explicitly, and `.sql` keeps working when they don’t.

The type argument decides how the result is mapped, and should be nullable when the expression can evaluate to `NULL`:

```kotlin
project(sqlValue<Int?> { "NULLIF(${books.rating.sql}, 0)" })
```

The result composes anywhere an ordinary column does — inside `pair`, `triple`, or a data class projection:

```kotlin
val pagesPrintedPerAuthor = yawn.project(BookTable) { books ->
  val authors = join(books.author)

  project(
    YawnProjections.pair(
      YawnProjections.groupBy(authors.name),
      sqlValue<Long> { "SUM(${books.numberOfPages.sql} * ${books.sales.paperBacksSold.sql})" },
    ),
  )
}.list()
```

Do not name the result yourself (no `AS total`): **Yawn** selects it under an alias it generates, so two SQL values in one query can never collide.

`sqlValue` lives on the projected query scope, so it already knows what you are selecting from and only the result type has to be named. To share one across
queries, write a helper on that scope, taking the table definition as a parameter:

```kotlin
private fun BookProjectedQueryScope<Long>.pagesPrinted(
  books: BookTableDefType,
): YawnSingleValueProjection<Book, Long> {
  return sqlValue<Long> { "SUM(${books.numberOfPages.sql} * ${books.sales.paperBacksSold.sql})" }
}
```

> [!WARNING]
> 🥱 Raw SQL projections cannot bind parameters, so anything you interpolate into the expression is inlined into the statement verbatim. Never build one out of
> untrusted input. And as with any raw SQL, the result type is a claim **Yawn** takes at face value — it cannot verify that your expression really produces it.

## Refine

> [!NOTE]
> 🚸 Currently we don’t support further refining a projection, for example, by using `HAVING`.
> This section is documenting a potentially future feature; adding support for this is on our roadmap but might not work well with Hibernate.

In order to further refine a projection, i.e. add conditions on top of projected values, the `project` function takes an optional block that gives you the
**Yawn** querying DSL but anchored to the projected type. This is yet to be implemented, but would look something like this:

```kotlin
project(
  AuthorAndBooksProjection.create(
    author = YawnProjections.groupBy(books.author),
    numberOfBooks = YawnProjections.count(books.name),
  ),
) { authorAndBooks ->
  addGe(authorAndBooks.numberOfBooks, 1)
}
```

This would be equivalent of a SQL `HAVING`, but might not be fully supported by the underlying Hibernate API.

## More Examples

`applyProjection` can come in handy when you are refactoring code where multiple callers share the same base criteria but apply different projections to it.

```kotlin
val authors = createBaseBookTableCriteria(yawn)
  .applyProjection { ... }
  .list()

fun createBaseBookTableCriteria(
  yawn: Yawn
): EntityYawnQueryBuilder<BookTable, BookTableDef<BookTable>> {
  return yawn.query(BookTable) { books ->
    ...
  }
}
```

You can see even more complex examples [on this test file][yawn-projections-test].


[yawn-projections-file]: https://github.com/Faire/yawn/blob/main/yawn-api/src/main/kotlin/com/faire/yawn/project/YawnProjections.kt#L13
[yawn-projections-test]: https://github.com/Faire/yawn/blob/main/yawn-database-test/src/test/kotlin/com/faire/yawn/database/YawnProjectionTest.kt
