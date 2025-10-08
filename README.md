# yawn

**Yawn** is thin encapsulation of the *Hibernate Criteria Query API* in a **Kotlin-friendly**, **type-safe** and **intuitive** interface.

It leverages [KSP](https://kotlinlang.org/docs/ksp-overview.html) to generate **type-safe definitions** used to power **Yawn Queries**.

## Getting Started

1. Add the `com.faire.yawn.version` property to your Gradle properties (with the actual version in place of `<VERSION>`)

```properties
com.faire.yawn.version=<VERSION>
```

1. Add the Gradle dependencies to your build (with the actual version in place of `<VERSION>`)

```kotlin
plugins {
  id("com.faire.yawn")
}

dependencies {
  implementation("com.faire.yawn:yawn-api:<VERSION>")
}
```

1. Write a query!

```kotlin
  val yawn = Yawn(queryFactory = YourQueryFactory(...))
  val tolkienBooks = yawn.query(BookTable) { books ->
    val authors = join(books.author)
    addEq(authors.name, "J. R. R. Tolkien")
  }.list()
```

For more advanced details, read through our [docs](/docs/README.md)!

## Why Yawn?

### Full type safety

No longer guess column names and types. Begone brittle strings. No runtime errors when running your tests (or even worse, in prod).

With **Yawn**, it is impossible to pass in the wrong type, enforced by our friend the compiler; join paths are guaranteed to be correct; project to data classes
with ease.

### Find all the usages of column

Easy to refactor, investigate, and safely delete fields. Analyze cascading impacts of your changes with ease.

Leverage Kotlin’s powerful introspection and refactor tools like you already do with everything else.

### Intellisense Suggestions

Use the power of the IDE that we know and love, now for queries.

Get code suggestions and hints from your coding environment.

### Familiarity with the Hibernate Criteria syntax

Yawn keeps mostly the same syntax provided by Hibernate Criteria API, just adding type-safety as the cherry on top.

If you like that style of building queries, but want some type-safety on top, **Yawn** might just be for you.


## How does it work?

**Yawn** is just a thin wrapper on top of Hibernate queries. In order to power type-safe queries, **Yawn** generates objects representing the metadata of each
table and column related to a Hibernate entity annotated with `@YawnEntity`; so the migration can be completely unobtrusive and opt-in.

The generation is powered by [KSP](https://kotlinlang.org/docs/ksp-overview.html), and is composed by a set of `TableDef` (table definitions) that contain a set
of `ColumnDef` (column definitions). The generate code contains the type information as generics that is necessary to make the queries safe. For example, it
forces you to query columns that belong (or were joined from) to the table being queried; and it enforces the type of the values provided for comparison.

## Contributing

If you like Yawn, give us a star to help the project!

Have you found a bug or have a suggestion? Open an [issue](https://github.com/Faire/yawn/issues) and we will take a
look at it as soon as possible.

Do you want to contribute with a PR? Make sure to read our [Contributing Guide](/CONTRIBUTING.md)!
