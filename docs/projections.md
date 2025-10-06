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

Now you have to choose what to project to. There are **3 kinds of projections** supported by Yawn/Hibernate. All 3 are done by calling the `project` method with
a different type of argument.

### Project to Column

The simplest kind is to just project to a single column. So instead of returning an entire entity, the values of a specific single column are returned.

To do that, just provide the column you wish to project to to the `project` method:

```kotlin
project(books.author)
```

Note that this is fully type safe! The query now returns `String` instead of `DbBook` because **Yawn** knows that `author` is a String.

### Project to Function

Sometimes you want to project to a derived value, like a count or sum. To do that, we take a page off of Hibernate’s book, but instead of using `Projections`,
use our version `YawnProjections`. For example:

```kotlin
project(YawnProjections.count(books.token))
```

or

```kotlin
project(YawnProjections.sum(books.numberOfPages))
```

Again, **Yawn** knows that `sum` must take a numerical type, and that both `count` and `sum` return `Long`.

Other projection functions include the usual suspects such as `distinct`, `countDistinct`, `avg`, `min` and `max`. You can see all currently supported
projection functions on [the `YawnProjections` file][yawn-projections-file].

> [!NOTE]
> 🥱 If something isn’t support by **Yawn**, you can also create your own custom `YawnQueryProjection` by implementing the interface. However, in that case you
> will need to guarantee the type-safety of your implementation!

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
    AuthorAndBooksProjectionDef.create(
      author = TypedProjections.groupBy(books.author),
      numberOfBooks = TypedProjections.count(books.name),
    ),
  )
}
```

## Refine

> [!NOTE]
> 🚸 Currently we don’t support further refining a projection, for example, by using `HAVING`.
> Adding support for this is on our roadmap.

In order to further refine a projection, i.e. add conditions on top of projected values, the `project` function takes an optional block that gives you the
**Yawn** querying DSL but anchored to the projected type. This is yet to be implemented, but would look something like this:

```kotlin
project(
  AuthorAndBooksProjectionDef.create(
    author = TypedProjections.groupBy(books.author),
    numberOfBooks = TypedProjections.count(books.name),
  ),
) { authorAndBooks ->
  addGe(authorAndBooks.numberOfBooks, 1)
}
```

This would be equivalent of a SQL `HAVING`, but might not be fully supported by the underlying Hibernate API.

## More Examples

`applyProjection` can come in handy when you are refactoring code where multiple callers share the same base criteria but apply different projections to it.

```sql
val authors = createBaseBookTableCriteria(yawn)
  .applyProjection { ... }
  .list()

fun createBaseBookTableCriteria(
  yawn: Yawn
): TypeSafeCriteriaBuilder<BookTable, BookTableDef<BookTable>> {
  return yawn.query(BookTable) { books ->
    ...
  }
}
```

```kotlin
val authors = yawn.project(BookTable) { books ->
  addEq(books.name, "The Hobbit")
  project(books.author)
}.list()

// authors = ["Tolkien"]
```

You can see even more complex examples [on this test
file](https://github.com/Faire/yawn/blob/main/yawn-database-test/src/test/kotlin/com/faire/yawn/database/YawnProjectionTest.kt).


[yawn-projections-file]: https://github.com/Faire/yawn/blob/main/yawn-api/src/main/kotlin/com/faire/yawn/project/YawnProjections.kt#L13
