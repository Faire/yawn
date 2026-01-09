# Sub-queries

How do sub-queries work on **Yawn**? As with our other APIs, it is actually heavily inspired by Hibernate, but comes with type-safety on top. Currently,
**Yawn** supports creating projected detached criteria that can be used in type-safe sub-query restrictions.

We support two types of sub-queries:

- Detached Sub-queries, which are created outside any query (or even transaction), and can even be reused.
- Correlated Sub-queries, which are created within a query and can access context of that query.

## Detached Sub-queries

You can call `Yawn.createProjectedDetachedCriteria(tableRef, query)` in order to construct a detached criteria. You can then use it from within a transaction in
a restriction.

```kotlin
    // outside the transaction
    val detachedSubQuery = Yawn.createProjectedDetachedCriteria(DbPersonTable) { person ->
        addLike(person.name, "J.%")
        project(YawnProjections.distinct(person.name))
    }

    // within your transaction
    val books = yawn.query(DbBookTable) { books ->
        addIn(books.authorName, detachedSubQuery)
        addLt(books.numberOfPages, 500)
    }.list()
```

## Correlated Sub-queries

Within a query, you can create a sub-query that can access the context of the parent query. This is called a "correlated sub-query".

```kotlin
val people = session.query(PersonTable) { people ->
    val correlatedSubQuery = createProjectedSubQuery(BookTable.forSubQuery()) { books ->
        addEq(books.author.foreignKey, people.id) // access people from outer query
        addGt(books.numberOfPages, 500)
        project(books.author.foreignKey)
    }

    addExists(correlatedSubQuery)
}.list()
```

## Risks
While **Yawn** provides guarantees through the type system that a sub-query is only used against columns of the correct
type, it _cannot_ guarantee that the sub-query only returns 1 row. This means that if a sub-query is used in an `addEq`
restriction and it returns multiple results, your database will likely fail and an exception will be thrown in your
application. See [#96](https://github.com/Faire/yawn/issues/96) for more details.
