# Sub-queries

How do sub-queries work on **Yawn**? As with our other APIs, it is actually heavily inspired by Hibernate, but comes with type-safety on top. Currently,
**Yawn** supports creating projected detached criteria that can be used in type-safe subquery restrictions.

## Basics

A sub-query may contain restrictions, joins, and even projections. However, it can only be used as a restriction argument. It’ll never be executed as a root
criteria.

## How to create a detached criteria

You can call `Yawn.createProjectedSubQuery(tableRef, query)` in order to construct a detached criteria. You can then use it from within a transaction in a
restriction.

## Example

```kotlin
    // outside the transaction
    val detachedCriteria = Yawn.createProjectedDetachedCriteria(DbPersonTable) { person ->
      addLike(person.name, "J.%")
      project(YawnProjections.distinct(person.name))
    }

    // within your transaction
    val books = yawn.query(DbBookTable) { books ->
      addIn(books.authorName, detachedCriteria)
      addLt(books.numberOfPages, 500)
    }.list()
```
