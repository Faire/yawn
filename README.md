# yawn

**Yawn** is a thin encapsulation of the *Hibernate Criteria Query API* into a **Kotlin-friendly**, **type-safe** and **intuitive** interface.

It leverages [KSP](https://kotlinlang.org/docs/ksp-overview.html) to generate **type-safe definitions** used to power **Yawn Queries**.

## Getting Started

### Adding the Dependencies

There are two sets of dependencies you must add to your project to use Yawn.

1. The `yawn-processor` dependency is what generates the definitions for your entities annotated with `@YawnEntity`.

That needs to be added as a `compileOnly` and also `ksp` dependency:

```kotlin
    compileOnly("com.faire.yawn:yawn-processor:$version")
    ksp("com.faire.yawn:yawn-processor:$version")
```

As an alternative, you can use the Yawn Gradle plugin to automatically add the processor for you - see the
[Yawn Gradle Plugin readme][yawn-gradle-plugin-readme] for more details. This is recommended for multi-module Gradle projects.

1. The `yawn-api` as a regular dependency in order to actually make queries:

```kotlin
    implementation("com.faire.yawn:yawn-api:$version")
```

### Annotate your Entities

Annotate your Hibernate entities with `@YawnEntity` in order to have the necessary table and column definitions generated for them.

```kotlin
@Entity
@Table(name = "books")
@YawnEntity // <-- add this
class Book {
  // ...
}
```

### Wire your QueryFactory

In order to hook Yawn into your Hibernate setup, you need to provide a `QueryFactory` implementation that knows how map the Yawn models into a Hibernate query.
For inspiration, you can check out the [`YawnTestQueryFactory`][yawn-test-query-factory] implementation.

Tip: wrap the Yawn class creation within your transaction management code to make it easier to use throughout your codebase!

### Write your queries

Finally, you are ready! Now you can write your type-safe queries using the power of Yawn:

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


[yawn-gradle-plugin-readme]: /yawn-gradle-plugin/README.md
[yawn-test-query-factory]: /yawn-database-test/src/main/kotlin/com/faire/yawn/setup/hibernate/YawnTestQueryFactory.kt
