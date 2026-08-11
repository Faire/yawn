# Building Queries Piecemeal

Yawn query builders are mutable. This makes it possible to start a query in one function, pass it to other functions to add filters, joins, ordering,
or pagination, and execute it only after all of the pieces have been applied.

Usually having a single query lambda is preferred when all of the query logic belongs together.
Piecemeal construction is useful when several callers share filters or when optional filters are selected at runtime.

## Passing a query builder around

Create the builder without a lambda, pass it to functions that call `applyFilter`, and invoke a terminal operation only after the query is complete:

```kotlin
fun findBooks(filters: BookFilters): List<Book> {
    val query = yawn.query(BookTable)

    query.applyBookFilters(filters)
    query.orderAsc { name }

    return query.list()
}

private fun EntityYawnQueryBuilder<Book, BookTableDefType>.applyBookFilters(
    filters: BookFilters,
) {
    if (filters.title != null) {
        val title = filters.title
        applyFilter { books ->
            addLike(books.name, "%$title%")
        }
    }

    if (filters.minimumPages != null) {
        val minimumPages = filters.minimumPages
        applyFilter { books ->
            addGe(books.numberOfPages, minimumPages)
        }
    }
}
```

Each call mutates and returns the same builder, so calls can also be chained. Do not call `list`, `uniqueResult`, or another terminal operation in a helper
that is only meant to contribute part of a query.

If a base query will be used to execute more than one variation, create a new base builder for each variation or call `clone()` before diverging.
Mutations accumulate on a builder; executing it does not reset it.

## Choosing a type for a helper

The type to use depends on whether the helper receives a builder or runs inside a query lambda.

| Helper receives | Type to use | What it can do |
| --- | --- | --- |
| A query being assembled | `EntityYawnQueryBuilder<Entity, TableDef>` | Add filters, projections, ordering, pagination, and terminal operations |
| The receiver inside a root entity-query lambda | The generated `EntityEntityQueryScope` alias | Add filters and joins |
| The receiver inside a projection lambda | The generated `EntityProjectedQueryScope<Result>` alias | Add filters and define a projection |
| The receiver inside a join lambda, or any scope at all | `YawnQueryScopeWithWhere<SOURCE, Entity>` | Add filters |

KSP generates the scope aliases and a `TableDefType` alias for every `@YawnEntity`. For example, an entity named `Book` produces `BookEntityQueryScope`,
`BookProjectedQueryScope<Result>`, and `BookTableDefType`. Prefer these aliases for lambda helpers because they hide the internal `SOURCE`,
table-definition, and projection generics:

```kotlin
private fun BookEntityQueryScope.addPublishedFilter(
    books: BookTableDefType,
) {
    addEq(books.published, true)
}
```

There is deliberately no alias for a join lambda. A join's source is the entity of the *enclosing* query rather than the joined one, so it cannot be pinned
by an alias — and the same is true of the table definition the lambda receives, which is a `BookTableDef<SOURCE>` rather than a `BookTableDefType`. Since
filtering is all a join scope can do, write such a helper against `YawnQueryScopeWithWhere` instead, which every scope implements:

```kotlin
private fun <SOURCE : Any> YawnQueryScopeWithWhere<SOURCE, Book>.addLongBookFilter(
    books: BookTableDef<SOURCE>,
) {
    addGt(books.numberOfPages, 500)
}
```

That form has a useful side effect: leaving the source open means the same helper also works in a root entity query and in a projected query, which a
per-scope alias cannot do.

Use the concrete `EntityYawnQueryBuilder` type when a helper needs to own a larger piece of construction or decide which lambdas to apply. Use a generated
scope alias when the helper only needs DSL operations such as `addEq` or `join`.

`BaseYawnBuilder` and `BaseYawnQueryScope` are common implementation types, not the types to expose in application helpers. Prefer the concrete builder or
generated aliases such as `BookEntityQueryScope` and `BookProjectedQueryScope<Result>`, which preserve useful table-definition context. If direct generic
types are unavoidable, `SOURCE` is the root entity
of the whole query, `T` is the entity at the current scope, `DEF` is its generated definition, and a projected builder's `RETURNS` type is the terminal
result type.

## Reusing a join across query pieces

Every call to `join` creates another join. When independently applied filters need to refer to the same joined table, create a join reference once and apply
filters through that reference:

```kotlin
val query = yawn.query(BookTable)
val authorRef = query.joinRef { author }

query.applyJoinRef(authorRef) { authors ->
    addLike(authors.name, "J.%")
}

query.applyJoinRef(authorRef) { authors ->
    addLike(authors.name, "%n")
}

val books = query.list()
```

`joinRef` registers the join immediately. Its `get` method maps the root table definition supplied to a later `applyFilter` block to the table definition for
that exact join. `applyJoinRef` packages that mapping into a less error-prone form. `applyJoinRefs` is available when a single filter needs two or three
saved joins.

If a helper may be called after another part of the query has already registered the same root join, use `getOrCreateJoinRef` instead:

```kotlin
private fun EntityYawnQueryBuilder<Book, BookTableDefType>.applyAuthorFilter(
    authorName: String,
) {
    val authorRef = getOrCreateJoinRef { author }
    applyJoinRef(authorRef) { authors ->
        addEq(authors.name, authorName)
    }
}
```

Use `joinRef` when separate joins are intentional—for example, when two conditions must target two independently joined rows. Use `getOrCreateJoinRef` when
the filters should constrain the same joined row. Reusing a reference also avoids creating duplicate join paths in Hibernate Criteria queries.

Join references belong to the builder that created them. Do not save one globally or use it with another query builder, even if both builders query the
same entity.
